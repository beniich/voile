import * as Sentry from "@sentry/react";

interface ErrorContext {
  feature?: string;
  action?: string;
  server?: { id?: number; country?: string; city?: string };
  extra?: Record<string, unknown>;
}

/**
 * Log une erreur métier avec contexte Voile.
 * Envoie à Sentry si configuré, console.warn sinon (dev).
 */
export function logError(error: unknown, context: ErrorContext = {}): void {
  // Log console toujours (debug + sentry ingestion logs)
  console.error(`[Voile] ${context.feature ?? "unknown"}.${context.action ?? "unknown"}:`, error);

  // Sentry
  Sentry.withScope((scope) => {
    if (context.feature) scope.setTag("feature", context.feature);
    if (context.action) scope.setTag("action", context.action);
    if (context.server?.id) {
      scope.setContext("server", {
        id: context.server.id,
        country: context.server.country ?? "unknown",
        city: context.server.city ?? "unknown",
      });
    }
    if (context.extra) scope.setExtras(context.extra);
    scope.setLevel("error");
    Sentry.captureException(error);
  });
}

/**
 * Log un message informatif (non-erreur).
 */
export function logInfo(message: string, data?: Record<string, unknown>): void {
  Sentry.addBreadcrumb({
    message,
    data,
    level: "info",
  });
}

/**
 * Démarre une transaction de performance.
 */
export function startTransaction(name: string, op: string) {
  return Sentry.startSpan({ name, op }, () => {});
}
