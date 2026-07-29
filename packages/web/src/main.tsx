// IMPORTANT : sentry.ts doit être importé AVANT React pour
// hooker les erreurs au plus tôt (avant même le render).
import "./sentry.js";
// i18n doit être importé avant App pour que les traductions soient disponibles
import "./i18n/index.js";

import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App.js";
import { Sentry } from "./sentry.js";

// Composant racine wrappé avec les providers Sentry
const root = ReactDOM.createRoot(document.getElementById("root")!);

root.render(
  <React.StrictMode>
    <Sentry.ErrorBoundary
      fallback={({ error, resetError }) => (
        <ErrorFallback error={error} resetError={resetError} />
      )}
      showDialog={false}
    >
      <App />
    </Sentry.ErrorBoundary>
  </React.StrictMode>
);

// Fallback UI quand ErrorBoundary catch une erreur React
function ErrorFallback({ error, resetError }: { error: unknown; resetError: () => void }) {
  return (
    <div
      role="alert"
      style={{
        minHeight: "100vh",
        background: "#0A0F1C",
        color: "#E8ECF4",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        padding: 24,
        fontFamily: "system-ui, sans-serif",
      }}
    >
      <h1 style={{ fontSize: 18, marginBottom: 8 }}>Une erreur est survenue</h1>
      <p style={{ fontSize: 13, color: "#8A95AD", marginBottom: 20, textAlign: "center" }}>
        L'équipe technique a été notifiée. Vous pouvez réessayer.
      </p>
      <button
        onClick={resetError}
        style={{
          padding: "10px 20px",
          background: "#26D9C4",
          color: "#0A0F1C",
          border: "none",
          borderRadius: 8,
          fontSize: 14,
          fontWeight: 600,
          cursor: "pointer",
        }}
      >
        Réessayer
      </button>
    </div>
  );
}
