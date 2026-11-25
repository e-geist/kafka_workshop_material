package com.example.consumer;

import java.time.Instant;

// Eigene Klasse des Consumers, liegt in anderem Package als beim Producer (Part 4)
public record Measurement(String id, int value, Instant ts) {
}
