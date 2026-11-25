package dev.kafka.contracts;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderCreated(UUID eventId, UUID orderId, String customerEmail,
                           List<LineItem> items, BigDecimal total, Instant occurredAt) {
    public record LineItem(String sku, int quantity, BigDecimal unitPrice) {}
}
