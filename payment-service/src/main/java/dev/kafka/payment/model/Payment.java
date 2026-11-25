package dev.kafka.payment.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment", uniqueConstraints = @UniqueConstraint(columnNames = "sourceEventId"))
public class Payment {
    @Id private UUID id;
    @Column(nullable = false) private UUID sourceEventId;
    @Column(nullable = false) private UUID orderId;
    @Column(nullable = false) private boolean approved;
    private String transactionId;
    @Column(nullable = false) private Instant createdAt;

    protected Payment() {}
    public Payment(UUID id, UUID sourceEventId, UUID orderId, boolean approved, String transactionId) {
        this.id = id; this.sourceEventId = sourceEventId; this.orderId = orderId;
        this.approved = approved; this.transactionId = transactionId; this.createdAt = Instant.now();
    }
}
