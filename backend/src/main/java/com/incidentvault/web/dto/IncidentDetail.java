package com.incidentvault.web.dto;

import com.incidentvault.domain.IncidentStatus;
import com.incidentvault.domain.Severity;

import java.time.Instant;
import java.util.List;

public record IncidentDetail(
        Long id,
        String title,
        Severity severity,
        IncidentStatus status,
        String service,
        Instant startedAt,
        Instant resolvedAt,
        String summary,
        Instant createdAt,
        Instant updatedAt,
        List<TimelineEventDto> timeline,
        List<LogSnippetDto> logSnippets,
        List<RootCauseDto> rootCauses,
        List<FixDto> fixes,
        List<LinkedRunbook> runbooks) {

    public record LinkedRunbook(Long id, String title) {
        public static LinkedRunbook from(com.incidentvault.domain.Runbook r) {
            return new LinkedRunbook(r.getId(), r.getTitle());
        }
    }
}
