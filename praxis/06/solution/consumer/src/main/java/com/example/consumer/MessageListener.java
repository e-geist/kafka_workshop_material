package com.example.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component // Spring Bean, wird beim Start automatisch instanziiert
public class MessageListener {

    // Listener Container übernimmt subscribe + poll Schleife, concurrency = Anzahl Consumer in der Consumer Group (Part 3)
    @KafkaListener(topics = "${app.topic}", groupId = "spring_group", concurrency = "3")
    public void listen(@Payload Measurement measurement,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.RECEIVED_KEY) String key,
                       Acknowledgment ack) {
        System.out.printf("Received: partition = %d, key = %s, value = %s, thread = %s%n",
                partition, key, measurement, Thread.currentThread().getName());
        ack.acknowledge(); // Offset committen (Part 2, ack-mode: manual)
    }
}
