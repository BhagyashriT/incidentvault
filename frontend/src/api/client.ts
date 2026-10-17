import type {
  IncidentDetail,
  IncidentQuery,
  IncidentSummary,
  NewIncidentPayload,
  NewTimelineEventPayload,
  RunbookDetail,
  RunbookSummary,
  TimelineEvent,
} from "./types";

/** Thrown for non-2xx responses and network failures. */
export class ApiError extends Error {
  readonly status: number | null;
  readonly body: string;

  constructor(status: number | null, body: string, message?: string) {
    super(message ?? `API request failed${status != null ? ` (${status})` : ""}`);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}

const API_BASE =
  (import.meta.env.VITE_API_URL as string | undefined)?.replace(/\/$/, "") ??
  "http://localhost:8080";

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let res: Response;
  try {
    res = await fetch(`${API_BASE}${path}`, {
      ...init,
      headers: { "Content-Type": "application/json", ...(init?.headers ?? {}) },
    });
  } catch {
    throw new ApiError(
      null,
      "",
      "Cannot reach the IncidentVault API. Is the backend running?",
    );
  }

  if (!res.ok) {
    const body = await res.text().catch(() => "");
    throw new ApiError(
      res.status,
      body,
      `API request failed (${res.status} ${res.statusText})`,
    );
  }

  if (res.status === 204) return undefined as T;
  return (await res.json()) as T;
}

function toQuery(params: Record<string, string | undefined>): string {
  const sp = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) {
    if (v !== undefined && v !== "") sp.set(k, v);
  }
  const s = sp.toString();
  return s ? `?${s}` : "";
}

export const api = {
  listIncidents: (q: IncidentQuery = {}): Promise<IncidentSummary[]> =>
    request<IncidentSummary[]>(
      `/api/incidents${toQuery({ q: q.q, severity: q.severity, service: q.service, status: q.status })}`,
    ),

  createIncident: (payload: NewIncidentPayload): Promise<IncidentDetail> =>
    request<IncidentDetail>("/api/incidents", {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  getIncident: (id: string): Promise<IncidentDetail> =>
    request<IncidentDetail>(`/api/incidents/${encodeURIComponent(id)}`),

  addTimelineEvent: (
    id: string,
    payload: NewTimelineEventPayload,
  ): Promise<TimelineEvent> =>
    request<TimelineEvent>(`/api/incidents/${encodeURIComponent(id)}/timeline`, {
      method: "POST",
      body: JSON.stringify(payload),
    }),

  listRunbooks: (q?: string): Promise<RunbookSummary[]> =>
    request<RunbookSummary[]>(`/api/runbooks${toQuery({ q })}`),

  getRunbook: (id: string): Promise<RunbookDetail> =>
    request<RunbookDetail>(`/api/runbooks/${encodeURIComponent(id)}`),
};

export { API_BASE };
