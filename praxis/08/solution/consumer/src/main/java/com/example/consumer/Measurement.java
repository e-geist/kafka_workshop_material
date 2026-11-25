package com.example.consumer;

import java.time.Instant;

// Eigene Klasse des Consumers, kennt das Feld unit aus Version 2 nicht (Part 2)
public record Measurement(String id, int value, Instant ts) {
}
