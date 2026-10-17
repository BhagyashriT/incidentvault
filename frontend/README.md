# IncidentVault — Frontend

React + TypeScript + Vite UI for browsing incidents and runbooks, backed by the
IncidentVault Spring Boot API. Dark-slate theme, plain CSS, react-router.

## Prerequisites

- Node 24+, npm 10+
- The IncidentVault backend running (Spring Boot, all endpoints under `/api`)

## Dev

```bash
npm install
npm run dev
```

The dev server starts at http://localhost:5173.

### API URL

The frontend reads the API base from `VITE_API_URL` (default
`http://localhost:8080`). To point at another host:

```bash
VITE_API_URL=https://api.example.com npm run dev
```

## Build

```bash
npm run build     # type-checks (tsc) + builds to dist/
npm run preview   # serve the production build locally
```

## Docker

The multi-stage `Dockerfile` builds the app (`node:24-alpine`) and serves
`dist/` from `nginx:alpine`. The bundled `nginx.conf`:

- proxies `/api/` → `http://backend:8080/` (compose service name)
- serves `/assets/*` with long-lived immutable caching
- falls back to `index.html` for client-side routes

```bash
docker build -t incidentvault-frontend .
```

## Pages

| Route            | Page                                          |
| ---------------- | --------------------------------------------- |
| `/`              | Incident list (search, severity/service/status filters) |
| `/incidents/new` | New incident form                             |
| `/incidents/:id` | Incident detail: timeline, root causes, fixes, log snippets, linked runbooks |
| `/runbooks`      | Runbook list (search)                         |
| `/runbooks/:id`  | Runbook viewer: numbered steps + linked incidents |

## Structure

```
src/
  api/
    client.ts   # typed fetch wrapper, base URL from VITE_API_URL, ApiError on non-2xx
    types.ts    # Incident, TimelineEvent, Runbook, … matching the backend contract
  components/   # Nav, badges/chips, loading/empty/error states
  hooks/        # useApi (fetch-on-mount), useDebouncedValue
  pages/        # route components
  styles/
    tokens.css  # design tokens (colors, spacing, radius, type)
    global.css  # layout, cards, timeline, forms, code blocks
  App.tsx       # routes
  main.tsx      # entry
```
