package dev.kafka.orders.security;

import dev.kafka.orders.controller.AuthController;
import dev.kafka.orders.controller.OrderController;
import dev.kafka.orders.model.Order;
import dev.kafka.orders.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifies the authorization rules at the HTTP layer (no DB/Kafka needed). */
@WebMvcTest({OrderController.class, AuthController.class})
@Import({SecurityConfig.class, JwtTokenService.class})
class SecurityConfigTest {
    @Autowired MockMvc mvc;
    @Autowired JwtTokenService tokens;
    @MockitoBean OrderService orderService;

    private static final String ORDER_JSON = """
            {"customerEmail":"a@b.com","items":[{"sku":"LAPTOP-14","quantity":1,"unitPrice":1299.00}]}
            """;

    @Test
    void postOrderWithoutTokenIs401() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(ORDER_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postOrderWithValidTokenSucceeds() throws Exception {
        var order = new Order(UUID.randomUUID(), "a@b.com", new BigDecimal("1299.00"));
        when(orderService.create(any(), any())).thenReturn(order);
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(ORDER_JSON)
                        .header("Authorization", "Bearer " + tokens.createToken("demo")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getOrderIsPublic() throws Exception {
        UUID id = UUID.randomUUID();
        when(orderService.find(id)).thenReturn(Optional.of(new Order(id, "a@b.com", new BigDecimal("1.00"))));
        mvc.perform(get("/api/orders/" + id)).andExpect(status().isOk());
    }

    @Test
    void tokenEndpointIssuesTokenForDemoUser() throws Exception {
        mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"demo-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    void tokenEndpointRejectsBadPassword() throws Exception {
        mvc.perform(post("/api/auth/token").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"demo\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postOrderWithGarbageTokenIs401() throws Exception {
        mvc.perform(post("/api/orders").contentType(MediaType.APPLICATION_JSON).content(ORDER_JSON)
                        .header("Authorization", "Bearer garbage"))
                .andExpect(status().isUnauthorized());
    }
}
