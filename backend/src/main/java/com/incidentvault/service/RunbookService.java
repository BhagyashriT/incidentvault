package com.incidentvault.service;

import com.incidentvault.domain.Incident;
import com.incidentvault.domain.Runbook;
import com.incidentvault.domain.Step;
import com.incidentvault.repository.IncidentRepository;
import com.incidentvault.repository.RunbookRepository;
import com.incidentvault.web.dto.CreateRunbookRequest;
import com.incidentvault.web.dto.RunbookDetail;
import com.incidentvault.web.dto.RunbookSummary;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class RunbookService {

    private final RunbookRepository runbooks;
    private final IncidentRepository incidents;
    private final DatabaseDialect dialect;

    public RunbookService(RunbookRepository runbooks, IncidentRepository incidents, DatabaseDialect dialect) {
        this.runbooks = runbooks;
        this.incidents = incidents;
        this.dialect = dialect;
    }

    @Transactional(readOnly = true)
    public List<RunbookSummary> search(String q) {
        String normalized = SearchSupport.normalize(q);
        List<Runbook> found;
        if (normalized != null && dialect.isPostgres()) {
            found = runbooks.searchFullText(normalized);
        } else if (normalized != null) {
            // Portable fallback: steps are stored as a JSON TEXT column via an
            // AttributeConverter, so filter in memory instead of JPQL LIKE.
            String needle = normalized.toLowerCase(Locale.ROOT);
            found = runbooks.findAllByOrderByCreatedAtDesc().stream()
                    .filter(r -> containsIgnoreCase(r.getTitle(), needle) || stepsContain(r.getSteps(), needle))
                    .toList();
        } else {
            found = runbooks.findAllByOrderByCreatedAtDesc();
        }
        return found.stream().map(RunbookSummary::from).toList();
    }

    @Transactional
    public RunbookDetail create(CreateRunbookRequest request) {
        List<Step> steps = request.steps().stream()
                .map(s -> new Step(s.order(), s.title(), s.detail(), s.command()))
                .toList();
        Runbook runbook = new Runbook(request.title(), steps);
        if (request.linkedIncidentIds() != null) {
            for (Long incidentId : request.linkedIncidentIds()) {
                runbook.getLinkedIncidents().add(findIncidentOrThrow(incidentId));
            }
        }
        return RunbookDetail.from(runbooks.save(runbook));
    }

    @Transactional(readOnly = true)
    public RunbookDetail get(Long id) {
        return RunbookDetail.from(findOrThrow(id));
    }

    @Transactional
    public RunbookDetail linkIncident(Long runbookId, Long incidentId) {
        Runbook runbook = findOrThrow(runbookId);
        runbook.getLinkedIncidents().add(findIncidentOrThrow(incidentId));
        return RunbookDetail.from(runbooks.save(runbook));
    }

    private Runbook findOrThrow(Long id) {
        return runbooks.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Runbook not found: " + id));
    }

    private Incident findIncidentOrThrow(Long id) {
        return incidents.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Incident not found: " + id));
    }

    private static boolean containsIgnoreCase(String haystack, String needleLower) {
        return haystack != null && haystack.toLowerCase(Locale.ROOT).contains(needleLower);
    }

    private static boolean stepsContain(List<Step> steps, String needleLower) {
        if (steps == null) {
            return false;
        }
        return steps.stream().anyMatch(s ->
                containsIgnoreCase(s.title(), needleLower)
                        || containsIgnoreCase(s.detail(), needleLower)
                        || containsIgnoreCase(s.command(), needleLower));
    }
}
