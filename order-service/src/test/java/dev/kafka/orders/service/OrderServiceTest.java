package dev.kafka.orders.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.kafka.contracts.OrderCreated;
import dev.kafka.contracts.Topics;
import dev.kafka.orders.model.Order;
import dev.kafka.orders.model.OutboxEvent;
import dev.kafka.orders.repository.OrderRepository;
import dev.kafka.orders.repository.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {
    private OrderRepository orders;
    private OutboxRepository outbox;
    private OrderService service;
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        orders = mock(OrderRepository.class);
        outbox = mock(OutboxRepository.class);
        when(orders.save(any())).thenAnswer(i -> i.getArgument(0));
        service = new OrderService(orders, outbox, json);
    }

    @Test
    void createComputesTotalStartsPendingAndWritesOutbox() throws Exception {
        var items = List.of(
                new OrderCreated.LineItem("LAPTOP-14", 1, new BigDecimal("1299.00")),
                new OrderCreated.LineItem("USB-C-DOCK", 2, new BigDecimal("100.50")));
        Order order = service.create("a@b.com", items);

        assertEquals(new BigDecimal("1500.00"), order.getTotal());
        assertEquals("PENDING", order.getStatus());
        var captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outbox).save(captor.capture());
        assertEquals(Topics.ORDER_CREATED, captor.getValue().getTopic());
        assertEquals(order.getId(), captor.getValue().getAggregateId());
        OrderCreated payload = json.readValue(captor.getValue().getPayload(), OrderCreated.class);
        assertEquals(order.getId(), payload.orderId());
        assertEquals(new BigDecimal("1500.00"), payload.total());
    }

    @Test
    void createRejectsEmptyItems() {
        assertThrows(IllegalArgumentException.class, () -> service.create("a@b.com", List.of()));
        verify(outbox, never()).save(any());
    }

    @Test
    void createRejectsZeroQuantity() {
        var items = List.of(new OrderCreated.LineItem("LAPTOP-14", 0, new BigDecimal("1.00")));
        assertThrows(IllegalArgumentException.class, () -> service.create("a@b.com", items));
        verify(orders, never()).save(any());
    }
}
