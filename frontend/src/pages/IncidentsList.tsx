import { useCallback, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { api } from "../api/client";
import type { Severity } from "../api/types";
import { useApi } from "../hooks/useApi";
import { useDebouncedValue } from "../hooks/useDebouncedValue";
import { SeverityBadge, StatusChip } from "../components/Badges";
import { EmptyState, ErrorState, LoadingState } from "../components/States";

const SEVERITIES: Severity[] = ["P1", "P2", "P3", "P4"];

function formatDate(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleString();
}

export function IncidentsList() {
  const [searchParams] = useSearchParams();
  const [q, setQ] = useState(searchParams.get("q") ?? "");
  const [severity, setSeverity] = useState<"" | Severity>("");
  const [service, setService] = useState("");
  const [status, setStatus] = useState("");

  const debouncedQ = useDebouncedValue(q);
  const debouncedService = useDebouncedValue(service);

  const fetcher = useCallback(
    () =>
      api.listIncidents({
        q: debouncedQ || undefined,
        severity: severity || undefined,
        service: debouncedService || undefined,
        status: status || undefined,
      }),
    [debouncedQ, severity, debouncedService, status],
  );

  const { data, loading, error, refetch } = useApi(fetcher, [
    debouncedQ,
    severity,
    debouncedService,
    status,
  ]);

  // Service dropdown options: distinct services seen in the current result set.
  const serviceOptions = useMemo(() => {
    const seen = new Map<string, string>();
    for (const incident of data ?? []) {
      const key = incident.service.toLowerCase();
      if (!seen.has(key)) seen.set(key, incident.service);
    }
    return [...seen.values()].sort();
  }, [data]);

  const incidents = data ?? [];
  const hasFilters = Boolean(q || severity || service || status);

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Incidents</h1>
          <p>Browse and search every incident in the vault.</p>
        </div>
        <Link to="/incidents/new" className="btn">
          + New incident
        </Link>
      </div>

      <div className="toolbar" role="search">
        <div className="field grow">
          <label htmlFor="incident-search">Search</label>
          <input
            id="incident-search"
            className="input"
            type="search"
            placeholder="Search titles, summaries…"
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />
        </div>
        <div className="field">
          <label htmlFor="severity-filter">Severity</label>
          <select
            id="severity-filter"
            className="select"
            value={severity}
            onChange={(e) => setSeverity(e.target.value as "" | Severity)}
          >
            <option value="">All</option>
            {SEVERITIES.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="service-filter">Service</label>
          <select
            id="service-filter"
            className="select"
            value={service}
            onChange={(e) => setService(e.target.value)}
          >
            <option value="">All services</option>
            {serviceOptions.map((s) => (
              <option key={s} value={s}>
                {s}
              </option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="status-filter">Status</label>
          <select
            id="status-filter"
            className="select"
            value={status}
            onChange={(e) => setStatus(e.target.value)}
          >
            <option value="">All</option>
            <option value="open">Open</option>
            <option value="investigating">Investigating</option>
            <option value="mitigated">Mitigated</option>
            <option value="resolved">Resolved</option>
          </select>
        </div>
      </div>

      {loading && <LoadingState label="Loading incidents…" />}
      {Boolean(error) && !loading && <ErrorState error={error} onRetry={refetch} />}
      {!loading && !error && incidents.length === 0 && (
        <EmptyState
          title={hasFilters ? "No incidents match" : "No incidents yet"}
          message={
            hasFilters
              ? "Try widening the search or clearing a filter."
              : "Create the first incident to start the vault."
          }
        />
      )}
      {!loading && !error && incidents.length > 0 && (
        <div className="stack">
          {incidents.map((incident) => (
            <Link
              key={incident.id}
              to={`/incidents/${incident.id}`}
              className="card card-link incident-row"
            >
              <div className="incident-row-top">
                <SeverityBadge severity={incident.severity} />
                <span className="incident-title">{incident.title}</span>
              </div>
              <div className="incident-meta">
                <span>
                  <strong>Service</strong>
                  {incident.service}
                </span>
                <span>
                  <strong>Status</strong>
                  <StatusChip status={incident.status} />
                </span>
                <span>
                  <strong>Started</strong>
                  {formatDate(incident.startedAt)}
                </span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
