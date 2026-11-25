package com.example.dualwrite;

import com.google.gson.Gson;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

// Part 1: speichert Messungen in der Datenbank und sendet sie direkt an Kafka (Dual Write)
public class DualWriteProducerApp {

    private static final String TOPIC = "measurements";
    private static final int MEASUREMENTS = 50;
    private static final Gson GSON = new Gson();

    public static void main(String[] args) throws Exception {
        // jede n-te Messung schlägt nach dem Senden, aber vor dem Commit fehl (0 = nie)
        int failEvery = Integer.parseInt(System.getenv().getOrDefault("FAIL_EVERY", "0"));

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:19092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Random random = new Random();
        try (Connection db = DriverManager.getConnection("jdbc:postgresql://postgres:5432/workshop", "workshop", "workshop");
             KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            db.setAutoCommit(false);

            for (int i = 1; i <= MEASUREMENTS; i++) {
                String sensorId = "sensor-" + random.nextInt(5);
                MeasurementEvent event = new MeasurementEvent(UUID.randomUUID().toString(), sensorId, i, Instant.now().toString());
                try {
                    insertMeasurement(db, event);
                    producer.send(new ProducerRecord<>(TOPIC, sensorId, GSON.toJson(event))).get();
                    if (failEvery > 0 && i % failEvery == 0) {
                        throw new IllegalStateException("simulated error after send, before commit");
                    }
                    db.commit();
                    System.out.println("Saved and sent: " + event);
                } catch (Exception e) {
                    db.rollback();
                    System.out.println("Rollback: " + event + " (" + e.getMessage() + ")");
                }
                Thread.sleep(500);
            }
        }
        System.out.println("Done: " + MEASUREMENTS + " measurements");
    }

    private static void insertMeasurement(Connection db, MeasurementEvent event) throws SQLException {
        try (PreparedStatement stmt = db.prepareStatement("INSERT INTO measurement (sensor_id, value, ts) VALUES (?, ?, ?)")) {
            stmt.setString(1, event.sensorId());
            stmt.setInt(2, event.value());
            stmt.setTimestamp(3, Timestamp.from(Instant.parse(event.ts())));
            stmt.executeUpdate();
        }
    }
}
