"""IncidentVault MCP server.

Exposes Bhagyashri's incident-knowledge base to MCP clients (e.g. Claude
Desktop) as four tools. The server talks to the IncidentVault Spring Boot
backend over HTTP (see INCIDENTVAULT_API_URL) and communicates with the
MCP client over stdio.

Run:
    pip install -r requirements.txt
    python3 server.py
"""

from __future__ import annotations

import os
from typing import Any

import httpx
from mcp.server.mcpserver import MCPServer

# ---------------------------------------------------------------------------
# Config
# ---------------------------------------------------------------------------

API_URL: str = os.environ.get("INCIDENTVAULT_API_URL", "http://localhost:8080")
TIMEOUT_S: float = 10.0
SUMMARY_TRUNCATE: int = 200
VALID_EVENT_TYPES = ("NOTE", "ACTION", "ALERT")

mcp = MCPServer("incidentvault")


# ---------------------------------------------------------------------------
# Backend client helpers
# ---------------------------------------------------------------------------


def _client() -> httpx.Client:
    """Build a short-lived HTTP client for the IncidentVault API.

    trust_env=False: proxy variables in this shell are unreliable, and the
    API is normally a localhost service we never want proxied.
    """
    return httpx.Client(base_url=API_URL, timeout=TIMEOUT_S, trust_env=False)


def _get(path: str, params: dict[str, Any] | None = None) -> tuple[int, Any]:
    """GET a backend path. Returns (status_code, decoded JSON)."""
    try:
        with _client() as client:
            response = client.get(path, params=params or {})
    except httpx.InvalidURL as exc:
        # httpx.InvalidURL is a ValueError, not a RequestError; normalize it so
        # callers only need to handle RequestError.
        raise httpx.RequestError(f"invalid API URL {API_URL!r}: {exc}") from exc
    return response.status_code, response.json()


def _post(path: str, payload: dict[str, Any]) -> tuple[int, Any]:
    """POST a backend path. Returns (status_code, decoded JSON)."""
    try:
        with _client() as client:
            response = client.post(path, json=payload)
    except httpx.InvalidURL as exc:
        raise httpx.RequestError(f"invalid API URL {API_URL!r}: {exc}") from exc
    return response.status_code, response.json()


def _not_reachable(exc: httpx.RequestError) -> str:
    return f"IncidentVault API not reachable at {API_URL}: {exc}"


# ---------------------------------------------------------------------------
# Formatting helpers
# ---------------------------------------------------------------------------


def _truncate(text: Any, limit: int = SUMMARY_TRUNCATE) -> str:
    text = str(text or "")
    return text if len(text) <= limit else text[:limit].rstrip() + "..."


def _fmt_header(inc: dict[str, Any]) -> str:
    return (
        f"## {inc.get('title', '(untitled)')}\n"
        f"**ID:** {inc.get('id')}  |  "
        f"**Severity:** {inc.get('severity')}  |  "
        f"**Status:** {inc.get('status')}  |  "
        f"**Service:** {inc.get('service')}\n"
        f"**Started:** {inc.get('startedAt')}  |  "
        f"**Resolved:** {inc.get('resolvedAt') or '—'}\n"
    )


# ---------------------------------------------------------------------------
# Tools
# ---------------------------------------------------------------------------


@mcp.tool(
    description=(
        "Search the incident knowledge base by free-text query, optionally "
        "filtered by severity and/or service. Returns a compact list of "
        "matching incidents (id, title, severity, status, service, summary)."
    )
)
def search_incidents(
    query: str, severity: str | None = None, service: str | None = None
) -> str:
    """Search incidents by text, severity, and service."""
    params: dict[str, Any] = {"q": query}
    if severity:
        params["severity"] = severity
    if service:
        params["service"] = service
    try:
        status, incidents = _get("/api/incidents", params)
    except httpx.RequestError as exc:
        return _not_reachable(exc)
    if status != 200:
        return f"Incident search failed: backend returned HTTP {status}."
    if not incidents:
        return "No incidents matched your search."
    lines = ["# Incident search results\n"]
    for inc in incidents:
        lines.append(
            f"- **#{inc.get('id')}** [{inc.get('severity')}] "
            f"{inc.get('title')} "
            f"({inc.get('status')} · {inc.get('service')})\n"
            f"  {_truncate(inc.get('summary'))}"
        )
    return "\n".join(lines)


