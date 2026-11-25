package dev.kafka.payment.controller;

import dev.kafka.contracts.InventoryResult;
import dev.kafka.contracts.Topics;
import dev.kafka.payment.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentKafkaController {
    private final PaymentService service;
    public PaymentKafkaController(PaymentService service) { this.service = service; }

    @KafkaListener(topics = Topics.INVENTORY_RESULT)
    public void authorize(InventoryResult event) throws Exception { service.authorize(event); }
}
