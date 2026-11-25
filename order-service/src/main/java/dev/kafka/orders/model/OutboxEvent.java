package dev.kafka.orders.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
public class OutboxEvent {
    @Id private UUID id;
    @Column(nullable = false) private UUID aggregateId;
    @Column(nullable = false) private String topic;
    @Column(nullable = false, columnDefinition = "text") private String payload;
    @Column(nullable = false) private Instant createdAt;
    private Instant publishedAt;

    protected OutboxEvent() {}

    public OutboxEvent(UUID id, UUID aggregateId, String topic, String payload) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.topic = topic;
        this.payload = payload;
        this.createdAt = Instant.now();
    }

    public void markPublished() { this.publishedAt = Instant.now(); }
    public UUID getAggregateId() { return aggregateId; }
    public String getTopic() { return topic; }
    public String getPayload() { return payload; }
}
