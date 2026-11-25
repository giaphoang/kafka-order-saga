package dev.kafka.orders.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {
    private final JwtTokenService tokens = new JwtTokenService("test-secret-for-unit-tests-only", 3600);

    JwtTokenServiceTest() throws Exception {}

    @Test
    void validTokenRoundTripsSubject() {
        String token = tokens.createToken("demo");
        assertEquals("demo", tokens.validate(token).orElseThrow());
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = tokens.createToken("demo");
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertTrue(tokens.validate(tampered).isEmpty());
    }

    @Test
    void garbageTokenIsRejected() {
        assertTrue(tokens.validate("not-a-jwt").isEmpty());
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        var shortLived = new JwtTokenService("test-secret-for-unit-tests-only", -10);
        assertTrue(shortLived.validate(shortLived.createToken("demo")).isEmpty());
    }
}
