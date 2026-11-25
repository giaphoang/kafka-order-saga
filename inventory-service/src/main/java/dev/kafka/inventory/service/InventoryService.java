package dev.kafka.inventory.service;

import dev.kafka.contracts.*;
import dev.kafka.inventory.messaging.InventoryEventPublisher;
import dev.kafka.inventory.model.*;
import dev.kafka.inventory.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
public class InventoryService {
    private final StockRepository stock;
    private final ProcessedEventRepository processed;
    private final ReservationRepository reservations;
    private final InventoryEventPublisher publisher;

    public InventoryService(StockRepository stock, ProcessedEventRepository processed,
                            ReservationRepository reservations, InventoryEventPublisher publisher) {
        this.stock = stock; this.processed = processed; this.reservations = reservations; this.publisher = publisher;
    }

    @Transactional
    public void reserve(OrderCreated event) throws Exception {
        if (processed.existsById(event.eventId())) return;
        Map<String, StockItem> locked = new LinkedHashMap<>();
        String failure = null;
        for (var line : event.items()) {
            StockItem item = stock.lock(line.sku()).orElse(null);
            locked.put(line.sku(), item);
            if (item == null || !item.canReserve(line.quantity())) {
                failure = "Insufficient stock for " + line.sku();
                break;
            }
        }
        boolean reserved = failure == null;
        if (reserved) {
            event.items().forEach(line -> {
                locked.get(line.sku()).reserve(line.quantity());
                reservations.save(new StockReservation(event.orderId(), line.sku(), line.quantity()));
            });
        }
        UUID resultId = UUID.nameUUIDFromBytes(("inventory:" + event.eventId()).getBytes(StandardCharsets.UTF_8));
        publisher.publish(new InventoryResult(resultId, event.orderId(), reserved, event.total(), failure, Instant.now()));
        processed.save(new ProcessedEvent(event.eventId()));
    }

    @Transactional
    public void releaseAfterDecline(PaymentResult event) {
        if (event.approved() || processed.existsById(event.eventId())) return;
        for (var reservation : reservations.findByOrderIdAndReleasedFalse(event.orderId())) {
            stock.lock(reservation.getSku()).ifPresent(item -> item.release(reservation.getQuantity()));
            reservation.markReleased();
        }
        processed.save(new ProcessedEvent(event.eventId()));
    }
}
