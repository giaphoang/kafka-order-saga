package dev.kafka.inventory.model;

import jakarta.persistence.*;

@Entity
@Table(name = "stock_item")
public class StockItem {
    @Id private String sku;
    @Column(nullable = false) private int available;

    protected StockItem() {}
    public StockItem(String sku, int available) { this.sku = sku; this.available = available; }

    public boolean canReserve(int quantity) { return available >= quantity; }
    public void reserve(int quantity) { available -= quantity; }
    public void release(int quantity) { available += quantity; }
    public String getSku() { return sku; }
    public int getAvailable() { return available; }
}
