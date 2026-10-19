import { Link } from "react-router-dom";

const STEPS = [
  {
    title: "Record",
    detail:
      "When something breaks, log the incident: what happened, which service, and when. Capture the timeline as you investigate.",
  },
  {
    title: "Capture the fix",
    detail:
      "Add evidence, the validated root cause, and the fix — then link the runbook so the response is repeatable next time.",
  },
  {
    title: "Search next time",
    detail:
      "When it breaks again, search the vault first — or let your AI agent query it through the MCP server, mid-incident.",
  },
];

export function Welcome() {
  return (
    <div className="welcome">
      <section className="welcome-hero">
        <span className="welcome-mark" aria-hidden="true">
          ◈
        </span>
        <h1>Welcome to IncidentVault</h1>
        <p className="welcome-tagline">
          Shared incident knowledge for humans and AI agents. Record what broke
          and how you fixed it — so the next outage starts with a search, not
          a war room.
        </p>
      </section>

      <section className="welcome-actions" aria-label="Get started">
        <Link to="/incidents?q=redis" className="card card-link welcome-card">
          <span className="welcome-card-icon" aria-hidden="true">
            🔍
          </span>
          <h2>Search the vault</h2>
          <p>
            Find past incidents by keyword, service, or severity — including
            their timelines, root causes, and fixes.
          </p>
          <span className="welcome-card-cta">Try it: search “redis” →</span>
        </Link>
        <Link to="/incidents/new" className="card card-link welcome-card">
          <span className="welcome-card-icon" aria-hidden="true">
            ➕
          </span>
          <h2>Record an incident</h2>
          <p>
            Log what happened, build the investigation timeline, and capture
            the validated root cause and fix.
          </p>
          <span className="welcome-card-cta">New incident →</span>
        </Link>
        <Link to="/runbooks" className="card card-link welcome-card">
          <span className="welcome-card-icon" aria-hidden="true">
            📖
          </span>
          <h2>Browse runbooks</h2>
          <p>
            Step-by-step response guides for known failure modes — follow them
            the next time the alert fires.
          </p>
          <span className="welcome-card-cta">View runbooks →</span>
        </Link>
      </section>

      <section className="section" aria-label="How it works">
        <h2>How it works</h2>
        <ol className="step-list">
          {STEPS.map((step, i) => (
            <li key={step.title} className="step-card">
              <span className="step-number" aria-hidden="true">
                {i + 1}
              </span>
              <h3>{step.title}</h3>
              <p className="step-detail">{step.detail}</p>
            </li>
          ))}
        </ol>
      </section>

      <section className="welcome-agent card" aria-label="For AI agents">
        <h2>For AI agents</h2>
        <p>
          IncidentVault ships a Python MCP server that exposes the same
          knowledge base as tools — an agent can search incidents and pull
          validated troubleshooting steps mid-incident, no UI needed. See{" "}
          <code>mcp-server/</code> in the repo.
        </p>
      </section>
    </div>
  );
}
