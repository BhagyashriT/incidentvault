package com.incidentvault.service;

import com.incidentvault.domain.Fix;
import com.incidentvault.domain.Incident;
import com.incidentvault.domain.IncidentStatus;
import com.incidentvault.domain.LogSnippet;
import com.incidentvault.domain.RootCause;
import com.incidentvault.domain.Severity;
import com.incidentvault.domain.TimelineEvent;
import com.incidentvault.repository.FixRepository;
import com.incidentvault.repository.IncidentRepository;
import com.incidentvault.repository.LogSnippetRepository;
import com.incidentvault.repository.RootCauseRepository;
import com.incidentvault.repository.RunbookRepository;
import com.incidentvault.repository.TimelineEventRepository;
import com.incidentvault.web.dto.CreateFixRequest;
import com.incidentvault.web.dto.CreateIncidentRequest;
import com.incidentvault.web.dto.CreateLogSnippetRequest;
import com.incidentvault.web.dto.CreateRootCauseRequest;
import com.incidentvault.web.dto.CreateTimelineEventRequest;
import com.incidentvault.web.dto.FixDto;
import com.incidentvault.web.dto.IncidentDetail;
import com.incidentvault.web.dto.IncidentListItem;
import com.incidentvault.web.dto.LogSnippetDto;
import com.incidentvault.web.dto.RootCauseDto;
import com.incidentvault.web.dto.TimelineEventDto;
import com.incidentvault.web.dto.UpdateIncidentRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IncidentService {

    private final IncidentRepository incidents;
    private final TimelineEventRepository timelineEvents;
    private final LogSnippetRepository logSnippets;
    private final RootCauseRepository rootCauses;
    private final FixRepository fixes;
    private final RunbookRepository runbooks;
    private final DatabaseDialect dialect;

    public IncidentService(IncidentRepository incidents,
                           TimelineEventRepository timelineEvents,
                           LogSnippetRepository logSnippets,
                           RootCauseRepository rootCauses,
                           FixRepository fixes,
                           RunbookRepository runbooks,
                           DatabaseDialect dialect) {
        this.incidents = incidents;
        this.timelineEvents = timelineEvents;
        this.logSnippets = logSnippets;
        this.rootCauses = rootCauses;
        this.fixes = fixes;
        this.runbooks = runbooks;
        this.dialect = dialect;
    }

    @Transactional(readOnly = true)
    public List<IncidentListItem> search(String q, Severity severity, String service, IncidentStatus status) {
        String normalized = SearchSupport.normalize(q);
        List<Incident> found;
        if (normalized != null && dialect.isPostgres()) {
            found = incidents.searchFullText(
                    normalized,
                    severity == null ? null : severity.name(),
                    service,
                    status == null ? null : status.name());
        } else {
            found = incidents.searchPortable(normalized, severity, service, status);
        }
        return found.stream().map(IncidentListItem::from).toList();
    }

    @Transactional
    public IncidentListItem create(CreateIncidentRequest request) {
        Incident incident = new Incident(
                request.title(), request.severity(), request.status(),
                request.service(), request.startedAt(), request.summary());
        return IncidentListItem.from(incidents.save(incident));
    }

    @Transactional(readOnly = true)
    public IncidentDetail getDetail(Long id) {
        Incident incident = findOrThrow(id);
        return new IncidentDetail(
                incident.getId(), incident.getTitle(), incident.getSeverity(), incident.getStatus(),
                incident.getService(), incident.getStartedAt(), incident.getResolvedAt(),
                incident.getSummary(), incident.getCreatedAt(), incident.getUpdatedAt(),
                timelineEvents.findByIncidentIdOrderByTimestampAsc(id).stream()
                        .map(TimelineEventDto::from).toList(),
                logSnippets.findByIncidentIdOrderByCapturedAtAscIdAsc(id).stream()
                        .map(LogSnippetDto::from).toList(),
                rootCauses.findByIncidentIdOrderByIdAsc(id).stream()
                        .map(RootCauseDto::from).toList(),
                fixes.findByIncidentIdOrderByIdAsc(id).stream()
                        .map(FixDto::from).toList(),
                runbooks.findByLinkedIncidentsId(id).stream()
                        .map(IncidentDetail.LinkedRunbook::from).toList());
    }

    @Transactional
    public IncidentListItem update(Long id, UpdateIncidentRequest request) {
        Incident incident = findOrThrow(id);
        if (request.status() != null) {
            incident.setStatus(request.status());
        }
        if (request.severity() != null) {
            incident.setSeverity(request.severity());
        }
        if (request.summary() != null) {
            incident.setSummary(request.summary());
        }
        if (request.resolvedAt() != null) {
            incident.setResolvedAt(request.resolvedAt());
        }
        return IncidentListItem.from(incidents.save(incident));
    }

    @Transactional
    public TimelineEventDto addTimelineEvent(Long id, CreateTimelineEventRequest request) {
        Incident incident = findOrThrow(id);
        TimelineEvent event = new TimelineEvent(
                incident, request.timestamp(), request.actor(), request.type(), request.message());
        return TimelineEventDto.from(timelineEvents.save(event));
    }

    @Transactional(readOnly = true)
    public List<TimelineEventDto> getTimeline(Long id) {
        findOrThrow(id);
        return timelineEvents.findByIncidentIdOrderByTimestampAsc(id).stream()
                .map(TimelineEventDto::from).toList();
    }

    @Transactional
    public LogSnippetDto addLogSnippet(Long id, CreateLogSnippetRequest request) {
        Incident incident = findOrThrow(id);
        LogSnippet snippet = new LogSnippet(incident, request.source(), request.snippet(), null);
        return LogSnippetDto.from(logSnippets.save(snippet));
    }

    @Transactional
    public RootCauseDto addRootCause(Long id, CreateRootCauseRequest request) {
        Incident incident = findOrThrow(id);
        boolean validated = request.validated() != null && request.validated();
        RootCause rootCause = new RootCause(incident, request.hypothesis(), validated, request.evidence());
        return RootCauseDto.from(rootCauses.save(rootCause));
    }

    @Transactional
    public FixDto addFix(Long id, CreateFixRequest request) {
        Incident incident = findOrThrow(id);
        Fix fix = new Fix(incident, request.description(), request.prUrl(), request.commitSha());
        return FixDto.from(fixes.save(fix));
    }

    private Incident findOrThrow(Long id) {
        return incidents.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Incident not found: " + id));
    }
}
