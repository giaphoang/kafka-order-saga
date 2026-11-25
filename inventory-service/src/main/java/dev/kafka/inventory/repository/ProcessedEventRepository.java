package dev.kafka.inventory.repository;
import dev.kafka.inventory.model.ProcessedEvent; import org.springframework.data.jpa.repository.JpaRepository; import java.util.UUID;
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {}
