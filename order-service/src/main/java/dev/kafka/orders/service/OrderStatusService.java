package dev.kafka.orders.service;

import dev.kafka.contracts.OrderStatusChanged;
import dev.kafka.contracts.Topics;
import dev.kafka.orders.repository.OrderRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.UUID;

@Service
public class OrderStatusService {
    private final OrderRepository orders;
    private final KafkaTemplate<String, Object> kafka;

    public OrderStatusService(OrderRepository orders, KafkaTemplate<String, Object> kafka) {
        this.orders = orders;
        this.kafka = kafka;
    }

    @Transactional
    public void update(UUID orderId, String status, String detail) {
        orders.findById(orderId).ifPresent(order -> {
            order.updateStatus(status, detail);
            kafka.send(Topics.ORDER_STATUS, orderId.toString(),
                    new OrderStatusChanged(UUID.randomUUID(), orderId, order.getCustomerEmail(), status, detail, Instant.now()));
        });
    }
}