@mcp.tool(
    description=(
        "Fetch full detail for one incident by ID: summary, chronological "
        "timeline, root causes (with validation status), fixes, and linked "
        "runbooks."
    )
)
def get_incident(incident_id: int) -> str:
    """Fetch the full detail of a single incident."""
    try:
        status, inc = _get(f"/api/incidents/{incident_id}")
    except httpx.RequestError as exc:
        return _not_reachable(exc)
    if status != 200:
        return (
            f"Could not load incident {incident_id}: "
            f"backend returned HTTP {status}."
        )

    parts = [_fmt_header(inc), f"**Summary:**\n{inc.get('summary', '—')}\n"]

    timeline = sorted(
        inc.get("timeline") or [], key=lambda e: e.get("timestamp") or ""
    )
    parts.append("### Timeline")
    if timeline:
        for e in timeline:
            parts.append(
                f"- `{e.get('timestamp')}` **[{e.get('type')}]** "
                f"{e.get('actor')}: {e.get('message')}"
            )
    else:
        parts.append("_No timeline events recorded._")

    parts.append("\n### Root causes")
    root_causes = inc.get("rootCauses") or []
    if root_causes:
        for rc in root_causes:
            mark = "✅ validated" if rc.get("validated") else "❓ unvalidated"
            parts.append(
                f"- {mark} — {rc.get('hypothesis')}\n"
                f"  Evidence: {rc.get('evidence') or '—'}"
            )
    else:
        parts.append("_No root causes recorded._")

    parts.append("\n### Fixes")
    fixes = inc.get("fixes") or []
    if fixes:
        for f in fixes:
            ref = f.get("prUrl") or f.get("commitSha") or "no link"
            parts.append(f"- {f.get('description')} ({ref})")
    else:
        parts.append("_No fixes recorded._")

    parts.append("\n### Linked runbooks")
    runbooks = inc.get("runbooks") or []
    if runbooks:
        for r in runbooks:
            parts.append(f"- **{r.get('id')}** — {r.get('title')}")
    else:
        parts.append("_No linked runbooks._")

    return "\n".join(parts)


@mcp.tool(
    description=(
        "Look up the best-matching runbook for a query (e.g. 'redis failover') "
        "and return its full procedure as numbered steps, including commands."
    )
)
def get_runbook(query: str) -> str:
    """Return the top-matching runbook as numbered steps."""
    try:
        status, runbooks = _get("/api/runbooks", {"q": query})
    except httpx.RequestError as exc:
        return _not_reachable(exc)
    if status != 200:
        return f"Runbook search failed: backend returned HTTP {status}."
    if not runbooks:
        return f'No runbook found matching "{query}".'

    top = runbooks[0]
    try:
        status, rb = _get(f"/api/runbooks/{top.get('id')}")
    except httpx.RequestError as exc:
        return _not_reachable(exc)
    if status != 200:
        return (
            f"Could not load runbook {top.get('id')}: "
            f"backend returned HTTP {status}."
        )

    lines = [f"# {rb.get('title')} (runbook {rb.get('id')})\n"]
    steps = sorted(rb.get("steps") or [], key=lambda s: s.get("order") or 0)
    for s in steps:
        lines.append(f"{s.get('order')}. **{s.get('title')}**\n   {s.get('detail')}")
        if s.get("command"):
            lines.append(f"   `$ {s.get('command')}`")
    linked = rb.get("linkedIncidents") or []
    if linked:
        lines.append("\n_Used in incidents:_ " + ", ".join(
            f"#{i.get('id')} {i.get('title')}" for i in linked
        ))
    return "\n".join(lines)


@mcp.tool(
    description=(
        "Append a timeline event (NOTE, ACTION, or ALERT) to an incident, "
        "e.g. recording an investigation finding or a mitigation step."
    )
)
def add_timeline_event(
    incident_id: int, actor: str, message: str, event_type: str = "NOTE"
) -> str:
    """Append a timeline event to an incident."""
    event_type = (event_type or "NOTE").upper()
    if event_type not in VALID_EVENT_TYPES:
        return (
            f'Invalid event_type "{event_type}". '
            f"Must be one of {', '.join(VALID_EVENT_TYPES)}."
        )
    try:
        status, body = _post(
            f"/api/incidents/{incident_id}/timeline",
            {"actor": actor, "type": event_type, "message": message},
        )
    except httpx.RequestError as exc:
        return _not_reachable(exc)
    if status not in (200, 201):
        return (
            f"Could not add timeline event to incident {incident_id}: "
            f"backend returned HTTP {status}."
        )
    event_id = (body or {}).get("id", "?") if isinstance(body, dict) else "?"
    timestamp = (body or {}).get("timestamp", "—") if isinstance(body, dict) else "—"
    return (
        f"✅ Timeline event added to incident {incident_id} "
        f"(event id {event_id}, {timestamp})."
    )


# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------

if __name__ == "__main__":
    mcp.run()
