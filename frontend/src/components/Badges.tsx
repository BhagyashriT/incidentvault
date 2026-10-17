import type { Severity, TimelineEventType } from "../api/types";

/** Severity badge, color-coded P1..P4 per the design tokens. */
export function SeverityBadge({ severity }: { severity: Severity }) {
  return <span className={`badge sev-${severity}`}>{severity}</span>;
}

export function StatusChip({ status }: { status: string }) {
  const normalized = status.toLowerCase();
  const variant =
    normalized === "resolved" || normalized === "closed"
      ? "chip-status-resolved"
      : normalized === "investigating" || normalized === "open"
        ? "chip-status-open"
        : "chip-neutral";
  return <span className={`chip ${variant}`}>{status}</span>;
}

export function TypeChip({ type }: { type: TimelineEventType }) {
  return <span className="chip chip-type">{type}</span>;
}

export function ActorChip({ actor }: { actor: string }) {
  return <span className="chip chip-actor">{actor}</span>;
}
