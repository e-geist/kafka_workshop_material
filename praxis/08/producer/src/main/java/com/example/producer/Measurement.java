package com.example.producer;

import java.time.Instant;

// JSON Schema wird vom Serializer aus dieser Klasse generiert
public record Measurement(String id, int value, Instant ts) {
}
