import { useCallback, useState, type FormEvent } from "react";
import { Link, useParams } from "react-router-dom";
import { api, ApiError } from "../api/client";
import type { IncidentDetail as IncidentDetailType, TimelineEventType } from "../api/types";
import { useApi } from "../hooks/useApi";
import { SeverityBadge, StatusChip, TypeChip, ActorChip } from "../components/Badges";
import { EmptyState, ErrorState, LoadingState } from "../components/States";

const EVENT_TYPES: TimelineEventType[] = ["NOTE", "ACTION", "ALERT"];

function formatDateTime(iso: string): string {
  const d = new Date(iso);
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleString();
}

function AddTimelineEventForm({
  incidentId,
  onAdded,
}: {
  incidentId: string;
  onAdded: () => void;
}) {
  const [actor, setActor] = useState("");
  const [type, setType] = useState<TimelineEventType>("NOTE");
  const [message, setMessage] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      await api.addTimelineEvent(incidentId, {
        actor: actor.trim(),
        type,
        message: message.trim(),
      });
      setActor("");
      setType("NOTE");
      setMessage("");
      onAdded();
    } catch (err) {
      if (err instanceof ApiError && err.status === null) {
        setError("Cannot reach the API. The event was not saved.");
      } else if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("Failed to add the event.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <form className="mini-form" onSubmit={handleSubmit}>
      <h3>Add timeline event</h3>
      {error && (
        <div className="form-error" role="alert">
          {error}
        </div>
      )}
      <div className="form-row">
        <div className="field">
          <label htmlFor="event-actor">Actor</label>
          <input
            id="event-actor"
            className="input"
            required
            placeholder="e.g. oncall-sre"
            value={actor}
            onChange={(e) => setActor(e.target.value)}
          />
        </div>
        <div className="field">
          <label htmlFor="event-type">Type</label>
          <select
            id="event-type"
            className="select"
            value={type}
            onChange={(e) => setType(e.target.value as TimelineEventType)}
          >
            {EVENT_TYPES.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </div>
      </div>
      <div className="field">
        <label htmlFor="event-message">Message</label>
        <textarea
          id="event-message"
          className="textarea"
          required
          placeholder="What happened?"
          value={message}
          onChange={(e) => setMessage(e.target.value)}
        />
      </div>
      <button type="submit" className="btn" disabled={submitting}>
        {submitting ? "Adding…" : "Add event"}
      </button>
    </form>
  );
}

function IncidentDetailView({
  incident,
  refetch,
}: {
  incident: IncidentDetailType;
  refetch: () => void;
}) {
  return (
    <div>
      <div className="detail-header">
        <div className="detail-header-top">
          <SeverityBadge severity={incident.severity} />
          <StatusChip status={incident.status} />
          <h1>{incident.title}</h1>
        </div>
        <div className="card">
          <div className="detail-meta-grid">
            <div>
              <div className="meta-label">Service</div>
              <div className="meta-value">{incident.service}</div>
            </div>
            <div>
              <div className="meta-label">Started</div>
              <div className="meta-value">{formatDateTime(incident.startedAt)}</div>
            </div>
            <div>
              <div className="meta-label">Resolved</div>
              <div className="meta-value">
                {incident.resolvedAt ? formatDateTime(incident.resolvedAt) : "—"}
              </div>
            </div>
          </div>
          {incident.summary && (
            <div style={{ marginTop: "var(--space-4)" }}>
              <div className="meta-label">Summary</div>
              <p className="meta-value">{incident.summary}</p>
            </div>
          )}
        </div>
      </div>

      <section className="section" aria-label="Timeline">
        <h2>Timeline</h2>
        {incident.timeline.length === 0 ? (
          <EmptyState
            title="No timeline events"
            message="Nothing recorded yet — add the first event below."
          />
        ) : (
          <ol className="timeline">
            {incident.timeline.map((event) => (
              <li key={event.id} className={`timeline-item type-${event.type}`}>
                <div className="timeline-meta">
                  <span className="timeline-time">
                    {formatDateTime(event.timestamp)}
                  </span>
                  <ActorChip actor={event.actor} />
                  <TypeChip type={event.type} />
                </div>
                <p className="timeline-message">{event.message}</p>
              </li>
            ))}
          </ol>
        )}
      </section>

      <section className="section" aria-label="Add timeline event">
        <AddTimelineEventForm incidentId={incident.id} onAdded={refetch} />
      </section>

      <section className="section" aria-label="Root causes">
        <h2>Root causes</h2>
        {incident.rootCauses.length === 0 ? (
          <p className="section-sub">No root causes recorded.</p>
        ) : (
          <div className="stack">
            {incident.rootCauses.map((rc) => (
              <div
                key={rc.id}
                className={`root-cause${rc.validated ? " validated" : ""}`}
              >
                <div className="detail-header-top">
                  <strong>{rc.hypothesis}</strong>
                  {rc.validated ? (
                    <span className="chip chip-validated">VALIDATED</span>
                  ) : (
                    <span className="chip chip-neutral">UNVALIDATED</span>
                  )}
                </div>
                {rc.evidence && (
                  <p className="evidence">
                    <strong>Evidence — </strong>
                    {rc.evidence}
                  </p>
                )}
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="section" aria-label="Fixes">
        <h2>Fixes</h2>
        {incident.fixes.length === 0 ? (
          <p className="section-sub">No fixes recorded.</p>
        ) : (
          <div className="stack">
            {incident.fixes.map((fix) => (
              <div key={fix.id} className="card incident-row">
                <p>{fix.description}</p>
                <div className="incident-meta">
                  {fix.prUrl && (
                    <a
                      href={fix.prUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="fix-pr"
                    >
                      {fix.prUrl}
                    </a>
                  )}
                  {fix.commitSha && (
                    <span className="fix-pr">{fix.commitSha}</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="section" aria-label="Log snippets">
        <h2>Log snippets</h2>
        {incident.logSnippets.length === 0 ? (
          <p className="section-sub">No log snippets attached.</p>
        ) : (
          <div className="stack">
            {incident.logSnippets.map((log) => (
              <div key={log.id}>
                <div className="snippet-source">{log.source}</div>
                <pre className="codeblock">{log.snippet}</pre>
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="section" aria-label="Linked runbooks">
        <h2>Linked runbooks</h2>
        {incident.runbooks.length === 0 ? (
          <p className="section-sub">No runbooks linked to this incident.</p>
        ) : (
          <ul className="link-list">
            {incident.runbooks.map((rb) => (
              <li key={rb.id} className="card">
                <Link to={`/runbooks/${rb.id}`}>{rb.title}</Link>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}

export function IncidentDetail() {
  const { id } = useParams<{ id: string }>();

  const fetcher = useCallback(() => {
    if (!id) return Promise.reject(new Error("Missing incident id."));
    return api.getIncident(id);
  }, [id]);

  const { data, loading, error, refetch } = useApi(fetcher, [id]);

  if (loading) return <LoadingState label="Loading incident…" />;
  if (error) return <ErrorState error={error} onRetry={refetch} />;
  if (!data) return <EmptyState title="Incident not found" />;

  return <IncidentDetailView incident={data} refetch={refetch} />;
}
