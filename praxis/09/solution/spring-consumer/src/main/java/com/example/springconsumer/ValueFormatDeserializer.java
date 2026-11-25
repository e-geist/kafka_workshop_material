package com.example.springconsumer;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.nio.charset.StandardCharsets;

// Prüft bereits beim Deserialisieren das Format "Value <Zahl>" -> ungültiges Format = Deserialisierungsfehler
public class ValueFormatDeserializer implements Deserializer<String> {

    @Override
    public String deserialize(String topic, byte[] data) {
        String value = new String(data, StandardCharsets.UTF_8);
        if (!value.matches("Value -?\\d+")) {
            throw new SerializationException("Keine gültige Zahl in: " + value);
        }
        return value;
    }
}
