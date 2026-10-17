package com.incidentvault.web.dto;

import com.incidentvault.domain.Incident;
import com.incidentvault.domain.IncidentStatus;
import com.incidentvault.domain.Severity;

import java.time.Instant;

public record IncidentListItem(
        Long id,
        String title,
        Severity severity,
        IncidentStatus status,
        String service,
        Instant startedAt,
        Instant resolvedAt,
        Instant createdAt,
        Instant updatedAt) {

    public static IncidentListItem from(Incident i) {
        return new IncidentListItem(
                i.getId(), i.getTitle(), i.getSeverity(), i.getStatus(), i.getService(),
                i.getStartedAt(), i.getResolvedAt(), i.getCreatedAt(), i.getUpdatedAt());
    }
}
