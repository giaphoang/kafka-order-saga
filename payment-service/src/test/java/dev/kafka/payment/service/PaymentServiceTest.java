package dev.kafka.payment.service;

import dev.kafka.contracts.InventoryResult;
import dev.kafka.contracts.PaymentResult;
import dev.kafka.payment.messaging.PaymentEventPublisher;
import dev.kafka.payment.model.Payment;
import dev.kafka.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentServiceTest {
    private PaymentRepository payments;
    private PaymentEventPublisher publisher;
    private PaymentService service;

    @BeforeEach
    void setUp() {
        payments = mock(PaymentRepository.class);
        publisher = mock(PaymentEventPublisher.class);
        service = new PaymentService(payments, publisher);
    }

    private InventoryResult inventory(boolean reserved, String amount) {
        return new InventoryResult(UUID.randomUUID(), UUID.randomUUID(), reserved,
                new BigDecimal(amount), reserved ? null : "Insufficient stock", Instant.now());
    }

    @Test
    void unreservedInventoryStopsPayment() throws Exception {
        var event = inventory(false, "100.00");
        service.authorize(event);
        verify(payments, never()).save(any());
        verify(publisher, never()).publish(any());
    }

    @Test
    void duplicateInventoryResultIsIgnored() throws Exception {
        var event = inventory(true, "100.00");
        when(payments.existsBySourceEventId(event.eventId())).thenReturn(true);
        service.authorize(event);
        verify(payments, never()).save(any());
        verify(publisher, never()).publish(any());
    }

    @Test
    void amountWithinLimitIsApproved() throws Exception {
        var event = inventory(true, "1299.00");
        when(payments.existsBySourceEventId(event.eventId())).thenReturn(false);
        service.authorize(event);
        verify(payments).save(any(Payment.class));
        var captor = ArgumentCaptor.forClass(PaymentResult.class);
        verify(publisher).publish(captor.capture());
        assertTrue(captor.getValue().approved());
        assertNotNull(captor.getValue().transactionId());
        assertTrue(captor.getValue().transactionId().startsWith("PAY-"));
    }

    @Test
    void amountOverLimitIsDeclined() throws Exception {
        var event = inventory(true, "9999.00");
        when(payments.existsBySourceEventId(event.eventId())).thenReturn(false);
        service.authorize(event);
        var captor = ArgumentCaptor.forClass(PaymentResult.class);
        verify(publisher).publish(captor.capture());
        assertFalse(captor.getValue().approved());
        assertEquals("Demo gateway limit exceeded", captor.getValue().reason());
        assertNull(captor.getValue().transactionId());
    }
}
