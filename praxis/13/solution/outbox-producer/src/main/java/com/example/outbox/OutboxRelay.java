package com.example.outbox;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

// Liest noch nicht gesendete Events aus der Tabelle outbox, sendet sie an Kafka und markiert sie als gesendet
public class OutboxRelay implements Runnable {

    private static final String TOPIC = "measurements";

    // Part 3: jedes n-te gesendete Event stürzt der Relay nach dem Senden, aber vor dem Markieren ab (0 = nie)
    private final int crashEvery = Integer.parseInt(System.getenv().getOrDefault("RELAY_CRASH_EVERY", "0"));
    private int sent = 0;

    @Override
    public void run() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:19092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        try (Connection db = DriverManager.getConnection(OutboxProducerApp.DB_URL, OutboxProducerApp.DB_USER, OutboxProducerApp.DB_PASSWORD);
             KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            db.setAutoCommit(false);
            while (true) {
                try {
                    publishNextEntry(db, producer);
                } catch (Exception e) {
                    db.rollback(); // Eintrag bleibt ungesendet markiert und wird beim nächsten Durchlauf erneut gesendet
                    System.out.println("Relay error, retrying: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void publishNextEntry(Connection db, KafkaProducer<String, String> producer) throws Exception {
        // ältester ungesendeter Eintrag, FOR UPDATE SKIP LOCKED sperrt ihn für weitere Relay Instanzen
        try (PreparedStatement select = db.prepareStatement(
                "SELECT id, message_key, payload FROM outbox WHERE published_at IS NULL ORDER BY id LIMIT 1 FOR UPDATE SKIP LOCKED");
             ResultSet rs = select.executeQuery()) {
            if (!rs.next()) {
                db.commit();
                Thread.sleep(500);
                return;
            }
            long id = rs.getLong("id");

            // synchron senden: Eintrag erst als gesendet markieren, wenn der Broker das Event bestätigt hat
            producer.send(new ProducerRecord<>(TOPIC, rs.getString("message_key"), rs.getString("payload"))).get();
            sent++;
            if (crashEvery > 0 && sent % crashEvery == 0) {
                throw new IllegalStateException("simulated crash after send, before marking outbox entry " + id);
            }

            try (PreparedStatement update = db.prepareStatement("UPDATE outbox SET published_at = now() WHERE id = ?")) {
                update.setLong(1, id);
                update.executeUpdate();
            }
            db.commit();
            System.out.println("Published outbox entry " + id);
        }
    }
}
