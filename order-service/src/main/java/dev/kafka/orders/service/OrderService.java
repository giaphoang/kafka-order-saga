package dev.kafka.orders.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.kafka.contracts.OrderCreated;
import dev.kafka.contracts.Topics;
import dev.kafka.orders.model.Order;
import dev.kafka.orders.model.OutboxEvent;
import dev.kafka.orders.repository.OrderRepository;
import dev.kafka.orders.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final OutboxRepository outbox;
    private final ObjectMapper json;

    public OrderService(OrderRepository orders, OutboxRepository outbox, ObjectMapper json) {
        this.orders = orders;
        this.outbox = outbox;
        this.json = json;
    }

    @Transactional
    public Order create(String email, List<OrderCreated.LineItem> items) throws JsonProcessingException {
        if (items == null || items.isEmpty() || items.stream().anyMatch(i -> i.quantity() < 1 || i.unitPrice().signum() < 0)) {
            throw new IllegalArgumentException("Invalid order items");
        }
        UUID orderId = UUID.randomUUID();
        BigDecimal total = items.stream()
                .map(i -> i.unitPrice().multiply(BigDecimal.valueOf(i.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Order order = orders.save(new Order(orderId, email, total));
        OrderCreated event = new OrderCreated(UUID.randomUUID(), orderId, email, List.copyOf(items), total, Instant.now());
        outbox.save(new OutboxEvent(event.eventId(), orderId, Topics.ORDER_CREATED, json.writeValueAsString(event)));
        return order;
    }

    public Optional<Order> find(UUID id) { return orders.findById(id); }
}
