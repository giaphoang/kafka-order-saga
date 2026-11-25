package dev.kafka.orders.config;

import dev.kafka.contracts.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {
    @Bean NewTopic orders() { return new NewTopic(Topics.ORDER_CREATED, 6, (short) 1); }
    @Bean NewTopic inventory() { return new NewTopic(Topics.INVENTORY_RESULT, 6, (short) 1); }
    @Bean NewTopic payments() { return new NewTopic(Topics.PAYMENT_RESULT, 6, (short) 1); }
    @Bean NewTopic statuses() { return new NewTopic(Topics.ORDER_STATUS, 6, (short) 1); }
}
