# IncidentVault MCP Server

An [MCP](https://modelcontextprotocol.io/) server that lets AI agents (e.g. Claude)
search Bhagyashri's incident-knowledge base and contribute to it. It talks to the
IncidentVault Spring Boot backend over HTTP and exposes it to the MCP client over
stdio.

## Install

```bash
cd mcp-server
pip install -r requirements.txt
python3 server.py          # speaks MCP over stdio
```

The backend URL defaults to `http://localhost:8080`; override with the
`INCIDENTVAULT_API_URL` environment variable. Requests time out after ~10s, and
connection failures come back as plain-language error strings. No auth is
required (server side).

## Claude Desktop setup

1. Install the requirements above.
2. Copy the snippet from `mcp.json.example` into the `mcpServers` object of your
   Claude Desktop config:
   - macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
3. Replace `/ABSOLUTE/PATH/TO/server.py` with the real absolute path, e.g.
   `/Users/bhagyashri/workspace/incidentvault/mcp-server/server.py`.
4. Restart Claude Desktop. The `incidentvault` server should appear in the tools list.

## Tools

| Tool | Args | What it does |
|---|---|---|
| `search_incidents` | `query: str`, `severity?: str`, `service?: str` | GET `/api/incidents?q=&severity=&service=`; returns a compact list (id, title, severity, status, service, summary truncated to ~200 chars). |
| `get_incident` | `incident_id: int` | GET `/api/incidents/{id}`; returns a formatted block: title, severity/status/service, summary, chronological timeline, root causes (flagged validated/unvalidated), fixes, linked runbooks. |
| `get_runbook` | `query: str` | GET `/api/runbooks?q=`, takes the top hit, then GET `/api/runbooks/{id}`; returns numbered steps with commands. Says so when nothing matches. |
| `add_timeline_event` | `incident_id: int`, `actor: str`, `message: str`, `event_type: str = "NOTE"` | POST `/api/incidents/{id}/timeline` with `{actor, type, message}` (`type` ∈ NOTE/ACTION/ALERT); returns confirmation with the new event id/timestamp. |

## Example session (illustrative, not a real transcript)

> **Bhagyashri:** check what we know about the redis latency spike and add a note
> about the cache-eviction finding
>
> **Agent:** I'll search the incident knowledge base first.
> *(calls `search_incidents("redis latency")`)*
> Got 2 hits. The recent one is **#1 "Redis latency spike — checkout service"**
> (SEV-2, resolved). Let me pull the full detail.
> *(calls `get_incident(1)`)*
> The incident was resolved Oct 12; the validated root cause was a hot key
> saturating a single primary, with a failover to a replica as the fix.
> There's a linked runbook — let me grab it.
> *(calls `get_runbook("redis failover")`)*
> The runbook lists 6 steps, including:
> `4. Promote replica: $ redis-cli -h redis-replica-2 CLUSTER FAILOVER`
> Now I'll record your finding on the incident timeline.
> *(calls `add_timeline_event(incident_id=1, actor="Bhagyashri", event_type="NOTE", message="cache-eviction churn observed on redis-primary-1 during traffic ramp; correlates with latency window")`)*
> Done — note added to incident #1's timeline (event id 14, 2026-10-16T19:02:00Z).

## Layout

- `server.py` — the MCP server (current `mcp` 2.x `MCPServer` API, stdio transport)
- `requirements.txt` — `mcp`, `httpx`
- `mcp.json.example` — Claude Desktop config snippet
