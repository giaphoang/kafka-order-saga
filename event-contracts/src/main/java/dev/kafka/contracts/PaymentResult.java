package dev.kafka.contracts;

import java.time.Instant;
import java.util.UUID;

public record PaymentResult(UUID eventId, UUID orderId, boolean approved, String transactionId,
                            String reason, Instant occurredAt) {}
