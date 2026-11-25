package com.example.outbox;

// Wird als JSON gesendet, eventId identifiziert das Event eindeutig (auch bei mehrfachem Senden)
public record MeasurementEvent(String eventId, String sensorId, int value, String ts) {
}
