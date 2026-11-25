package com.example.consumer;

import java.time.Instant;

// Wird erst in Part 4 genutzt: eigene Klasse des Consumers, liegt in anderem Package als beim Producer
public record Measurement(String id, int value, Instant ts) {
}
