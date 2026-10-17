package com.incidentvault;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IncidentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createIncidentReturns201WithDefaults() throws Exception {
        mvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Cache stampede on flash sale","severity":"P1","service":"catalog-api",
                                 "summary":"Hot keys expired at once."}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.severity").value("P1"))
                .andExpect(jsonPath("$.service").value("catalog-api"));
    }

    @Test
    void createIncidentRejectsInvalidBody() throws Exception {
        mvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void getIncidentDetailIncludesChildren() throws Exception {
        long id = createIncident("Redis failover drill", "P2", "checkout-api");

        mvc.perform(post("/api/incidents/" + id + "/timeline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"actor":"on-call","type":"ALERT","message":"Latency SLO breached"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/incidents/" + id + "/root-causes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"hypothesis":"Stale failover endpoint","validated":true,"evidence":"Config diff"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/incidents/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Redis failover drill"))
                .andExpect(jsonPath("$.timeline", hasSize(1)))
                .andExpect(jsonPath("$.timeline[0].type").value("ALERT"))
                .andExpect(jsonPath("$.rootCauses", hasSize(1)))
                .andExpect(jsonPath("$.rootCauses[0].validated").value(true))
                .andExpect(jsonPath("$.logSnippets", hasSize(0)))
                .andExpect(jsonPath("$.fixes", hasSize(0)))
                .andExpect(jsonPath("$.runbooks", hasSize(0)));
    }

    @Test
    void getMissingIncidentReturns404() throws Exception {
        mvc.perform(get("/api/incidents/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Incident not found: 999999"));
    }

    @Test
    void patchIncidentUpdatesStatusAndSummary() throws Exception {
        long id = createIncident("Pool exhaustion", "P3", "payments-api");

        mvc.perform(patch("/api/incidents/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"MITIGATED","summary":"Batch holding connections"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MITIGATED"));

        // The list item is lean; verify the summary via the detail view.
        mvc.perform(get("/api/incidents/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MITIGATED"))
                .andExpect(jsonPath("$.summary").value("Batch holding connections"));
    }

    @Test
    void timelineIsOrderedByTimestamp() throws Exception {
        long id = createIncident("Ordering check", "P4", "svc");

        mvc.perform(post("/api/incidents/" + id + "/timeline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"actor":"a","type":"NOTE","message":"second","timestamp":"2026-10-02T10:00:00Z"}
                                """))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/incidents/" + id + "/timeline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"actor":"a","type":"NOTE","message":"first","timestamp":"2026-10-01T10:00:00Z"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/incidents/" + id + "/timeline"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].message").value("first"))
                .andExpect(jsonPath("$[1].message").value("second"));
    }

    @Test
    void addLogSnippetAndFix() throws Exception {
        long id = createIncident("Log check", "P2", "svc");

        mvc.perform(post("/api/incidents/" + id + "/logs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"source":"app log","snippet":"Connection timed out"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("app log"));

        mvc.perform(post("/api/incidents/" + id + "/fixes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Added timeout","prUrl":"https://github.com/example-org/svc/pull/1","commitSha":"abc123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prUrl").value("https://github.com/example-org/svc/pull/1"));
    }

    @Test
    void invalidEnumQueryParamReturns400() throws Exception {
        mvc.perform(get("/api/incidents").param("severity", "P9"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void searchUsesH2Fallback() throws Exception {
        createIncident("Zebra cache stampede", "P2", "catalog-api");
        createIncident("Unrelated kafka lag", "P2", "order-events");

        MvcResult result = mvc.perform(get("/api/incidents").param("q", "zebra"))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(body).hasSize(1);
        assertThat(body.get(0).get("title").asText()).isEqualTo("Zebra cache stampede");
    }

    @Test
    void searchWithFilters() throws Exception {
        createIncident("Filter me P1", "P1", "checkout-api");

        mvc.perform(get("/api/incidents").param("severity", "P1").param("service", "checkout-api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Filter me P1"));
    }

    @Test
    void createRunbookAndLinkToIncident() throws Exception {
        long incidentId = createIncident("Link target", "P2", "svc");

        MvcResult created = mvc.perform(post("/api/runbooks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Failover verification",
                                 "steps":[{"order":1,"title":"Check primary","detail":"Verify role","command":"redis-cli info replication"}],
                                 "linkedIncidentIds":[%d]}
                                """.formatted(incidentId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.steps", hasSize(1)))
                .andExpect(jsonPath("$.steps[0].command").value("redis-cli info replication"))
                .andExpect(jsonPath("$.linkedIncidents", hasSize(1)))
                .andReturn();
        long runbookId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        mvc.perform(get("/api/runbooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].linkedIncidentIds[0]").value(incidentId));

        // Link endpoint is idempotent and returns the runbook.
        mvc.perform(post("/api/runbooks/" + runbookId + "/link/" + incidentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.linkedIncidents", hasSize(1)))
                .andExpect(jsonPath("$.linkedIncidents[0].title").value("Link target"));

        // q searches title and steps.
        mvc.perform(get("/api/runbooks").param("q", "failover"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Failover verification"));

        mvc.perform(get("/api/runbooks/" + runbookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.steps[0].title").value("Check primary"));
    }

    @Test
    void healthEndpointIsUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    private long createIncident(String title, String severity, String service) throws Exception {
        MvcResult result = mvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"%s","severity":"%s","service":"%s"}
                                """.formatted(title, severity, service)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }
}
