package com.incidentvault.web.dto;

/** JSON error body, e.g. {"error": "Incident not found: 42"}. */
public record ApiError(String error) {
}
