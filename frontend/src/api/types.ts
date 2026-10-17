/** Domain types matching the IncidentVault backend API contract. */

export type Severity = "P1" | "P2" | "P3" | "P4";

export type TimelineEventType = "NOTE" | "ACTION" | "ALERT";

export interface IncidentSummary {
  id: string;
  title: string;
  severity: Severity;
  status: string;
  service: string;
  startedAt: string;
  resolvedAt: string | null;
  summary: string;
}

export interface TimelineEvent {
  id: string;
  timestamp: string;
  actor: string;
  type: TimelineEventType;
  message: string;
}

export interface LogSnippet {
  id: string;
  source: string;
  snippet: string;
}

export interface RootCause {
  id: string;
  hypothesis: string;
  validated: boolean;
  evidence: string;
}

export interface Fix {
  id: string;
  description: string;
  prUrl: string;
  commitSha: string;
}

export interface LinkedRunbook {
  id: string;
  title: string;
}

export interface IncidentDetail extends IncidentSummary {
  timeline: TimelineEvent[];
  logSnippets: LogSnippet[];
  rootCauses: RootCause[];
  fixes: Fix[];
  runbooks: LinkedRunbook[];
}

export interface NewIncidentPayload {
  title: string;
  severity: Severity;
  service: string;
  startedAt?: string;
  summary?: string;
}

export interface NewTimelineEventPayload {
  actor: string;
  type: TimelineEventType;
  message: string;
}

export interface IncidentQuery {
  q?: string;
  severity?: Severity | "";
  service?: string;
  status?: string;
}

export interface RunbookSummary {
  id: string;
  title: string;
  linkedIncidentIds: string[];
}

export interface RunbookStep {
  order: number;
  title: string;
  detail: string;
  command?: string;
}

export interface RunbookLinkedIncident {
  id: string;
  title: string;
}

export interface RunbookDetail {
  id: string;
  title: string;
  steps: RunbookStep[];
  linkedIncidents: RunbookLinkedIncident[];
}
