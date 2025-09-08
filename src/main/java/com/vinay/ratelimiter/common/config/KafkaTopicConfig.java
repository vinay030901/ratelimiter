package com.vinay.ratelimiter.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

// class to create kafka topics
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic notificationsRequestsTopic() {
        return TopicBuilder.name("notification.requests")
                .partitions(6)
                .replicas(1)
                .build();
    }
    // Dead-letter topic
    @Bean
    public NewTopic notificationsRequestDlt(){
        return TopicBuilder.name("notification.requests.dlt")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
