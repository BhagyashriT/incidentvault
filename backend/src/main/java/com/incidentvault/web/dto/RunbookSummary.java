package com.incidentvault.web.dto;

import com.incidentvault.domain.Runbook;

import java.util.List;

public record RunbookSummary(
        Long id,
        String title,
        List<Long> linkedIncidentIds) {

    public static RunbookSummary from(Runbook r) {
        List<Long> ids = r.getLinkedIncidents().stream()
                .map(i -> i.getId())
                .sorted()
                .toList();
        return new RunbookSummary(r.getId(), r.getTitle(), ids);
    }
}
