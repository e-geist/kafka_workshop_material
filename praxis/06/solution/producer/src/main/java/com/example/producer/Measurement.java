package com.example.producer;

import java.time.Instant;

// Wird als JSON gesendet (Part 4)
public record Measurement(String id, int value, Instant ts) {
}
