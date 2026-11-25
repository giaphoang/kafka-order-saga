package dev.kafka.inventory.repository;
import dev.kafka.inventory.model.StockReservation; import org.springframework.data.jpa.repository.JpaRepository; import java.util.*;
public interface ReservationRepository extends JpaRepository<StockReservation, UUID> {
    List<StockReservation> findByOrderIdAndReleasedFalse(UUID orderId);
}
