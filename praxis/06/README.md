# Spring for Apache Kafka Part 1

Gleiches Szenario wie Material 05, aber mit Spring Boot und Spring for Apache Kafka. Serializer und Topic Name sind bereits in `application.yml` konfiguriert, Fokus liegt auf den Spring Annotationen, Klassen und der Spring Boot Konfiguration.

Das Topic wird nicht automatisch vom Broker angelegt (`KAFKA_AUTO_CREATE_TOPICS_ENABLE: false`), sondern von der Producer Applikation.

1. Lücken in `producer` und `consumer` (markiert mit `?`) füllen, Hinweise stehen jeweils im Kommentar dahinter
    - Producer: `ProducerApp`, `TopicConfig`, `MessageSender`, `application.yml`
    - Consumer: `ConsumerApp`, `MessageListener`, `application.yml`
1. Hilfreiche Dokumentation
    - [Spring Boot Kafka Konfiguration](https://docs.spring.io/spring-boot/reference/messaging/kafka.html)
    - [@SpringBootApplication](https://docs.spring.io/spring-boot/reference/using/using-the-springbootapplication-annotation.html)
    - [@Configuration und @Bean](https://docs.spring.io/spring-framework/reference/core/beans/java/basic-concepts.html)
    - [@Component](https://docs.spring.io/spring-framework/reference/core/beans/classpath-scanning.html)
    - [@Value](https://docs.spring.io/spring-framework/reference/core/beans/annotation-config/value-annotations.html)
    - [@EnableScheduling und @Scheduled](https://docs.spring.io/spring-framework/reference/integration/scheduling.html#scheduling-annotation-support)
    - [Topics anlegen mit NewTopic und TopicBuilder](https://docs.spring.io/spring-kafka/reference/kafka/configuring-topics.html)
    - [Nachrichten senden mit KafkaTemplate](https://docs.spring.io/spring-kafka/reference/kafka/sending-messages.html)
    - [@KafkaListener](https://docs.spring.io/spring-kafka/reference/kafka/receiving-messages/listener-annotation.html)
    - [Payload und Header als Parameter (Partition, Key, ...)](https://docs.spring.io/spring-kafka/reference/kafka/receiving-messages/listener-annotation.html#consumer-record-metadata)
1. Kafka Cluster und Spring Producer/Consumer via docker compose starten
    ```bash
    docker compose up -d
    ```

1. Logs von Producer ausgeben
    ```bash
    docker logs kafka_workshop_06-producer-1
    ```

1. Logs von Consumer ausgeben
    ```bash
    docker logs kafka_workshop_06-consumer-1
    ```
    - Falls der Consumer vor dem Producer startet, erscheinen kurz Warnungen `UNKNOWN_TOPIC_OR_PARTITION`, bis der Producer das Topic angelegt hat
1. Vergleich mit Material 05
    - Wo ist die `poll` Schleife geblieben?
    - Wer erstellt die Instanzen von `KafkaTemplate` und dem Consumer?
    - Welcher Thread verarbeitet welche Partitionen?

1. Beispiel-Lösung kann in `solution/producer` bzw. `solution/consumer` gefunden werden (enthält bereits alle Parts)

# Spring for Apache Kafka Part 2: Manuelles Commit

1. [Dokumentation Commit von Offsets und AckMode](https://docs.spring.io/spring-kafka/reference/kafka/receiving-messages/message-listener-container.html#committing-offsets)
1. Im Consumer auf manuelles Committen umstellen
    1. In `application.yml` des Consumers den Listener Container auf manuelles Committen umstellen
    1. Listener Methode um einen Parameter erweitern, über den Offsets bestätigt werden können, diesen aber zunächst **nicht** aufrufen
    1. Consumer neu bauen und starten
        ```bash
        docker compose up -d consumer
        ```
    1. Was sieht man in der Auflistung der Consumer Group `spring_group`? (siehe Material 04 für Befehle)
    1. Was passiert, wenn der Consumer neu gestartet wird?
1. Offsets in Listener Methode nach Verarbeitung bestätigen und erneut prüfen

# Spring for Apache Kafka Part 3: Concurrency

1. Im Consumer `@KafkaListener` so konfigurieren, dass 3 Consumer in der Consumer Group laufen
    1. Welche Threads verarbeiten jetzt welche Partitionen? (siehe `thread = ...` in Logs)
    1. Was passiert bei 4 Consumern und 3 Partitionen?

# Spring for Apache Kafka Part 4: JSON

1. [Dokumentation JSON Serialisierung](https://docs.spring.io/spring-kafka/reference/kafka/serdes.html#json-serde)
1. Producer soll statt String eine Instanz von `Measurement` (bereits vorhanden) als JSON senden
    1. `KafkaTemplate` und `send` auf `Measurement` umstellen
    1. In `application.yml` des Producers den Value Serializer auf JSON (Jackson) umstellen
1. Consumer soll `Measurement` (eigene Klasse im Package `com.example.consumer`) empfangen
    1. Listener Methode auf `Measurement` umstellen
    1. In `application.yml` des Consumers den Value Deserializer auf JSON (Jackson) umstellen
    1. Producer und Consumer neu bauen und starten, Consumer Logs prüfen
        - ⚠️ Fehler erwartet, die Logs laufen sehr schnell voll: Consumer nach dem Prüfen der Logs direkt stoppen (`docker compose stop consumer`)
    1. Warum kann der Consumer die Nachricht nicht deserialisieren? Welche Information schickt der Producer mit?
    1. Warum bleibt der Consumer an derselben Nachricht hängen? (mehr dazu in Material 09)
1. Consumer so konfigurieren, dass er die Typ Information des Producers ignoriert und immer in `com.example.consumer.Measurement` deserialisiert
