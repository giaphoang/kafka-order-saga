package dev.kafka.orders.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "customer_order")
public class Order {
    @Id private UUID id;
    @Column(nullable = false) private String customerEmail;
    @Column(nullable = false) private BigDecimal total;
    @Column(nullable = false) private String status;
    private String detail;
    @Column(nullable = false) private Instant createdAt;

    protected Order() {}

    public Order(UUID id, String customerEmail, BigDecimal total) {
        this.id = id;
        this.customerEmail = customerEmail;
        this.total = total;
        this.status = "PENDING";
        this.createdAt = Instant.now();
    }

    public void updateStatus(String status, String detail) {
        this.status = status;
        this.detail = detail;
    }

    public UUID getId() { return id; }
    public String getCustomerEmail() { return customerEmail; }
    public BigDecimal getTotal() { return total; }
    public String getStatus() { return status; }
    public String getDetail() { return detail; }
    public Instant getCreatedAt() { return createdAt; }
}
