package com.incidentvault.web.dto;

import com.incidentvault.domain.IncidentStatus;
import com.incidentvault.domain.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateIncidentRequest(
        @NotBlank String title,
        @NotNull Severity severity,
        IncidentStatus status,
        @NotBlank String service,
        Instant startedAt,
        String summary) {
}
