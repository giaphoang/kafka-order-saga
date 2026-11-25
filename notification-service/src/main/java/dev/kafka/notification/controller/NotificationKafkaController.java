package dev.kafka.notification.controller;

import dev.kafka.contracts.*;
import dev.kafka.notification.service.NotificationService;
import org.springframework.kafka.annotation.*;
import org.springframework.kafka.retrytopic.TopicSuffixingStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
public class NotificationKafkaController {
    private final NotificationService service;
    public NotificationKafkaController(NotificationService service) { this.service = service; }

    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 1000, multiplier = 2),
            topicSuffixingStrategy = TopicSuffixingStrategy.SUFFIX_WITH_INDEX_VALUE,
            dltStrategy = org.springframework.kafka.retrytopic.DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = Topics.ORDER_STATUS)
    public void notifyCustomer(OrderStatusChanged event) { service.sendOrderEmail(event); }

    @DltHandler
    public void deadLetter(OrderStatusChanged event) { service.recordDeadLetter(event); }
}
