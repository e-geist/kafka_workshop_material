package com.example.springconsumer;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

? // Annotation: Klasse enthält Bean-Definitionen
public class KafkaErrorConfig {

    // Spring Boot nutzt diesen Error Handler automatisch für alle @KafkaListener
    ? // Annotation: Rückgabewert der Methode als Spring Bean registrieren
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        // Fehlerhafte Nachrichten in eigenes Dead Letter Topic schreiben (Partition -1: Kafka wählt Partition)
        DeadLetterPublishingRecoverer dltRecoverer = new DeadLetterPublishingRecoverer(?, // Template, über das ins Dead Letter Topic gesendet wird
                (record, ex) -> new TopicPartition(record.topic() + "-spring-dlt", -1));

        // Ausgabe pro Nachricht, die im Dead Letter Topic landet
        ConsumerRecordRecoverer recoverer = (record, ex) -> {
            System.out.printf("Failed at offset %d: %s -> DLT%n", record.offset(), rootCause(ex).getMessage());
            dltRecoverer.accept(record, ex);
        };

        // Bis zu 2 Wiederholungen im Abstand von 500ms, danach Recoverer (DLT)
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(?, ?); // Recoverer und BackOff mit festem Abstand

        // Wiederholung bei fachlich ungültigen Nachrichten sinnlos -> direkt ins DLT
        errorHandler.?(PoisonPillException.class); // Exception als nicht wiederholbar markieren
        return errorHandler;
    }

    private static Throwable rootCause(Throwable ex) {
        Throwable cause = ex;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }
}
