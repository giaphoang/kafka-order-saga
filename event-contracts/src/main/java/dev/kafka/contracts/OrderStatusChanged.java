package dev.kafka.contracts;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusChanged(UUID eventId, UUID orderId, String customerEmail,
                                 String status, String detail, Instant occurredAt) {}
