package com.example.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Random;

import java.util.Properties;

public class ProducerApp {
    public static void main(String[] args) throws InterruptedException {
        String topicName = "Test"; // Namen für das Topic hinterlegen, in das produziert werden soll

        Properties props = new Properties(); 
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "broker-1:19092,broker-2:19092,broker-3:19092"); // Serverhost + IP von allen Brokern kommaseparirert

        // Serialisierungsklasse für Message Key, in diesem Fall einfach String
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Serialisierungsklasse für Message Value, in diesem Fall einfach String
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Producer<String, String> producer = new KafkaProducer<String, String>(props); // Producer-Klasse mit Einstellungen anlegen

        int i = 0;
        Random rn = new Random();
        while (true) {
            String key = Integer.toString(i % 20);
            String value = "Value " + Integer.toString(i++);
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(topicName, key, value); // Instanz von record anlegen, der gesendet werden soll
            producer.send(record); // Record senden
            System.out.println("Sent: " + key + " " + value);
            Thread.sleep(1000); // 1 second delay
        }
    }
}