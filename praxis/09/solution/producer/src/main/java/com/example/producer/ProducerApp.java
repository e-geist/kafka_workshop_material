package com.example.producer;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Random;
import java.util.Properties;
public class ProducerApp {
    public static void main(String[] args) throws InterruptedException {
        String topicName = "asdf"; // Namen für das Topic hinterlegen, in das produziert werden soll
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:19092"); // Serverhost + IP von allen Brokern kommasepariert
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        Producer<String, String> producer = new KafkaProducer<String, String>(props);
        int i = 0;
        Random rn = new Random();
        while (true) {
            String key = Integer.toString(rn.nextInt(20));
            String value;

            int dice = rn.nextInt(10); // 0..9 entscheidet, was gesendet wird
            if (dice < 6) {
                // 60 %: gültige Nachricht -> erwartetes Format "Value <Positive Zahl>"
                value = "Value " + Integer.toString(i);
            } else if (dice < 8) {
                // 20 %: kaputtes Format (keine Zahl hinten) -> Poison Pill
                value = "Value abc" + i;
            } else {
                // 20 %: fachlich ungültig (negative Zahl)
                value = "Value -" + Integer.toString(i);
            }

            ProducerRecord<String, String> record = new ProducerRecord<String, String>(topicName, key, value);
            producer.send(record);
            producer.flush();
            System.out.println("Sent: " + key + " " + value);
            i++;
            Thread.sleep(1000); // 1 second delay
        }
    }
}