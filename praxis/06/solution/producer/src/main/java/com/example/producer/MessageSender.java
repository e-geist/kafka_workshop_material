package com.example.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Random;

@Component // Spring Bean, wird beim Start automatisch instanziiert
public class MessageSender {

    private final KafkaTemplate<String, Measurement> kafkaTemplate;
    private final String topicName;
    private final Random random = new Random();
    private int i = 0;

    // KafkaTemplate wird von Spring Boot anhand der application.yml konfiguriert und per Constructor Injection übergeben
    public MessageSender(KafkaTemplate<String, Measurement> kafkaTemplate, @Value("${app.topic}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @Scheduled(fixedRate = 1000) // jede Sekunde ausführen
    public void send() {
        String key = Integer.toString(random.nextInt(20));
        Measurement value = new Measurement(key, i++, Instant.now());

        // send ist asynchron und gibt CompletableFuture<SendResult> zurück
        kafkaTemplate.send(topicName, key, value)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        System.out.println("Error while sending: " + ex.getMessage());
                        return;
                    }
                    System.out.printf("Sent: %s %s (partition = %d, offset = %d)%n",
                            key, value, result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                });
    }
}
