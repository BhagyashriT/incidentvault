import { ApiError } from "../api/client";

export function LoadingState({ label = "Loading…" }: { label?: string }) {
  return (
    <div className="state-block" role="status" aria-live="polite">
      <div className="spinner" aria-hidden="true" />
      <p>{label}</p>
    </div>
  );
}

export function EmptyState({
  title,
  message,
}: {
  title: string;
  message?: string;
}) {
  return (
    <div className="state-block">
      <h3>{title}</h3>
      {message && <p>{message}</p>}
    </div>
  );
}

export function ErrorState({
  error,
  onRetry,
}: {
  error: unknown;
  onRetry?: () => void;
}) {
  const isApiDown = error instanceof ApiError && error.status === null;
  const detail =
    error instanceof Error ? error.message : "Something went wrong.";

  return (
    <div className="state-block error-block" role="alert">
      <h3>{isApiDown ? "API unavailable" : "Something went wrong"}</h3>
      <p>
        {isApiDown
          ? "The IncidentVault backend could not be reached. Check that it is running (default http://localhost:8080, or set VITE_API_URL) and try again."
          : detail}
      </p>
      {onRetry && (
        <button type="button" className="btn btn-ghost" onClick={onRetry}>
          Retry
        </button>
      )}
    </div>
  );
}
