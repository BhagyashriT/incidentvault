package com.incidentvault.web.dto;

import com.incidentvault.domain.RootCause;

import java.time.Instant;

public record RootCauseDto(
        Long id,
        String hypothesis,
        boolean validated,
        String evidence,
        Instant createdAt) {

    public static RootCauseDto from(RootCause r) {
        return new RootCauseDto(r.getId(), r.getHypothesis(), r.isValidated(), r.getEvidence(), r.getCreatedAt());
    }
}
