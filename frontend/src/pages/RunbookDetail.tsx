import { useCallback } from "react";
import { Link, useParams } from "react-router-dom";
import { api } from "../api/client";
import { useApi } from "../hooks/useApi";
import { EmptyState, ErrorState, LoadingState } from "../components/States";

export function RunbookDetail() {
  const { id } = useParams<{ id: string }>();

  const fetcher = useCallback(() => {
    if (!id) return Promise.reject(new Error("Missing runbook id."));
    return api.getRunbook(id);
  }, [id]);

  const { data, loading, error, refetch } = useApi(fetcher, [id]);

  if (loading) return <LoadingState label="Loading runbook…" />;
  if (error) return <ErrorState error={error} onRetry={refetch} />;
  if (!data) return <EmptyState title="Runbook not found" />;

  const steps = [...data.steps].sort((a, b) => a.order - b.order);

  return (
    <div>
      <div className="detail-header">
        <h1>{data.title}</h1>
      </div>

      <section className="section" aria-label="Steps">
        <h2>Steps</h2>
        {steps.length === 0 ? (
          <EmptyState
            title="No steps"
            message="This runbook has no steps defined yet."
          />
        ) : (
          <ol className="step-list">
            {steps.map((step) => (
              <li key={step.order} className="card step-card">
                <span className="step-number" aria-hidden="true">
                  {step.order}
                </span>
                <h3>{step.title}</h3>
                <p className="step-detail">{step.detail}</p>
                {step.command && (
                  <pre className="codeblock">{step.command}</pre>
                )}
              </li>
            ))}
          </ol>
        )}
      </section>

      <section className="section" aria-label="Linked incidents">
        <h2>Linked incidents</h2>
        {data.linkedIncidents.length === 0 ? (
          <p className="section-sub">No incidents linked to this runbook.</p>
        ) : (
          <ul className="link-list">
            {data.linkedIncidents.map((incident) => (
              <li key={incident.id} className="card">
                <Link to={`/incidents/${incident.id}`}>{incident.title}</Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
