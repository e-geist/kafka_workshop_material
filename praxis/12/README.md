# Security Part 1: TLS und SCRAM

Kafka läuft als einzelner Knoten mit zwei Listenern: `SECURE` (Port 9092, SASL_SSL) für Clients und `CONTROLLER` (Port 9093, mTLS) für KRaft. Die Broker Konfiguration ist vollständig in `kafka/server.properties`. Der Container `certs` erzeugt beim ersten Start eine eigene CA (Certificate Authority), das Zertifikat des Brokers und einen Truststore (`certs/create-certs.sh`). Die SCRAM Benutzer `admin`, `producer-user` und `consumer-user` werden beim Formatieren des Speichers angelegt (`kafka/start.sh`). Keycloak startet ebenfalls, wird aber erst in Part 3 benötigt.

1. Kafka starten, `certs` und `keycloak` werden automatisch mitgestartet (dauert ca. 1 Minute)
    ```bash
    docker compose up -d kafka
    ```
1. `kafka/server.properties` ansehen
    1. Welche Authentifizierung nutzen Clients am Listener `SECURE`, welche der Broker am Listener `CONTROLLER`?
    1. Welcher Principal entsteht aus dem Broker Zertifikat mit dem DN (Distinguished Name) `CN=kafka,OU=broker,O=kafka-workshop`? Warum steht dieser Principal in `super.users`?
1. Zertifikat des Brokers anzeigen
    ```bash
    docker exec kafka keytool -list -v -keystore /etc/kafka/secrets/kafka.keystore.p12 -storepass changeit | grep -E "Alias|Owner|Issuer|DNSName|Valid"
    ```
    1. Wer hat das Zertifikat ausgestellt? Für welche Hostnamen gilt es?
1. Ohne Security Konfiguration verbinden
    ```bash
    docker exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --list
    docker logs kafka | grep "Failed authentication"
    ```
    1. Warum scheitert die Verbindung? Was steht im Log des Brokers?
    1. Hinweis: der `OutOfMemoryError` beim Client ist erwartet. Der Client liest die TLS Antwort des Brokers als Größe einer Kafka Antwort (ca. 352 MB) und kann diesen Puffer nicht anlegen. Ein OOM (Out Of Memory) beim Verbinden deutet fast immer auf ein falsches `security.protocol` hin
