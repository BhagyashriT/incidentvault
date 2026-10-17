package com.incidentvault.web;

import com.incidentvault.domain.IncidentStatus;
import com.incidentvault.domain.Severity;
import com.incidentvault.service.IncidentService;
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
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidents;

    public IncidentController(IncidentService incidents) {
        this.incidents = incidents;
    }

    @GetMapping
    public List<IncidentListItem> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String status) {
        return incidents.search(q, parseSeverity(severity), trimToNull(service), parseStatus(status));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidentListItem create(@Valid @RequestBody CreateIncidentRequest request) {
        return incidents.create(request);
    }

    @GetMapping("/{id}")
    public IncidentDetail get(@PathVariable Long id) {
        return incidents.getDetail(id);
    }

    @PatchMapping("/{id}")
    public IncidentListItem update(@PathVariable Long id,
                                   @Valid @RequestBody UpdateIncidentRequest request) {
        return incidents.update(id, request);
    }

    @PostMapping("/{id}/timeline")
    @ResponseStatus(HttpStatus.CREATED)
    public TimelineEventDto addTimelineEvent(@PathVariable Long id,
                                             @Valid @RequestBody CreateTimelineEventRequest request) {
        return incidents.addTimelineEvent(id, request);
    }

    @GetMapping("/{id}/timeline")
    public List<TimelineEventDto> getTimeline(@PathVariable Long id) {
        return incidents.getTimeline(id);
    }

    @PostMapping("/{id}/logs")
    @ResponseStatus(HttpStatus.CREATED)
    public LogSnippetDto addLogSnippet(@PathVariable Long id,
                                       @Valid @RequestBody CreateLogSnippetRequest request) {
        return incidents.addLogSnippet(id, request);
    }

    @PostMapping("/{id}/root-causes")
    @ResponseStatus(HttpStatus.CREATED)
    public RootCauseDto addRootCause(@PathVariable Long id,
                                     @Valid @RequestBody CreateRootCauseRequest request) {
        return incidents.addRootCause(id, request);
    }

    @PostMapping("/{id}/fixes")
    @ResponseStatus(HttpStatus.CREATED)
    public FixDto addFix(@PathVariable Long id,
                         @Valid @RequestBody CreateFixRequest request) {
        return incidents.addFix(id, request);
    }

    private Severity parseSeverity(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Severity.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid severity '" + value + "'. Expected one of P1, P2, P3, P4.");
        }
    }

    private IncidentStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return IncidentStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Invalid status '" + value + "'. Expected one of OPEN, INVESTIGATING, MITIGATED, RESOLVED, CLOSED.");
        }
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
