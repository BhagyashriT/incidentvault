package com.incidentvault.web.dto;

import com.incidentvault.domain.Runbook;
import com.incidentvault.domain.Step;

import java.time.Instant;
import java.util.List;

public record RunbookDetail(
        Long id,
        String title,
        List<Step> steps,
        List<LinkedIncident> linkedIncidents,
        Instant createdAt,
        Instant updatedAt) {

    public record LinkedIncident(Long id, String title) {
    }

    public static RunbookDetail from(Runbook r) {
        List<LinkedIncident> linked = r.getLinkedIncidents().stream()
                .map(i -> new LinkedIncident(i.getId(), i.getTitle()))
                .sorted((a, b) -> Long.compare(a.id(), b.id()))
                .toList();
        return new RunbookDetail(
                r.getId(), r.getTitle(), List.copyOf(r.getSteps()), linked,
                r.getCreatedAt(), r.getUpdatedAt());
    }
}
