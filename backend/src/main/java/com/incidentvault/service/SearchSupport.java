package com.incidentvault.service;

/**
 * Normalizes raw search input. Kept static and dependency-free so it is trivially unit-testable.
 */
public final class SearchSupport {

    private SearchSupport() {
    }

    /**
     * Trims the query; returns {@code null} when there is nothing to search for,
     * so callers can skip the text predicate entirely.
     */
    public static String normalize(String q) {
        if (q == null || q.isBlank()) {
            return null;
        }
        return q.trim();
    }
}
