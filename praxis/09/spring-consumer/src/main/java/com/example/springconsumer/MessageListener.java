package com.example.springconsumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class MessageListener {

    private static final Random RND = new Random();

    // Keine Fehlerbehandlung im Listener: Exceptions werden an den DefaultErrorHandler weitergereicht
    @KafkaListener(topics = "${app.topic}", groupId = "spring_group")
    public void listen(@Payload String value,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.OFFSET) long offset) {
        // Format wurde bereits vom ValueFormatDeserializer geprüft
        int number = Integer.parseInt(value.substring("Value ".length()));

        // Fachliche Validierung -> negative Zahl ist ungültig
        if (number < 0) {
            throw new PoisonPillException("Negative Zahl nicht erlaubt: " + number);
        }

        // Simulierter transienter Fehler in 1 von 10 Fällen (z. B. DB kurz nicht erreichbar)
        if (RND.nextInt(10) == 0) {
            System.out.printf("Transient error at offset %d -> retry%n", offset);
            throw new TransientException("DB nicht erreichbar (simuliert)");
        }

        System.out.printf("Processed OK: partition = %d, offset = %d, number = %d%n", partition, offset, number);
    }
}
