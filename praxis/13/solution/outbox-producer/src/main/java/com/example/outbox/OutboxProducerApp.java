package com.example.outbox;

import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Random;
import java.util.UUID;

// Part 2: speichert Messung und Event in einer Datenbank Transaktion, OutboxRelay sendet die Events an Kafka
public class OutboxProducerApp {

    static final String DB_URL = "jdbc:postgresql://postgres:5432/workshop";
    static final String DB_USER = "workshop";
    static final String DB_PASSWORD = "workshop";
    private static final int MEASUREMENTS = 50;
    private static final Gson GSON = new Gson();

    public static void main(String[] args) throws Exception {
        // jede n-te Messung schlägt vor dem Commit fehl (0 = nie)
        int failEvery = Integer.parseInt(System.getenv().getOrDefault("FAIL_EVERY", "0"));

        // Relay läuft in eigenem Thread mit eigener Datenbank Verbindung weiter, auch wenn alle Messungen gespeichert sind
        new Thread(new OutboxRelay(), "outbox-relay").start();

        Random random = new Random();
        try (Connection db = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            db.setAutoCommit(false); // mehrere Statements in einer Transaktion

            for (int i = 1; i <= MEASUREMENTS; i++) {
                String sensorId = "sensor-" + random.nextInt(5);
                MeasurementEvent event = new MeasurementEvent(UUID.randomUUID().toString(), sensorId, i, Instant.now().toString());
                try {
                    insertMeasurement(db, event);
                    insertOutbox(db, event);
                    if (failEvery > 0 && i % failEvery == 0) {
                        throw new IllegalStateException("simulated error before commit");
                    }
                    db.commit(); // Messung und Event werden gemeinsam gespeichert
                    System.out.println("Saved with outbox entry: " + event);
                } catch (Exception e) {
                    db.rollback(); // Messung und Event werden gemeinsam verworfen
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

    private static void insertOutbox(Connection db, MeasurementEvent event) throws SQLException {
        // gleiche Verbindung wie insertMeasurement ➡️ gleiche Transaktion
        try (PreparedStatement stmt = db.prepareStatement("INSERT INTO outbox (event_id, message_key, payload) VALUES (?, ?, ?)")) {
            stmt.setObject(1, UUID.fromString(event.eventId()));
            stmt.setString(2, event.sensorId());
            stmt.setString(3, GSON.toJson(event));
            stmt.executeUpdate();
        }
    }
}
