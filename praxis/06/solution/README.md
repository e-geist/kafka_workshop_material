# Spring for Apache Kafka Part 1

1. Siehe `consumer` bzw. `producer` für Beispiel-Lösung, Code enthält bereits alle Parts
1. Annotationen und Funktionen
    - `ProducerApp` / `ConsumerApp`: `@SpringBootApplication`, Producer zusätzlich `@EnableScheduling`
    - `TopicConfig`: `@Configuration`, `@Bean`, `TopicBuilder.name(topicName).partitions(3).replicas(1).build()`
    - `MessageSender`: `@Component`, `KafkaTemplate<String, String>` als Feld und Konstruktor Parameter, `@Value("${app.topic}")`, `@Scheduled(fixedRate = 1000)`, `kafkaTemplate.send(topicName, key, value)`
    - `MessageListener`: `@Component`, `@KafkaListener(topics = "${app.topic}", groupId = "spring_group")`, `@Payload`, `@Header(KafkaHeaders.RECEIVED_PARTITION)`
    - `application.yml` (Producer und Consumer): `spring.kafka.bootstrap-servers: kafka:19092`
1. Vergleich mit Material 05
    - `poll` Schleife steckt im Listener Container von Spring, die Listener Methode wird pro Nachricht aufgerufen
    - Spring erstellt `KafkaTemplate` und Consumer anhand von `application.yml` und übergibt sie per Dependency Injection
    - Ohne `concurrency` verarbeitet ein Thread alle Partitionen

# Spring for Apache Kafka Part 2: Manuelles Commit

1. `application.yml` des Consumers:
    ```yaml
    spring:
      kafka:
        listener:
          ack-mode: manual
    ```
1. Listener Methode um Parameter `Acknowledgment ack` erweitern
1. Ohne `ack.acknowledge()` werden keine Offsets committed: Current Offset bleibt stehen, Lag wird größer
    ```bash
    docker exec --workdir /opt/kafka/bin/ -it kafka sh
    ./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --describe --group spring_group
    ```
1. Nach Neustart werden alle nicht bestätigten Nachrichten erneut verarbeitet
1. Mit `ack.acknowledge()` am Ende der Verarbeitung wird der Offset committed, Lag geht wieder gegen 0

# Spring for Apache Kafka Part 3: Concurrency

1. `@KafkaListener(topics = "${app.topic}", groupId = "spring_group", concurrency = "3")`
1. 3 Threads (`...#0-0-C-1`, `...#0-1-C-1`, `...#0-2-C-1`) mit jeweils einer eigenen Partition
1. Bei `concurrency = "4"` und 3 Partitionen bekommt ein Consumer keine Partition und bleibt untätig

# Spring for Apache Kafka Part 4: JSON

1. Producer
    - `KafkaTemplate<String, Measurement>`, `new Measurement(key, i++, Instant.now())` senden
    - `value-serializer: org.springframework.kafka.support.serializer.JacksonJsonSerializer`
1. Consumer
    - Listener Methode mit Parameter `@Payload Measurement measurement`
    - `value-deserializer: org.springframework.kafka.support.serializer.JacksonJsonDeserializer`
1. Fehler: `The class 'com.example.producer.Measurement' is not in the trusted packages`
    - Producer schreibt den Klassennamen in den Header `__TypeId__`, Consumer versucht diese Klasse zu laden
    - Klasse existiert im Consumer nicht bzw. Package ist nicht vertrauenswürdig
    - Deserialisierungsfehler tritt vor der Listener Methode auf, Offset rückt nie weiter ➡️ Poison Pill (Lösung mit `ErrorHandlingDeserializer` in Material 09)
1. Lösung in `application.yml` des Consumers:
    ```yaml
    spring:
      kafka:
        consumer:
          properties:
            spring.json.use.type.headers: false
            spring.json.value.default.type: com.example.consumer.Measurement
    ```
