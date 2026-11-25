# Kafka Client Error Handling Part 1

1. Gültige und Ungültige Nachrichten verschicken + empfangen - Gültiges Format: "Value <Positive Zahl>"
1. Java Producer und Consumer Lücken in `producer` und `consumer`  (markiert mit `?`) füllen
1. Kafka Cluster und Java Producer/Consumer via docker-compose starten

1. Logs von Producer ausgeben
    ```bash
    docker logs kafka_workshop_09-producer-1
    ```

1. Logs von Consumer ausgeben
    ```bash
    docker logs kafka_workshop_09-consumer-1
    ```
1. Öffne shell in laufendem Kafka Container, um DLT Topic zu lesen:
    ```bash
    docker exec --workdir /opt/kafka/bin/ -it kafka sh
    ./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic <dlt_topic_name>
    ```

1. Beispiel-Lösung kann in `solution/producer` bzw. `solution/consumer` gefunden werden

# Kafka Client Error Handling Part 2: Spring for Apache Kafka

Gleiche Nachrichten wie in Part 1, aber Fehlerbehandlung mit Spring. Der Spring Consumer (`spring-consumer`) nutzt eine eigene Consumer Group und schreibt in ein eigenes Dead Letter Topic `asdf-spring-dlt`.

Der Spring Consumer unterscheidet drei Fehlerarten:
- **Deserialisierungsfehler**: `ValueFormatDeserializer` (bereits vorhanden) prüft das Format `Value <Zahl>`, bspw. `Value abc17` schlägt bereits beim Deserialisieren fehl
- **Verarbeitungsfehler**: negative Zahl ist fachlich ungültig ➡️ `PoisonPillException` in Listener Methode
- **Transiente Fehler**: simulierter DB-Fehler ➡️ `TransientException` in Listener Methode

1. Lücken in `spring-consumer` (markiert mit `?`) füllen
    - `KafkaErrorConfig`: Error Handler mit Wiederholungen und Dead Letter Topic
    - `application.yml`: Deserialisierungsfehler abfangen
1. Hilfreiche Dokumentation
    - [DefaultErrorHandler](https://docs.spring.io/spring-kafka/reference/kafka/annotation-error-handling.html#default-eh)
    - [DeadLetterPublishingRecoverer](https://docs.spring.io/spring-kafka/reference/kafka/annotation-error-handling.html#dead-letters)
    - [ErrorHandlingDeserializer](https://docs.spring.io/spring-kafka/reference/kafka/serdes.html#error-handling-deserializer)
1. Kafka Cluster, Producer und beide Consumer starten (Spring Consumer ist über das Profil `spring` eingebunden)
    ```bash
    docker compose --profile spring up -d
    ```

1. Logs von Spring Consumer ausgeben
    ```bash
    docker logs kafka_workshop_09-spring-consumer-1
    ```
    1. Welche Nachrichten landen direkt im Dead Letter Topic, welche werden wiederholt?
    1. Warum wird die Listener Methode bei `Value abc...` gar nicht erst aufgerufen?
1. Dead Letter Topic des Spring Consumers lesen (in Shell im Kafka Container, siehe Part 1)
    ```bash
    ./kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic asdf-spring-dlt --from-beginning
    ```
    1. Zusätzlich die Header ausgeben mit `--formatter-property print.headers=true`: welche Informationen schreibt Spring zu jeder Nachricht?
1. Was passiert ohne `ErrorHandlingDeserializer`? (in `application.yml` wieder auf `ValueFormatDeserializer` als `value-deserializer` umstellen und neu starten, Consumer nach Prüfen der Logs direkt stoppen: `docker compose stop spring-consumer`)

1. Beispiel-Lösung kann in `solution/spring-consumer` gefunden werden
