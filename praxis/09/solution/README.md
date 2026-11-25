# Kafka Client Error Handling Part 1

1. Siehe `consumer` bzw. `producer` für Beispiel-Lösung

# Kafka Client Error Handling Part 2: Spring for Apache Kafka

1. Siehe `spring-consumer` für Beispiel-Lösung
1. `KafkaErrorConfig`
    - `@Configuration` und `@Bean`
    - `new DeadLetterPublishingRecoverer(template, ...)`
    - `new DefaultErrorHandler(recoverer, new FixedBackOff(500L, 2))`: bis zu 2 Wiederholungen im Abstand von 500ms, danach Dead Letter Topic
    - `errorHandler.addNotRetryableExceptions(PoisonPillException.class)`: keine Wiederholung bei fachlich ungültigen Nachrichten
    - Spring Boot nutzt den `DefaultErrorHandler` Bean automatisch für alle `@KafkaListener`
1. `application.yml`
    ```yaml
    spring:
      kafka:
        consumer:
          value-deserializer: org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
          properties:
            spring.deserializer.value.delegate.class: com.example.springconsumer.ValueFormatDeserializer
    ```
1. Ergebnis
    - `Value abc...`: Deserialisierungsfehler, Listener Methode wird nicht aufgerufen, `DeserializationException` wird nie wiederholt ➡️ direkt Dead Letter Topic
    - `Value -...`: `PoisonPillException` ➡️ direkt Dead Letter Topic
    - Transienter Fehler: Wiederholung nach 500ms, meist erfolgreich
1. Header im Dead Letter Topic: `kafka_dlt-exception-fqcn`, `kafka_dlt-exception-message`, `kafka_dlt-exception-stacktrace`, `kafka_dlt-original-topic`, `kafka_dlt-original-partition`, `kafka_dlt-original-offset`, `kafka_dlt-original-consumer-group`, ...
1. Ohne `ErrorHandlingDeserializer` tritt der Fehler bereits im `poll` auf: der Error Handler bekommt keinen Record, Offset rückt nie weiter ➡️ Consumer hängt endlos an derselben Nachricht (Poison Pill)
