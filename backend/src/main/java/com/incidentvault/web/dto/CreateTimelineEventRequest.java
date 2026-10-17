package com.incidentvault.web.dto;

import com.incidentvault.domain.TimelineEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record CreateTimelineEventRequest(
        String actor,
        @NotNull TimelineEventType type,
        @NotBlank String message,
        Instant timestamp) {
}
