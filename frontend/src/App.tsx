import { Route, Routes } from "react-router-dom";
import { Nav } from "./components/Nav";
import { Welcome } from "./pages/Welcome";
import { IncidentsList } from "./pages/IncidentsList";
import { NewIncident } from "./pages/NewIncident";
import { IncidentDetail } from "./pages/IncidentDetail";
import { RunbooksList } from "./pages/RunbooksList";
import { RunbookDetail } from "./pages/RunbookDetail";
import { EmptyState } from "./components/States";

export function App() {
  return (
    <div className="app-shell">
      <Nav />
      <main className="main">
        <Routes>
          <Route path="/" element={<Welcome />} />
          <Route path="/incidents" element={<IncidentsList />} />
          <Route path="/incidents/new" element={<NewIncident />} />
          <Route path="/incidents/:id" element={<IncidentDetail />} />
          <Route path="/runbooks" element={<RunbooksList />} />
          <Route path="/runbooks/:id" element={<RunbookDetail />} />
          <Route
            path="*"
            element={
              <EmptyState
                title="Page not found"
                message="The page you are looking for does not exist."
              />
            }
          />
        </Routes>
      </main>
      <footer className="footer">IncidentVault — incident &amp; runbook browser</footer>
    </div>
  );
}
