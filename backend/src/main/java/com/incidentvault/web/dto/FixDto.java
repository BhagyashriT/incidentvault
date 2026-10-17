package com.incidentvault.web.dto;

import com.incidentvault.domain.Fix;
import com.incidentvault.domain.IncidentStatus;
import com.incidentvault.domain.Severity;

import java.time.Instant;

public record FixDto(
        Long id,
        String description,
        String prUrl,
        String commitSha,
        Instant createdAt) {

    public static FixDto from(Fix f) {
        return new FixDto(f.getId(), f.getDescription(), f.getPrUrl(), f.getCommitSha(), f.getCreatedAt());
    }
}
