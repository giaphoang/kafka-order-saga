package dev.kafka.contracts;

import java.time.Instant;
import java.math.BigDecimal;
import java.util.UUID;

public record InventoryResult(UUID eventId, UUID orderId, boolean reserved, BigDecimal amount,
                              String reason, Instant occurredAt) {}
