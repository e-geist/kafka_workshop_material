package com.example.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class MessageListener {

    @KafkaListener(topics = "${app.topic}", groupId = "measurement-group")
    public void listen(@Payload Measurement measurement,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.RECEIVED_KEY) String key) {
        System.out.printf("Received: partition = %d, key = %s, value = %s%n", partition, key, measurement);
    }
}
