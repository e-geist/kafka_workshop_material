package com.example.consumer;

import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

// Part 3: zählt Messungen pro Sensor und erkennt doppelte Events anhand der eventId
public class ConsumerApp {

    private static final Gson GSON = new Gson();

    public static void main(String[] args) throws Exception {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:19092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "outbox_group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, ?); // Offsets erst nach der Verarbeitung committen

        // in der Praxis eigene Datenbank des Consumers, hier zur Vereinfachung dieselbe
        try (Connection db = DriverManager.getConnection("jdbc:postgresql://postgres:5432/workshop", "workshop", "workshop");
             KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            db.setAutoCommit(false);
            consumer.subscribe(List.of("measurements"));

            while (true) {
                for (ConsumerRecord<String, String> record : consumer.poll(Duration.ofMillis(500))) {
                    MeasurementEvent event = GSON.fromJson(record.value(), MeasurementEvent.class);
                    try {
                        if (markProcessed(db, event)) {
                            updateSensorTotal(db, event);
                            System.out.println("Processed: " + event);
                        } else {
                            System.out.println("Duplicate skipped: " + event);
                        }
                        db.commit(); // Event ID und Verarbeitung gemeinsam speichern
                    } catch (SQLException e) {
                        db.rollback();
                        throw e;
                    }
                }
                consumer.?(); // Offsets synchron committen, erst nach erfolgreicher Verarbeitung aller Events des polls
            }
        }
    }

    // true, wenn das Event zum ersten Mal verarbeitet wird
    private static boolean markProcessed(Connection db, MeasurementEvent event) throws SQLException {
        // SQL: eventId in processed_events einfügen, bereits vorhandene IDs ohne Fehler ignorieren
        try (PreparedStatement stmt = db.prepareStatement("?")) {
            stmt.setObject(1, UUID.fromString(event.eventId()));
            return stmt.executeUpdate() == ?; // Anzahl eingefügter Zeilen, wenn die Event ID neu ist
        }
    }

    private static void updateSensorTotal(Connection db, MeasurementEvent event) throws SQLException {
        try (PreparedStatement stmt = db.prepareStatement("""
                INSERT INTO sensor_total (sensor_id, measurement_count, value_sum) VALUES (?, 1, ?)
                ON CONFLICT (sensor_id) DO UPDATE
                SET measurement_count = sensor_total.measurement_count + 1, value_sum = sensor_total.value_sum + EXCLUDED.value_sum""")) {
            stmt.setString(1, event.sensorId());
            stmt.setInt(2, event.value());
            stmt.executeUpdate();
        }
    }
}