1. Lücken in `client/producer-user.properties` und `client/consumer-user.properties` (markiert mit `?`) füllen, Hinweise stehen jeweils im Kommentar darüber. Das Verzeichnis `client` ist im Container `kafka` unter `/etc/kafka/client` eingebunden.
    - [Authentifizierung mit SASL / SCRAM](https://kafka.apache.org/44/security/authentication-using-sasl/)
    - [Verschlüsselung mit TLS](https://kafka.apache.org/44/security/encryption-and-authentication-using-ssl/)
1. Mit `producer-user` verbinden (die Liste der Topics ist noch leer)
    ```bash
    docker exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --list \
      --command-config /etc/kafka/client/producer-user.properties
    ```
1. Mit falschem Passwort verbinden
    ```bash
    docker exec kafka bash -c 'sed "s/producer-secret/wrong/" /etc/kafka/client/producer-user.properties > /tmp/wrong.properties'
    docker exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --list --command-config /tmp/wrong.properties
    ```
    1. Welche Exception erscheint? Wird das Passwort im Klartext übertragen?

# Security Part 2: ACLs und Audit Log

Der Broker nutzt den `StandardAuthorizer` mit `allow.everyone.if.no.acl.found=false`. Ohne passende ACL ist alles verboten, nur `admin` und `kafka` sind Super User. Abgelehnte Zugriffe werden vom Logger `kafka.authorizer.logger` protokolliert (`kafka/log4j2.yaml`).

1. Topic als `admin` anlegen
    ```bash
    docker exec kafka /opt/kafka/bin/kafka-topics.sh --bootstrap-server kafka:9092 --create --topic measurements --partitions 3 \
      --command-config /etc/kafka/client/admin.properties
    ```
1. Als `producer-user` schreiben (Eingabe mit `Strg + C` beenden)
    ```bash
    docker exec -it kafka /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server kafka:9092 --topic measurements \
      --command-config /etc/kafka/client/producer-user.properties
    ```
1. Audit Log ansehen
    ```bash
    docker logs kafka | grep "is Denied"
    ```
    1. Welcher Principal wollte welche Operation auf welcher Ressource ausführen?
1. ACLs als `admin` anlegen: `producer-user` darf in `measurements` schreiben, `consumer-user` darf `measurements` mit der Consumer Group `measurement-group` lesen. Lücken (markiert mit `?`) füllen, `kafka-acls.sh` kennt Abkürzungen für die typischen Rechte von Producern und Consumern ([ACLs](https://kafka.apache.org/44/security/authorization-and-acls/), `kafka-acls.sh --help`)
    ```bash
    docker exec kafka /opt/kafka/bin/kafka-acls.sh --bootstrap-server kafka:9092 --command-config /etc/kafka/client/admin.properties \
      --add --allow-principal User:? --? --topic measurements
    docker exec kafka /opt/kafka/bin/kafka-acls.sh --bootstrap-server kafka:9092 --command-config /etc/kafka/client/admin.properties \
      --add --allow-principal User:? --? --topic measurements --group ?
    ```
1. Angelegte ACLs ausgeben
    ```bash
    docker exec kafka /opt/kafka/bin/kafka-acls.sh --bootstrap-server kafka:9092 --command-config /etc/kafka/client/admin.properties --list
    ```
    1. Welche Operationen haben die Abkürzungen jeweils erlaubt? Braucht ein Producer das Recht `CREATE`?
1. Erneut als `producer-user` schreiben und als `consumer-user` lesen
    ```bash
    docker exec -it kafka /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server kafka:9092 --topic measurements \
      --command-config /etc/kafka/client/producer-user.properties
    docker exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic measurements --from-beginning \
      --group measurement-group --command-config /etc/kafka/client/consumer-user.properties
    ```
1. Als `consumer-user` schreiben und mit einer anderen Consumer Group lesen
    ```bash
    docker exec -it kafka /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server kafka:9092 --topic measurements \
      --command-config /etc/kafka/client/consumer-user.properties
    docker exec kafka /opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server kafka:9092 --topic measurements --from-beginning \
      --group other-group --command-config /etc/kafka/client/consumer-user.properties
    docker logs kafka | grep "is Denied"
    ```
    1. Warum erscheint beim Schreiben eine `ClusterAuthorizationException` und keine `TopicAuthorizationException`?
    1. Wie könnte man einem Service mit einer einzigen ACL alle Topics freigeben, die mit `bw.` beginnen?

# Security Part 3: OIDC mit Keycloak

Keycloak enthält den Realm `workshop` mit den Clients `measurement-producer` und `measurement-consumer` (`keycloak/realm-workshop.json`). Beide holen sich ihr Token per Client Credentials, also als Service ohne Benutzer. Ein Audience Mapper trägt `kafka` als Empfänger in das Token ein. Der Broker prüft die Signatur der Tokens mit den öffentlichen Schlüsseln von Keycloak (Abschnitt OAUTHBEARER in `kafka/server.properties`). Der Spring Producer und Consumer verbinden sich per OAUTHBEARER und senden bzw. empfangen Measurements.

1. OpenID Konfiguration von Keycloak abfragen
    ```bash
    curl -s localhost:8080/realms/workshop/.well-known/openid-configuration | tr ',' '\n' | grep -E '"(issuer|token_endpoint|jwks_uri)"'
    ```
1. Token für `measurement-producer` holen und den Inhalt (Payload) ansehen
    ```bash
    TOKEN=$(curl -s -d grant_type=client_credentials -d client_id=measurement-producer -d client_secret=producer-oidc-secret \
      localhost:8080/realms/workshop/protocol/openid-connect/token | sed 's/.*"access_token":"\([^"]*\)".*/\1/')
    echo "$TOKEN" | cut -d. -f2 | tr '_-' '/+' | base64 -d 2>/dev/null | tr ',' '\n' | grep -E '"(iss|aud|azp|exp|iat|sub)"'
    ```
    1. Welche Claims prüft der Broker laut `server.properties`? Aus welchem Claim entsteht der Principal?
    1. Hinweis: von außerhalb des Docker Netzwerks steht in `iss` der Hostname `localhost`, Producer und Consumer holen ihr Token über `keycloak:8080` und bekommen `http://keycloak:8080/realms/workshop`
    1. Wie lange ist das Token gültig (`exp` minus `iat` in Sekunden)?
1. Lücken in `application.yml` von `producer` und `consumer` (markiert mit `?`) füllen, Hinweise stehen jeweils im Kommentar dahinter
    - [Authentifizierung mit SASL / OAUTHBEARER](https://kafka.apache.org/44/security/authentication-using-sasl/)
    - [Spring Boot Kafka Properties](https://docs.spring.io/spring-boot/appendix/application-properties/index.html#appendix.application-properties.integration)
1. Producer und Consumer starten
    ```bash
    docker compose up -d --build producer consumer
    docker logs kafka_workshop_12-producer-1
    docker logs kafka_workshop_12-consumer-1
    docker logs kafka | grep "is Denied"
    ```
    1. Ist die Anmeldung erfolgreich? Mit welchem Principal greifen Producer und Consumer zu?
1. ACLs für die Principals aus den Tokens anlegen, Lücken (markiert mit `?`) wie in Part 2 füllen
    ```bash
    docker exec kafka /opt/kafka/bin/kafka-acls.sh --bootstrap-server kafka:9092 --command-config /etc/kafka/client/admin.properties \
      --add --allow-principal User:? --? --topic measurements
    docker exec kafka /opt/kafka/bin/kafka-acls.sh --bootstrap-server kafka:9092 --command-config /etc/kafka/client/admin.properties \
      --add --allow-principal User:? --? --topic measurements --group measurement-group
    ```
1. Logs erneut ausgeben, bis `Sent` bzw. `Received` erscheint (der Consumer versucht es alle 10 Sekunden erneut)
1. Keycloak stoppen und Consumer neu starten
    ```bash
    docker compose stop keycloak
    docker logs --since 20s kafka_workshop_12-producer-1
    docker compose restart consumer
    docker logs kafka_workshop_12-consumer-1
    ```
    1. Warum sendet der Producer weiter, obwohl Keycloak nicht läuft? Warum startet der Consumer nicht mehr?
    1. Wie kann der Broker Tokens prüfen, ohne Keycloak bei jeder Anmeldung zu fragen?
    1. Was passiert mit bestehenden Verbindungen, wenn das Token abläuft? Was ändert sich mit `connections.max.reauth.ms` am Broker?
1. Keycloak und Consumer wieder starten
    ```bash
    docker compose start keycloak
    docker compose up -d consumer
    ```
1. Fragen
    1. Warum holen sich die Services ein eigenes Token per Client Credentials, statt das Token des Benutzers weiterzugeben?
    1. Warum stehen Client ID und Secret nicht in `application.yml`? Wo kommen sie in Produktion her?
1. Aufräumen, löscht auch die Zertifikate
    ```bash
    docker compose down -v
    ```

1. Beispiel-Lösung kann in `solution/client`, `solution/producer` bzw. `solution/consumer` gefunden werden
