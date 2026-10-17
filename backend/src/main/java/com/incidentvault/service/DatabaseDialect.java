package com.incidentvault.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Detects which database dialect the app is running against so search can pick
 * the right strategy: native tsvector queries on PostgreSQL, portable JPQL on H2.
 */
@Component
public class DatabaseDialect {

    private final String jdbcUrl;

    public DatabaseDialect(@Value("${spring.datasource.url:}") String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public boolean isPostgres() {
        return jdbcUrl != null && jdbcUrl.contains("postgresql");
    }
}
