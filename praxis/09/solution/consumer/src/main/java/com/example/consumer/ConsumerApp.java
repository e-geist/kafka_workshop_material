package com.example.consumer;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Random;
public class ConsumerApp {

    private static final int MAX_RETRIES = 3;
    private static final Random RND = new Random();

    public static void main(String[] args) {
        String topicName = "asdf";
        String deadLetterTopic = "asdf-dlt"; // Topic für nicht verarbeitbare Nachrichten

        // --- Consumer-Einstellungen ---
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:19092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        // WICHTIG: Auto-Commit AUS -> Offset wird manuell nach der Behandlung committet
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group_id_consumer1");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // --- Producer für das Dead Letter Topic ---
        Properties dltProps = new Properties();
        dltProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:19092");
        dltProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        dltProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Consumer<String, String> consumer = new KafkaConsumer<String, String>(props);
        Producer<String, String> dltProducer = new KafkaProducer<String, String>(dltProps);
        consumer.subscribe(Collections.singletonList(topicName));
        System.out.println("Start consuming...");

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));
            for (ConsumerRecord<String, String> record : records) {
                try {
                    processWithRetry(record);
                } catch (PoisonPillException e) {
                    // Dauerhaft kaputt -> ab ins Dead Letter Topic, NICHT blockieren
                    System.err.printf("Poison pill at offset %d: %s -> DLT%n", record.offset(), e.getMessage());
                    sendToDlt(dltProducer, deadLetterTopic, record, e.getMessage());
                } catch (TransientException e) {
                    // Retries erschöpft -> ebenfalls in DLT, damit die Partition weiterläuft
                    System.err.printf("Transient failure exhausted at offset %d: %s -> DLT%n", record.offset(), e.getMessage());
                    sendToDlt(dltProducer, deadLetterTopic, record, e.getMessage());
                }
                // Offset NACH der Behandlung committen (auch bei Skip rückt der Offset weiter)
                commitOffset(consumer, record);
            }
        }
    }

    // Verarbeitung mit begrenzten Wiederholungen bei transienten Fehlern
    private static void processWithRetry(ConsumerRecord<String, String> record)
            throws PoisonPillException, TransientException {
        int attempt = 0;
        while (true) {
            try {
                process(record);
                return; // Erfolg
            } catch (TransientException e) {
                attempt++;
               if (attempt >= MAX_RETRIES) {
                    throw e;
                }
                long backoff = 200L * attempt;
                System.err.printf("Transient error (Versuch %d/%d), retry in %dms: %s%n",
                        attempt, MAX_RETRIES, backoff, e.getMessage());
                sleep(backoff);
            }
        }
    }

    private static void process(ConsumerRecord<String, String> record)
            throws PoisonPillException, TransientException {
        String value = record.value();

        // 1. Format prüfen: muss mit "Value " beginnen
        if (value == null || !value.startsWith("Value ")) {
            throw new PoisonPillException("Unerwartetes Format: " + value);
        }

        // 2. Zahl extrahieren -> Parsefehler = Poison Pill
        int number;
        try {
            number = Integer.parseInt(value.substring("Value ".length()).trim());
        } catch (NumberFormatException e) {
            throw new PoisonPillException("Keine gültige Zahl in: " + value);
        }

        // 3. Fachliche Validierung -> negative Zahl ist ungültig
        if (number < 0) {
            throw new PoisonPillException("Negative Zahl nicht erlaubt: " + number);
        }

        // 4. Eigentliche Verarbeitung
        //    Simulierter transienter Fehler in 1 von 10 Fällen (z. B. DB kurz nicht erreichbar)
        if (RND.nextInt(10) == 0) {
            throw new TransientException("DB nicht erreichbar (simuliert)");
        }

        System.out.printf("Processed OK: partition = %d, offset = %d, key = %s, number = %d%n",
                record.partition(), record.offset(), record.key(), number);
    }

    private static void sendToDlt(Producer<String, String> producer, String dlt,
                                  ConsumerRecord<String, String> record, String reason) {
        ProducerRecord<String, String> pr = new ProducerRecord<String, String>(dlt, record.key(), record.value());
        pr.headers().add("error-reason", reason.getBytes());
        pr.headers().add("source-topic", record.topic().getBytes());
        producer.send(pr);
        producer.flush();
    }

    private static void commitOffset(Consumer<String, String> consumer, ConsumerRecord<String, String> record) {
        consumer.commitSync(Collections.singletonMap(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)));
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    // Exception für fehlerhafte Nachricht - bspw. Format oder fachlich ungültig
    static class PoisonPillException extends Exception {
        PoisonPillException(String msg) { super(msg); }
    }

    // Exception für temporäres Problem - bspw. Datenbankverbindung
    static class TransientException extends Exception {
        TransientException(String msg) { super(msg); }
    }
}