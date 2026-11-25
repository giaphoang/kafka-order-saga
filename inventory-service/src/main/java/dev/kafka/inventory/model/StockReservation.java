package dev.kafka.inventory.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "stock_reservation")
public class StockReservation {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private UUID orderId;
    @Column(nullable = false) private String sku;
    @Column(nullable = false) private int quantity;
    @Column(nullable = false) private boolean released;

    protected StockReservation() {}
    public StockReservation(UUID orderId, String sku, int quantity) { this.orderId = orderId; this.sku = sku; this.quantity = quantity; }
    public void markReleased() { released = true; }
    public String getSku() { return sku; }
    public int getQuantity() { return quantity; }
}
