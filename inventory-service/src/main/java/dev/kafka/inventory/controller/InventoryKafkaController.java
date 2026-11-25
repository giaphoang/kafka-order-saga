package dev.kafka.inventory.controller;

import dev.kafka.contracts.*;
import dev.kafka.inventory.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryKafkaController {
    private final InventoryService service;
    public InventoryKafkaController(InventoryService service) { this.service = service; }

    @KafkaListener(topics = Topics.ORDER_CREATED)
    public void reserve(OrderCreated event) throws Exception { service.reserve(event); }

    @KafkaListener(topics = Topics.PAYMENT_RESULT)
    public void releaseAfterDecline(PaymentResult event) { service.releaseAfterDecline(event); }
}
