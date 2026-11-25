package com.example.producer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

? // Annotation: Klasse enthält Bean-Definitionen
public class TopicConfig {

    ? // Annotation: Rückgabewert der Methode als Spring Bean registrieren (KafkaAdmin legt das Topic dann beim Start an)
    public NewTopic topic(@Value("${app.topic}") String topicName) {
        return ?; // Topic mit Namen topicName, 3 Partitionen und Replikationsfaktor 1 über TopicBuilder bauen
    }
}
