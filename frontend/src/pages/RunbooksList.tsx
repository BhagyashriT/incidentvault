import { useCallback, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../api/client";
import { useApi } from "../hooks/useApi";
import { useDebouncedValue } from "../hooks/useDebouncedValue";
import { EmptyState, ErrorState, LoadingState } from "../components/States";

export function RunbooksList() {
  const [q, setQ] = useState("");
  const debouncedQ = useDebouncedValue(q);

  const fetcher = useCallback(
    () => api.listRunbooks(debouncedQ || undefined),
    [debouncedQ],
  );
  const { data, loading, error, refetch } = useApi(fetcher, [debouncedQ]);

  const runbooks = data ?? [];

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>Runbooks</h1>
          <p>Playbooks for responding to known failure modes.</p>
        </div>
      </div>

      <div className="toolbar" role="search">
        <div className="field grow">
          <label htmlFor="runbook-search">Search</label>
          <input
            id="runbook-search"
            className="input"
            type="search"
            placeholder="Search runbooks…"
            value={q}
            onChange={(e) => setQ(e.target.value)}
          />
        </div>
      </div>

      {loading && <LoadingState label="Loading runbooks…" />}
      {Boolean(error) && !loading && <ErrorState error={error} onRetry={refetch} />}
      {!loading && !error && runbooks.length === 0 && (
        <EmptyState
          title={q ? "No runbooks match" : "No runbooks yet"}
          message={q ? "Try a different search term." : "No runbooks in the vault."}
        />
      )}
      {!loading && !error && runbooks.length > 0 && (
        <div className="stack">
          {runbooks.map((rb) => (
            <Link
              key={rb.id}
              to={`/runbooks/${rb.id}`}
              className="card card-link incident-row"
            >
              <span className="incident-title">{rb.title}</span>
              <div className="incident-meta">
                <span>
                  <strong>Linked incidents</strong>
                  {rb.linkedIncidentIds.length}
                </span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
