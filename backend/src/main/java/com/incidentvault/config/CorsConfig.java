package com.incidentvault.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the split deployment (Vercel SPA calling the Render API).
 *
 * <p>Reads the allowed origins from {@code CORS_ALLOWED_ORIGINS} (comma-separated).
 * Defaults to {@code *} so the public demo just works; set
 * {@code CORS_ALLOWED_ORIGINS=https://<your-vercel-app>.vercel.app} on Render to
 * lock it down. Local docker-compose development is unaffected: the nginx
 * frontend proxies {@code /api/} same-origin, so CORS never engages there.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public CorsConfig(@Value("${CORS_ALLOWED_ORIGINS:*}") String allowedOrigins) {
        this.allowedOrigins = allowedOrigins.split(",");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
