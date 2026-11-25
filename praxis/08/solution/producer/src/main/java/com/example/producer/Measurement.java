package com.example.producer;

import java.time.Instant;

// JSON Schema wird vom Serializer aus dieser Klasse generiert, unit ist neu in Version 2 (Part 2)
public record Measurement(String id, int value, Instant ts, String unit) {
}
