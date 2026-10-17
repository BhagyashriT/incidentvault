package com.incidentvault;

import com.incidentvault.domain.Incident;
import com.incidentvault.domain.IncidentStatus;
import com.incidentvault.domain.Runbook;
import com.incidentvault.domain.Severity;
import com.incidentvault.domain.Step;
import com.incidentvault.repository.IncidentRepository;
import com.incidentvault.repository.RunbookRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class IncidentRepositoryTest {

    @Autowired
    private IncidentRepository incidents;

    @Autowired
    private RunbookRepository runbooks;

    @Test
    void savesAndLoadsIncidentWithDefaultStatus() {
        Incident saved = incidents.save(
                new Incident("Cache stampede on flash sale", Severity.P1, null, "catalog-api", null, "Hot keys expired at once."));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(IncidentStatus.OPEN);

        Incident loaded = incidents.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getTitle()).isEqualTo("Cache stampede on flash sale");
        assertThat(loaded.getCreatedAt()).isNotNull();
    }

    @Test
    void filtersBySeverityServiceAndStatus() {
        incidents.save(new Incident("Redis failover issue", Severity.P1, IncidentStatus.OPEN, "checkout-api", null, null));
        incidents.save(new Incident("Kafka lag", Severity.P2, IncidentStatus.INVESTIGATING, "order-events", null, null));
        incidents.save(new Incident("Slow query", Severity.P1, IncidentStatus.RESOLVED, "checkout-api", null, null));

        assertThat(incidents.searchPortable(null, Severity.P1, null, null)).hasSize(2);
        assertThat(incidents.searchPortable(null, null, "order-events", null)).hasSize(1);
        assertThat(incidents.searchPortable(null, null, null, IncidentStatus.RESOLVED)).hasSize(1);
        assertThat(incidents.searchPortable(null, Severity.P1, "checkout-api", IncidentStatus.OPEN)).hasSize(1);
        assertThat(incidents.searchPortable(null, Severity.P3, null, null)).isEmpty();
    }

    @Test
    void portableSearchMatchesTitleSummaryAndService() {
        incidents.save(new Incident("Redis failover misconfig", Severity.P1, null, "checkout-api", null, "Sentinel pointed at a dead node"));
        incidents.save(new Incident("Kafka lag", Severity.P2, null, "order-events", null, "nothing relevant here"));

        assertThat(incidents.searchPortable("redis", null, null, null)).hasSize(1);
        assertThat(incidents.searchPortable("SENTINEL", null, null, null)).hasSize(1);
        assertThat(incidents.searchPortable("order-events", null, null, null)).hasSize(1);
        assertThat(incidents.searchPortable("no-such-thing", null, null, null)).isEmpty();
    }

    @Test
    void newestFirstOrdering() {
        incidents.save(new Incident("Older incident", Severity.P3, null, "svc", null, null));
        incidents.save(new Incident("Newer incident", Severity.P3, null, "svc", null, null));

        List<Incident> all = incidents.findAllByOrderByCreatedAtDesc();
        assertThat(all).hasSize(2);
        assertThat(all.get(0).getTitle()).isEqualTo("Newer incident");
    }

    @Test
    void linksRunbookToIncident() {
        Incident incident = incidents.save(
                new Incident("Pool exhaustion", Severity.P3, null, "payments-api", null, null));
        Runbook runbook = new Runbook("HikariCP triage",
                List.of(new Step(1, "Check pool stats", "Look at active vs idle", "curl localhost:8080/actuator/metrics/hikaricp.connections.active")));
        runbook.getLinkedIncidents().add(incident);
        Runbook saved = runbooks.save(runbook);

        Runbook loaded = runbooks.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getLinkedIncidents()).extracting(Incident::getId).containsExactly(incident.getId());
        assertThat(loaded.getSteps()).hasSize(1);
        assertThat(loaded.getSteps().get(0).command()).contains("hikaricp");

        assertThat(runbooks.findByLinkedIncidentsId(incident.getId()))
                .extracting(Runbook::getId).containsExactly(saved.getId());
    }
}
