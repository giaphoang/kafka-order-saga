package dev.kafka.orders.controller;

import dev.kafka.contracts.OrderCreated;
import dev.kafka.orders.model.Order;
import dev.kafka.orders.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;
    public OrderController(OrderService service) { this.service = service; }

    public record CreateOrderRequest(@Email String customerEmail, @NotEmpty List<OrderCreated.LineItem> items) {}
    public record OrderResponse(UUID id, String customerEmail, String total, String status, String detail) {
        static OrderResponse from(Order o) {
            return new OrderResponse(o.getId(), o.getCustomerEmail(), o.getTotal().toPlainString(), o.getStatus(), o.getDetail());
        }
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) throws Exception {
        Order order = service.create(request.customerEmail(), request.items());
        return ResponseEntity.created(URI.create("/api/orders/" + order.getId())).body(OrderResponse.from(order));
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return service.find(id).map(OrderResponse::from).orElseThrow();
    }
}
