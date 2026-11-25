package com.example.producer;

import java.time.Instant;

// Wird erst in Part 4 genutzt: wird als JSON gesendet
public record Measurement(String id, int value, Instant ts) {
}
