import * as Sentry from "@sentry/react";
import { browserTracingIntegration } from "@sentry/react";

// @ts-ignore
const SENTRY_DSN = import.meta.env?.VITE_SENTRY_DSN;
// @ts-ignore
const APP_VERSION = import.meta.env?.VITE_APP_VERSION || "dev";
// @ts-ignore
const ENV = import.meta.env?.MODE; // 'development' | 'production' | 'preview'

// Pas de DSN = pas d'instrumentation (dev local, tests)
if (!SENTRY_DSN) {
  console.info("[Voile] Sentry désactivé (pas de DSN)");
} else {
  Sentry.init({
    dsn: SENTRY_DSN,

    // Environnement (production, staging, preview-N, etc.)
    environment: ENV,

    // Release tracking — doit matcher le SENTRY_RELEASE à la build
    release: `voile-web@${APP_VERSION}`,

    // Sample rates
    tracesSampleRate: ENV === "production" ? 0.1 : 1.0, // 10% en prod
    replaysSessionSampleRate: 0, // Pas de session replay par défaut
    replaysOnErrorSampleRate: ENV === "production" ? 1.0 : 0, // 100% si erreur

    // Privacy : ne JAMAIS envoyer d'IP, email, ou user ID
    sendDefaultPii: false,

    // Performance : exclut les requêtes locales
    tracePropagationTargets: [
      "localhost",
      /^https:\/\/.*\.supabase\.co\//,
      /^https:\/\/1\.1\.1\.1\//,
      /^https:\/\/api\.ipify\.org\//,
    ],

    // Filtre le bruit (erreurs navigateur non-critiques)
    ignoreErrors: [
      // Extensions navigateur qui polluent les logs
      "ResizeObserver loop limit exceeded",
      "Non-Error promise rejection captured",
      // Network errors navigateurs (déjà gérés par retry)
      "Failed to fetch",
      "Network request failed",
      "Load failed",
      // Erreurs utilisateur sur bouton "annuler" d'un fetch
      "AbortError",
    ],

    // Filtre le bruit des URLs sensibles
    denyUrls: [
      // Chrome extensions
      /extensions\//i,
      /^chrome:\/\//i,
      /^moz-extension:\/\//i,
    ],

    // Hooks pour enrichir les events
    beforeSendTransaction(event) {
      // Drop les transactions health check / ping
      if (event.transaction === "/cdn-cgi/trace") return null;
      return event;
    },

    beforeSend(event, hint) {
      const error = hint.originalException;

      // Filtre les erreurs de DevTools
      if (error && typeof error === "object" && "message" in error) {
        const msg = String((error as Error).message);
        if (msg.includes("DevTools")) return null;
      }

      return event;
    },

    integrations: [
      Sentry.replayIntegration({
        maskAllText: true,        // Masque tout le texte
        blockAllMedia: true,      // Bloque screenshots
        maskAllInputs: true,      // Masque inputs
      }),
      browserTracingIntegration({
        // Trace les navigations SPA
        enableLongTask: true,
        enableInp: true,
      }),
    ],
  });
}

// Wrapper exporté pour typer proprement
export { Sentry };
