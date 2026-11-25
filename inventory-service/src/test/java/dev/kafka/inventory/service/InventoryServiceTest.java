package dev.kafka.inventory.service;

import dev.kafka.contracts.InventoryResult;
import dev.kafka.contracts.OrderCreated;
import dev.kafka.contracts.PaymentResult;
import dev.kafka.inventory.messaging.InventoryEventPublisher;
import dev.kafka.inventory.model.StockItem;
import dev.kafka.inventory.model.StockReservation;
import dev.kafka.inventory.repository.ProcessedEventRepository;
import dev.kafka.inventory.repository.ReservationRepository;
import dev.kafka.inventory.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryServiceTest {
    private StockRepository stock;
    private ProcessedEventRepository processed;
    private ReservationRepository reservations;
    private InventoryEventPublisher publisher;
    private InventoryService service;

    @BeforeEach
    void setUp() {
        stock = mock(StockRepository.class);
        processed = mock(ProcessedEventRepository.class);
        reservations = mock(ReservationRepository.class);
        publisher = mock(InventoryEventPublisher.class);
        service = new InventoryService(stock, processed, reservations, publisher);
    }

    private OrderCreated order(UUID eventId, int qty) {
        return new OrderCreated(eventId, UUID.randomUUID(), "a@b.com",
                List.of(new OrderCreated.LineItem("LAPTOP-14", qty, new BigDecimal("1299.00"))),
                new BigDecimal("1299.00").multiply(BigDecimal.valueOf(qty)), Instant.now());
    }

    @Test
    void insufficientStockPublishesRejectionAndSavesNoReservation() throws Exception {
        var event = order(UUID.randomUUID(), 999);
        when(processed.existsById(event.eventId())).thenReturn(false);
        when(stock.lock("LAPTOP-14")).thenReturn(Optional.of(new StockItem("LAPTOP-14", 20)));

        service.reserve(event);

        var captor = ArgumentCaptor.forClass(InventoryResult.class);
        verify(publisher).publish(captor.capture());
        assertFalse(captor.getValue().reserved());
        assertTrue(captor.getValue().reason().contains("LAPTOP-14"));
        verify(reservations, never()).save(any());
    }

    @Test
    void sufficientStockReservesAndDecrements() throws Exception {
        var event = order(UUID.randomUUID(), 2);
        var item = new StockItem("LAPTOP-14", 20);
        when(processed.existsById(event.eventId())).thenReturn(false);
        when(stock.lock("LAPTOP-14")).thenReturn(Optional.of(item));

        service.reserve(event);

        assertEquals(18, item.getAvailable());
        verify(reservations).save(any(StockReservation.class));
        var captor = ArgumentCaptor.forClass(InventoryResult.class);
        verify(publisher).publish(captor.capture());
        assertTrue(captor.getValue().reserved());
        verify(processed).save(any());
    }

    @Test
    void duplicateDeliveryIsIgnored() throws Exception {
        var event = order(UUID.randomUUID(), 1);
        when(processed.existsById(event.eventId())).thenReturn(true);

        service.reserve(event);

        verify(stock, never()).lock(anyString());
        verify(publisher, never()).publish(any());
        verify(reservations, never()).save(any());
    }

    @Test
    void declinedPaymentReleasesReservedStock() {
        UUID orderId = UUID.randomUUID();
        var item = new StockItem("LAPTOP-14", 18);
        var reservation = new StockReservation(orderId, "LAPTOP-14", 2);
        var event = new PaymentResult(UUID.randomUUID(), orderId, false, null, "declined", Instant.now());
        when(processed.existsById(event.eventId())).thenReturn(false);
        when(reservations.findByOrderIdAndReleasedFalse(orderId)).thenReturn(List.of(reservation));
        when(stock.lock("LAPTOP-14")).thenReturn(Optional.of(item));

        service.releaseAfterDecline(event);

        assertEquals(20, item.getAvailable());
        verify(processed).save(any());
    }

    @Test
    void approvedPaymentDoesNotReleaseStock() {
        var event = new PaymentResult(UUID.randomUUID(), UUID.randomUUID(), true, "PAY-1", null, Instant.now());
        service.releaseAfterDecline(event);
        verify(reservations, never()).findByOrderIdAndReleasedFalse(any());
        verify(processed, never()).save(any());
    }

    @Test
    void duplicateDeclineIsIgnored() {
        var event = new PaymentResult(UUID.randomUUID(), UUID.randomUUID(), false, null, "declined", Instant.now());
        when(processed.existsById(event.eventId())).thenReturn(true);
        service.releaseAfterDecline(event);
        verify(reservations, never()).findByOrderIdAndReleasedFalse(any());
    }
}
