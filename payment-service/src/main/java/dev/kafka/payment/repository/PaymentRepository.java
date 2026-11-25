package dev.kafka.payment.repository;

import dev.kafka.payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    boolean existsBySourceEventId(UUID sourceEventId);
}
