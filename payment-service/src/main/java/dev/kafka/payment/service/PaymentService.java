package dev.kafka.payment.service;

import dev.kafka.contracts.*;
import dev.kafka.payment.messaging.PaymentEventPublisher;
import dev.kafka.payment.model.Payment;
import dev.kafka.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Service
public class PaymentService {
    private static final BigDecimal DEMO_GATEWAY_LIMIT = new BigDecimal("5000.00");
    private final PaymentRepository payments;
    private final PaymentEventPublisher publisher;

    public PaymentService(PaymentRepository payments, PaymentEventPublisher publisher) {
        this.payments = payments; this.publisher = publisher;
    }

    @Transactional
    public void authorize(InventoryResult inventory) throws Exception {
        if (!inventory.reserved() || payments.existsBySourceEventId(inventory.eventId())) return;
        boolean approved = inventory.amount().compareTo(DEMO_GATEWAY_LIMIT) <= 0;
        UUID resultId = UUID.nameUUIDFromBytes(("payment:" + inventory.eventId()).getBytes(StandardCharsets.UTF_8));
        String transactionId = approved ? "PAY-" + resultId : null;
        payments.save(new Payment(resultId, inventory.eventId(), inventory.orderId(), approved, transactionId));
        publisher.publish(new PaymentResult(resultId, inventory.orderId(), approved, transactionId,
                approved ? null : "Demo gateway limit exceeded", Instant.now()));
    }
}
