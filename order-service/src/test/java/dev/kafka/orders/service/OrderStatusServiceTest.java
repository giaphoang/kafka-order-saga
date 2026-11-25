package dev.kafka.orders.service;

import dev.kafka.contracts.OrderStatusChanged;
import dev.kafka.contracts.Topics;
import dev.kafka.orders.model.Order;
import dev.kafka.orders.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OrderStatusServiceTest {
    private OrderRepository orders;
    private KafkaTemplate<String, Object> kafka;
    private OrderStatusService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        orders = mock(OrderRepository.class);
        kafka = mock(KafkaTemplate.class);
        service = new OrderStatusService(orders, kafka);
    }

    @Test
    void updateChangesStatusAndPublishesStatusEvent() {
        UUID id = UUID.randomUUID();
        var order = new Order(id, "a@b.com", new BigDecimal("10.00"));
        when(orders.findById(id)).thenReturn(Optional.of(order));

        service.update(id, "CONFIRMED", "PAY-123");

        assertEquals("CONFIRMED", order.getStatus());
        assertEquals("PAY-123", order.getDetail());
        var captor = ArgumentCaptor.forClass(Object.class);
        verify(kafka).send(eq(Topics.ORDER_STATUS), eq(id.toString()), captor.capture());
        var event = (OrderStatusChanged) captor.getValue();
        assertEquals("CONFIRMED", event.status());
        assertEquals(id, event.orderId());
    }

    @Test
    void updateForUnknownOrderPublishesNothing() {
        UUID id = UUID.randomUUID();
        when(orders.findById(id)).thenReturn(Optional.empty());
        service.update(id, "REJECTED", "no stock");
        verify(kafka, never()).send(anyString(), anyString(), any());
    }
}
