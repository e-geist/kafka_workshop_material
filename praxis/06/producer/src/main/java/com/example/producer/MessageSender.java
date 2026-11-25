package com.example.producer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

? // Annotation: Spring Bean, die beim Start automatisch instanziiert wird
public class MessageSender {

    private final ? kafkaTemplate; // Spring Klasse zum Senden von Nachrichten mit String Key und String Value
    private final String topicName;
    private final Random random = new Random();
    private int i = 0;

    // KafkaTemplate wird von Spring Boot anhand der application.yml konfiguriert und per Constructor Injection übergeben
    public MessageSender(? kafkaTemplate, // gleicher Typ wie Feld kafkaTemplate
                         ? String topicName) { // Annotation: Wert von app.topic aus application.yml injizieren
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    ? // Annotation: Methode jede Sekunde ausführen
    public void send() {
        String key = Integer.toString(random.nextInt(20));
        String value = "Value " + i++;

        // Nachricht mit Topic, Key und Value senden: asynchron, Rückgabe ist CompletableFuture<SendResult>
        kafkaTemplate.?(topicName, key, value)
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
