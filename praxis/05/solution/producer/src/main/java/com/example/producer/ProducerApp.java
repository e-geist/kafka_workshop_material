package com.example.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Random;

import java.util.Properties;

public class ProducerApp {
    public static void main(String[] args) throws InterruptedException {
        String topicName = "asdf"; // Namen für das Topic hinterlegen, in das produziert werden soll

        Properties props = new Properties(); 
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:19092"); // Serverhost + IP von allen Brokern kommaseparirert
        
        // Serialisierungsklasse für Message Key, in diesem Fall einfach String
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // Serialisierungsklasse für Message Value, in diesem Fall einfach String
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Producer<String, String> producer = new KafkaProducer<String, String>(props); // Producer-Klasse mit Einstellungen anlegen

        int i = 0;
        Random rn = new Random();
        while (true) {
            String value = "Value " + Integer.toString(i++);
            String key = Integer.toString(rn.nextInt(20));
            ProducerRecord<String, String> record = new ProducerRecord<String, String>(topicName, key, value); // Instanz von record anlegen, der gesendet werden soll
            producer.send(record); // Record senden
            producer.flush();
            System.out.println("Sent: " + key + " " + value);
            Thread.sleep(1000); // 1 second delay
        }
    }
}