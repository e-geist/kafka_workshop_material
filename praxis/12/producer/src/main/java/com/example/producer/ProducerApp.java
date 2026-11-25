package com.example.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication // Spring Boot Applikation: Auto-Konfiguration + Component Scan im Package
@EnableScheduling // Methoden mit @Scheduled werden regelmäßig ausgeführt
public class ProducerApp {
    public static void main(String[] args) {
        SpringApplication.run(ProducerApp.class, args);
    }
}
