package com.example.producer;

import java.time.Instant;

public record Measurement(String id, int value, Instant ts) {
}
