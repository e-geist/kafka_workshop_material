package com.example.consumer;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;


import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerApp {
    public static void main(String[] args) {
        String topicName = ?; // Namen für das Topic hinterlegen, das konsumiert werden soll

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, ?); // Serverhost + IP von allen Brokern kommaseparirert

        // Deserialisierungsklasse für Message Key, in diesem Fall einfach String
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Deserialisierungsklasse für Message Value, in diesem Fall einfach String
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        // Auto-Commit zunächst einschalten
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);

        // Consumer Group
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id_consumer1");

        Consumer<String, String> consumer = new KafkaConsumer<String, String>(props); // Consumer Klasse mit Einstellungen anlegen
        ? // auf Consumer Instanz für Topic Nachrichten subscriben

        System.out.println("Start consuming...");
        int pollCount = 0;
        while (true) {
            ConsumerRecords<String, String> records = ?; // auf Consumer Instanz Nachrichten abrufen
            for (ConsumerRecord<String, String> record : records) {
                // Nachrichten ausgeben
                System.out.printf(
                    "Received: partition = %d, key = %s, value = %s%n", ?, ?, ?
                );
            }
        }
    }
}

