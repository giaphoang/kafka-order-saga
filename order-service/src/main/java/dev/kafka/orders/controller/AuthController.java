package dev.kafka.orders.controller;

import dev.kafka.orders.security.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** Demo token endpoint. Credentials come from DEMO_USERNAME / DEMO_PASSWORD env vars. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final JwtTokenService tokens;
    private final String demoUser;
    private final String demoPassword;

    public AuthController(JwtTokenService tokens,
                          @Value("${app.demo.username}") String demoUser,
                          @Value("${app.demo.password}") String demoPassword) {
        this.tokens = tokens;
        this.demoUser = demoUser;
        this.demoPassword = demoPassword;
    }

    public record TokenRequest(String username, String password) {}
    public record TokenResponse(String token) {}

    @PostMapping("/token")
    public TokenResponse token(@RequestBody TokenRequest request) {
        boolean ok = MessageDigest.isEqual(
                (demoUser + ":" + demoPassword).getBytes(StandardCharsets.UTF_8),
                ((request.username() == null ? "" : request.username()) + ":" +
                 (request.password() == null ? "" : request.password())).getBytes(StandardCharsets.UTF_8));
        if (!ok) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        return new TokenResponse(tokens.createToken(demoUser));
    }
}
