import { Link, NavLink } from "react-router-dom";

export function Nav() {
  return (
    <header className="topnav">
      <div className="topnav-inner">
        <Link to="/" className="brand">
          <span className="brand-mark">◈</span> IncidentVault
        </Link>
        <nav className="nav-links" aria-label="Primary">
          <NavLink
            to="/"
            end
            className={({ isActive }) =>
              `nav-link${isActive ? " active" : ""}`
            }
          >
            Incidents
          </NavLink>
          <NavLink
            to="/runbooks"
            className={({ isActive }) =>
              `nav-link${isActive ? " active" : ""}`
            }
          >
            Runbooks
          </NavLink>
          <NavLink to="/incidents/new" className="nav-link cta">
            + New incident
          </NavLink>
        </nav>
      </div>
    </header>
  );
}
