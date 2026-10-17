import { useState, type FormEvent } from "react";
import { useNavigate } from "react-router-dom";
import { api, ApiError } from "../api/client";
import type { Severity } from "../api/types";

const SEVERITIES: Severity[] = ["P1", "P2", "P3", "P4"];

/** Convert a datetime-local value to an ISO string for the API. */
function toIso(local: string): string | undefined {
  if (!local) return undefined;
  const d = new Date(local);
  return Number.isNaN(d.getTime()) ? undefined : d.toISOString();
}

export function NewIncident() {
  const navigate = useNavigate();
  const [title, setTitle] = useState("");
  const [severity, setSeverity] = useState<Severity>("P3");
  const [service, setService] = useState("");
  const [startedAt, setStartedAt] = useState("");
  const [summary, setSummary] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const incident = await api.createIncident({
        title: title.trim(),
        severity,
        service: service.trim(),
        startedAt: toIso(startedAt),
        summary: summary.trim() || undefined,
      });
      navigate(`/incidents/${incident.id}`);
    } catch (err) {
      if (err instanceof ApiError && err.status === null) {
        setError(
          "Cannot reach the IncidentVault API. Check that the backend is running and try again.",
        );
      } else if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("Failed to create the incident.");
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <div className="page-head">
        <div>
          <h1>New incident</h1>
          <p>Open a fresh incident record in the vault.</p>
        </div>
      </div>

      <form className="card" onSubmit={handleSubmit} noValidate={false}>
        {error && (
          <div className="form-error" role="alert">
            {error}
          </div>
        )}

        <div className="field">
          <label htmlFor="new-title">Title</label>
          <input
            id="new-title"
            className="input"
            required
            placeholder="e.g. Checkout latency spike in payments-api"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
          />
        </div>

        <div className="form-row">
          <div className="field">
            <label htmlFor="new-severity">Severity</label>
            <select
              id="new-severity"
              className="select"
              value={severity}
              onChange={(e) => setSeverity(e.target.value as Severity)}
            >
              {SEVERITIES.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </div>
          <div className="field">
            <label htmlFor="new-service">Service</label>
            <input
              id="new-service"
              className="input"
              required
              placeholder="e.g. payments-api"
              value={service}
              onChange={(e) => setService(e.target.value)}
            />
          </div>
        </div>

        <div className="field">
          <label htmlFor="new-started-at">
            Started at <span style={{ fontWeight: 400 }}>(optional)</span>
          </label>
          <input
            id="new-started-at"
            className="input"
            type="datetime-local"
            value={startedAt}
            onChange={(e) => setStartedAt(e.target.value)}
          />
        </div>

        <div className="field">
          <label htmlFor="new-summary">Summary</label>
          <textarea
            id="new-summary"
            className="textarea"
            placeholder="What happened, blast radius, current state…"
            value={summary}
            onChange={(e) => setSummary(e.target.value)}
          />
        </div>

        <button type="submit" className="btn" disabled={submitting}>
          {submitting ? "Creating…" : "Create incident"}
        </button>
      </form>
    </div>
  );
}
