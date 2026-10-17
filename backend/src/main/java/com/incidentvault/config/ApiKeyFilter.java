package com.incidentvault.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Placeholder API-key authentication.
 *
 * <p>Reads the {@code X-API-Key} header and compares it against
 * {@code incidentvault.api-key} (env {@code API_KEY}). When the property is empty
 * (the default), the filter is a no-op and every request is allowed through.
 * This is intentionally minimal: a real deployment would replace it with proper
 * authentication (OAuth2/JWT, rotated secrets, per-key scopes).
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private final String apiKey;

    public ApiKeyFilter(@Value("${incidentvault.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (apiKey != null && !apiKey.isBlank()) {
            String provided = request.getHeader("X-API-Key");
            if (!apiKey.equals(provided)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.getWriter().write("{\"error\":\"Invalid or missing API key\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
