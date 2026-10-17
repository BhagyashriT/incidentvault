# IncidentVault — Backend

Spring Boot 3.3 / Java 21 REST API for IncidentVault, an incident knowledge base:
track incidents, timelines, log snippets, root causes, fixes, and reusable runbooks.

## Prerequisites

- Java 21
- Maven (or use the wrapper `./mvnw`, which downloads Maven 3.9.16 on first run)
- PostgreSQL 14+ for local/prod runs (tests use in-memory H2)

## Run locally with Postgres

```bash
# 1. Create the database and user
createdb incidentvault
psql -c "CREATE USER incidentvault WITH PASSWORD 'incidentvault';"
psql -c "GRANT ALL PRIVILEGES ON DATABASE incidentvault TO incidentvault;"

# 2. Start the app (Flyway runs V1 schema + V2 seed data automatically)
./mvnw spring-boot:run
```

The API is then available at `http://localhost:8080/api`, e.g.
`GET http://localhost:8080/api/incidents`. Health: `GET http://localhost:8080/actuator/health`.

### Environment variables

| Variable | Default | Purpose |
|---|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/incidentvault` | JDBC URL |
| `DATABASE_USER` | `incidentvault` | DB user |
| `DATABASE_PASSWORD` | `incidentvault` | DB password |
| `API_KEY` | _(empty)_ | Enables API-key auth when set |

## API-key placeholder

`ApiKeyFilter` (`com.incidentvault.config.ApiKeyFilter`) reads the `X-API-Key`
request header and compares it against `incidentvault.api-key` (env `API_KEY`).
**When the property is empty (the default), authentication is disabled** — every
request passes through. This is intentionally minimal: a real deployment should
replace it with proper authentication (OAuth2/JWT, rotated secrets, per-key
scopes). The health endpoint (`/actuator/health`) is always unauthenticated so
load-balancer probes keep working.

To enable: `API_KEY=your-secret ./mvnw spring-boot:run`, then send
`X-API-Key: your-secret` with each request.

## Full-text search

`V1__schema.sql` adds generated `tsvector` columns (GIN-indexed) on `incidents`
(title + summary + service) and `runbooks` (title + steps). At runtime the
search service is dialect-aware: on PostgreSQL it uses native
`search_vector @@ plainto_tsquery(...)` queries ordered by `ts_rank`; on H2
(tests) it falls back to portable JPQL `LIKE` queries. Query normalization
(trim, blank → no-op) lives in `SearchSupport` and is unit-tested.

## Build & test

```bash
./mvnw test          # unit + slice + MockMvc tests (H2, no Postgres needed)
./mvnw package       # produces target/incidentvault-0.1.0.jar
```

## Docker

```bash
docker build -t incidentvault-backend ./backend
docker run -p 8080:8080 \
  -e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/incidentvault \
  -e DATABASE_USER=incidentvault -e DATABASE_PASSWORD=incidentvault \
  incidentvault-backend
```

## API overview

All routes are under `/api` and speak JSON. Errors are `{"error": "..."}`
(404 for unknown ids, 400 for validation errors).

- `GET /api/incidents?q=&severity=&service=&status=` — newest first; `q` is full-text
- `POST /api/incidents` — `{title, severity, status?, service, startedAt?, summary?}` → 201
- `GET /api/incidents/{id}` — full detail with timeline, logs, root causes, fixes, runbooks
- `PATCH /api/incidents/{id}` — `{status?, severity?, summary?, resolvedAt?}`
- `POST /api/incidents/{id}/timeline` — `{actor, type, message, timestamp?}` → 201
- `GET /api/incidents/{id}/timeline` — ordered by timestamp
- `POST /api/incidents/{id}/logs` — `{source, snippet}` → 201
- `POST /api/incidents/{id}/root-causes` — `{hypothesis, validated?, evidence?}` → 201
- `POST /api/incidents/{id}/fixes` — `{description, prUrl?, commitSha?}` → 201
- `GET /api/runbooks?q=` — `(id, title, linkedIncidentIds)`; `q` searches title + steps
- `POST /api/runbooks` — `{title, steps:[{order,title,detail,command?}], linkedIncidentIds?[]}` → 201
- `GET /api/runbooks/{id}` — steps plus linked incidents
- `POST /api/runbooks/{id}/link/{incidentId}` — link a runbook to an incident
