package com.example.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

? // Annotation: Klasse als Spring Boot Applikation markieren (Auto-Konfiguration + Component Scan im Package)
? // Annotation: geplante, regelmäßige Ausführung von Methoden aktivieren
public class ProducerApp {
    public static void main(String[] args) {
        SpringApplication.run(ProducerApp.class, args);
    }
}
