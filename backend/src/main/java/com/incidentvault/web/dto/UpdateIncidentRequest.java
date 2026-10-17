package com.incidentvault.web.dto;

import com.incidentvault.domain.IncidentStatus;
import com.incidentvault.domain.Severity;

import java.time.Instant;

public record UpdateIncidentRequest(
        IncidentStatus status,
        Severity severity,
        String summary,
        Instant resolvedAt) {
}
