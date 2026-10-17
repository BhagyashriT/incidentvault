package com.incidentvault.web;

import com.incidentvault.service.RunbookService;
import com.incidentvault.web.dto.CreateRunbookRequest;
import com.incidentvault.web.dto.RunbookDetail;
import com.incidentvault.web.dto.RunbookSummary;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/runbooks")
public class RunbookController {

    private final RunbookService runbooks;

    public RunbookController(RunbookService runbooks) {
        this.runbooks = runbooks;
    }

    @GetMapping
    public List<RunbookSummary> list(@RequestParam(required = false) String q) {
        return runbooks.search(q);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RunbookDetail create(@Valid @RequestBody CreateRunbookRequest request) {
        return runbooks.create(request);
    }

    @GetMapping("/{id}")
    public RunbookDetail get(@PathVariable Long id) {
        return runbooks.get(id);
    }

    @PostMapping("/{id}/link/{incidentId}")
    public RunbookDetail linkIncident(@PathVariable Long id, @PathVariable Long incidentId) {
        return runbooks.linkIncident(id, incidentId);
    }
}
