package dev.kafka.inventory.repository;

import dev.kafka.inventory.model.StockItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface StockRepository extends JpaRepository<StockItem, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from StockItem s where s.sku = :sku")
    Optional<StockItem> lock(@Param("sku") String sku);
}
