package dev.kafka.orders.controller;

import dev.kafka.contracts.*;
import dev.kafka.orders.service.OrderStatusService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderKafkaController {
    private final OrderStatusService service;
    public OrderKafkaController(OrderStatusService service) { this.service = service; }

    @KafkaListener(topics = Topics.INVENTORY_RESULT)
    public void inventoryResult(InventoryResult event) {
        if (!event.reserved()) service.update(event.orderId(), "REJECTED", event.reason());
    }

    @KafkaListener(topics = Topics.PAYMENT_RESULT)
    public void paymentResult(PaymentResult event) {
        service.update(event.orderId(), event.approved() ? "CONFIRMED" : "PAYMENT_FAILED",
                event.approved() ? event.transactionId() : event.reason());
    }
}
