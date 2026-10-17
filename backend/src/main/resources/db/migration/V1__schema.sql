-- IncidentVault V1 schema (PostgreSQL).
-- Enums are stored as VARCHAR with CHECK constraints; full-text search uses
-- generated tsvector columns with GIN indexes.

CREATE TABLE incidents (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    severity      VARCHAR(8) NOT NULL CHECK (severity IN ('P1', 'P2', 'P3', 'P4')),
    status        VARCHAR(16) NOT NULL DEFAULT 'OPEN'
                  CHECK (status IN ('OPEN', 'INVESTIGATING', 'MITIGATED', 'RESOLVED', 'CLOSED')),
    service       VARCHAR(255) NOT NULL,
    started_at    TIMESTAMPTZ,
    resolved_at   TIMESTAMPTZ,
    summary       TEXT,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    search_vector tsvector GENERATED ALWAYS AS (
                      to_tsvector('english',
                          coalesce(title, '') || ' ' ||
                          coalesce(summary, '') || ' ' ||
                          coalesce(service, ''))
                  ) STORED
);
CREATE INDEX incidents_search_vector_idx ON incidents USING GIN (search_vector);
CREATE INDEX incidents_severity_idx ON incidents (severity);
CREATE INDEX incidents_service_idx ON incidents (service);
CREATE INDEX incidents_status_idx ON incidents (status);
CREATE INDEX incidents_created_at_idx ON incidents (created_at DESC);

CREATE TABLE timeline_events (
    id          BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    timestamp   TIMESTAMPTZ NOT NULL DEFAULT now(),
    actor       VARCHAR(255),
    type        VARCHAR(16) NOT NULL CHECK (type IN ('NOTE', 'ACTION', 'ALERT')),
    message     TEXT NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX timeline_events_incident_idx ON timeline_events (incident_id, timestamp);

CREATE TABLE log_snippets (
    id          BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    source      VARCHAR(255),
    snippet     TEXT,
    captured_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX log_snippets_incident_idx ON log_snippets (incident_id);

CREATE TABLE root_causes (
    id          BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    hypothesis  TEXT NOT NULL,
    validated   BOOLEAN NOT NULL DEFAULT FALSE,
    evidence    TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX root_causes_incident_idx ON root_causes (incident_id);

CREATE TABLE fixes (
    id          BIGSERIAL PRIMARY KEY,
    incident_id BIGINT NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    pr_url      VARCHAR(1024),
    commit_sha  VARCHAR(64),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX fixes_incident_idx ON fixes (incident_id);

CREATE TABLE runbooks (
    id            BIGSERIAL PRIMARY KEY,
    title         VARCHAR(255) NOT NULL,
    steps         TEXT NOT NULL DEFAULT '[]',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    search_vector tsvector GENERATED ALWAYS AS (
                      to_tsvector('english',
                          coalesce(title, '') || ' ' ||
                          coalesce(steps, ''))
                  ) STORED
);
CREATE INDEX runbooks_search_vector_idx ON runbooks USING GIN (search_vector);

CREATE TABLE runbook_incidents (
    runbook_id  BIGINT NOT NULL REFERENCES runbooks (id) ON DELETE CASCADE,
    incident_id BIGINT NOT NULL REFERENCES incidents (id) ON DELETE CASCADE,
    PRIMARY KEY (runbook_id, incident_id)
);
