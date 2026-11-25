package dev.kafka.orders.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/** Validates a Bearer JWT and populates the SecurityContext. Rejection (401) is
 *  left to the authorization rules in SecurityConfig when no authentication is set. */
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtTokenService tokens;
    public JwtAuthFilter(JwtTokenService tokens) { this.tokens = tokens; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            tokens.validate(header.substring(7)).ifPresent(subject ->
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(subject, null,
                                    List.of(new SimpleGrantedAuthority("ROLE_USER")))));
        }
        chain.doFilter(request, response);
    }
}
