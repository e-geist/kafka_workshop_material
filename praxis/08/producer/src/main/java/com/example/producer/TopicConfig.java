package com.example.producer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration // Klasse enthält Bean-Definitionen
public class TopicConfig {

    @Bean // KafkaAdmin legt alle NewTopic Beans beim Start an, falls sie noch nicht existieren
    public NewTopic topic(@Value("${app.topic}") String topicName) {
        return TopicBuilder.name(topicName)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
