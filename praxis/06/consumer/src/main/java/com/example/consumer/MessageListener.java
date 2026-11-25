package com.example.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

? // Annotation: Spring Bean, die beim Start automatisch instanziiert wird
public class MessageListener {

    // Listener Container übernimmt subscribe + poll Schleife und ruft diese Methode pro Nachricht auf
    ? // Annotation: Methode als Listener für das Topic aus app.topic mit Consumer Group spring_group registrieren
    public void listen(? String value, // Annotation: Value der Nachricht als Parameter
                       ? int partition, // Annotation: Partition aus dem Header der Nachricht auslesen
                       @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        System.out.printf("Received: partition = %d, key = %s, value = %s, thread = %s%n",
                partition, key, value, Thread.currentThread().getName());
    }
}
