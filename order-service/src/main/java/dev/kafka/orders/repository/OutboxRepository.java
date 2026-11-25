package dev.kafka.orders.repository;

import dev.kafka.orders.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop50ByPublishedAtIsNullOrderByCreatedAt();
}
