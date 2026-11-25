package dev.kafka.orders.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dev.kafka.contracts.OrderCreated;
import dev.kafka.contracts.Topics;
import dev.kafka.orders.model.OutboxEvent;
import dev.kafka.orders.repository.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutboxPublisherTest {
    private OutboxRepository repository;
    private KafkaTemplate<String, Object> kafka;
    private OutboxPublisher publisher;
    private final ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        repository = mock(OutboxRepository.class);
        kafka = mock(KafkaTemplate.class);
        when(kafka.send(anyString(), anyString(), any())).thenReturn(CompletableFuture.completedFuture(null));
        publisher = new OutboxPublisher(repository, kafka, json);
    }

    @Test
    void publishSendsUnpublishedRowsAndMarksThemPublished() throws Exception {
        UUID orderId = UUID.randomUUID();
        var event = new OrderCreated(UUID.randomUUID(), orderId, "a@b.com",
                List.of(new OrderCreated.LineItem("LAPTOP-14", 1, new BigDecimal("1299.00"))),
                new BigDecimal("1299.00"), Instant.now());
        var row = spy(new OutboxEvent(event.eventId(), orderId, Topics.ORDER_CREATED, json.writeValueAsString(event)));
        when(repository.findTop50ByPublishedAtIsNullOrderByCreatedAt()).thenReturn(List.of(row));

        publisher.publish();

        verify(kafka).send(eq(Topics.ORDER_CREATED), eq(orderId.toString()), any(OrderCreated.class));
        verify(row).markPublished();
    }

    @Test
    void publishWithNoRowsSendsNothing() throws Exception {
        when(repository.findTop50ByPublishedAtIsNullOrderByCreatedAt()).thenReturn(List.of());
        publisher.publish();
        verify(kafka, never()).send(anyString(), anyString(), any());
    }
}
