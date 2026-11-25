# Security

1. Siehe `client`, `producer` und `consumer` für Beispiel-Lösung
1. Part 1: TLS und SCRAM
    - `security.protocol=SASL_SSL`, `sasl.mechanism=SCRAM-SHA-512`, `org.apache.kafka.common.security.scram.ScramLoginModule`, `ssl.truststore.location=/etc/kafka/secrets/truststore.p12`
    - Clients am Listener `SECURE`: SCRAM oder OAUTHBEARER über TLS; Broker am Listener `CONTROLLER`: mTLS (mutual TLS) mit seinem eigenen Zertifikat (`listener.name.controller.ssl.client.auth=required`)
    - `ssl.principal.mapping.rules` macht aus `CN=kafka,OU=broker,O=kafka-workshop` den Principal `User:kafka`; der Broker braucht als Super User Zugriff auf den Controller, ohne Eintrag in `super.users` würde der Authorizer ihn abweisen
    - Zertifikat ausgestellt von `CN=Workshop CA`, gültig für die Hostnamen `kafka` und `localhost` (SAN, Subject Alternative Name)
    - ohne Konfiguration spricht der Client Klartext mit einem TLS Listener ➡️ `SSL handshake failed` im Log des Brokers    - falsches Passwort: `SaslAuthenticationException ... invalid credentials`; SCRAM überträgt das Passwort nicht, sondern beweist per Challenge Response, dass der Client es kennt; zusätzlich ist die Verbindung per TLS verschlüsselt
1. Part 2: ACLs und Audit Log
    - ohne ACL: `TopicAuthorizationException`, im Log `Principal = User:producer-user is Denied operation = DESCRIBE ... on resource = Topic:LITERAL:measurements ... based on rule DefaultDeny`
    - `--allow-principal User:producer-user --producer --topic measurements`
    - `--allow-principal User:consumer-user --consumer --topic measurements --group measurement-group`
    - `--producer` erlaubt `WRITE`, `DESCRIBE` und `CREATE` auf dem Topic; `CREATE` braucht ein Producer nur, wenn er Topics selbst anlegt ➡️ in Produktion Topics zentral anlegen und `CREATE` weglassen (ACLs einzeln mit `--operation Write --operation Describe`)
    - `--consumer` erlaubt `READ` und `DESCRIBE` auf dem Topic und `READ` auf der Consumer Group
    - `ClusterAuthorizationException`: der Producer ist standardmäßig idempotent und holt sich zuerst eine Producer ID; dafür braucht er `WRITE` auf mindestens einem Topic (oder `IDEMPOTENT_WRITE` auf dem Cluster), `consumer-user` hat beides nicht
    - andere Consumer Group: `GroupAuthorizationException`, Consumer Groups sind eigene Ressourcen mit eigenen ACLs
    - Prefixed ACL: `--resource-pattern-type prefixed --topic bw.` erlaubt alle Topics, die mit `bw.` beginnen
1. Part 3: OIDC mit Keycloak
    - `protocol: SASL_SSL`, `sasl.mechanism: OAUTHBEARER`, `org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginCallbackHandler`, `http://keycloak:8080/realms/workshop/protocol/openid-connect/token`
    - Broker prüft Signatur (Schlüssel von `jwks_uri`), `iss` (`expected.issuer`), `aud` (`expected.audience`, `kafka`) und `exp`; Principal aus `azp` (`sasl.oauthbearer.sub.claim.name`) ➡️ `User:measurement-producer` bzw. `User:measurement-consumer`
    - Gültigkeit 300 Sekunden (`accessTokenLifespan` im Realm), der Client holt vor Ablauf automatisch ein neues Token
    - Anmeldung erfolgreich, aber ohne ACLs `TopicAuthorizationException` ➡️ Authentifizierung über Keycloak, Autorisierung weiterhin über Kafka ACLs
    - `--allow-principal User:measurement-producer --producer`, `--allow-principal User:measurement-consumer --consumer`
    - Keycloak gestoppt: bestehende Verbindungen bleiben angemeldet, der Producer sendet weiter; der neu gestartete Consumer bekommt kein Token (`UnknownHostException: keycloak`) und beendet sich
    - der Broker lädt die öffentlichen Schlüssel (JWKS) einmal und hält sie im Cache, Tokens werden lokal über ihre Signatur geprüft
    - ohne `connections.max.reauth.ms` (Standard 0) bleibt eine Verbindung nach Ablauf des Tokens bestehen; mit gesetztem Wert muss sich der Client regelmäßig neu anmelden, abgelaufene Tokens werden dann abgelehnt
    - Token des Benutzers läuft ab und gehört zu einem anderen Kontext; der Service handelt als eigener technischer Benutzer mit eigenen ACLs (siehe Folie "Security in asynchroner Verarbeitung")
    - Secrets nicht ins Repository: in Produktion aus Umgebungsvariablen, Kubernetes Secrets oder einem Secret Store (bspw. Vault)
