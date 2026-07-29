package dev.voile.lib

import io.sentry.Sentry
import io.sentry.SentryLevel

/**
 * Log une erreur métier avec contexte Voile.
 */
fun logError(
    throwable: Throwable,
    feature: String? = null,
    action: String? = null,
    serverId: Int? = null,
    extra: Map<String, Any>? = null,
) {
    // Console
    System.err.println("[Voile] ${feature ?: "unknown"}.${action ?: "unknown"}: $throwable")
    throwable.printStackTrace()

    // Sentry
    Sentry.withScope { scope ->
        feature?.let { scope.setTag("feature", it) }
        action?.let { scope.setTag("action", it) }
        serverId?.let { scope.setTag("server_id", it.toString()) }

        if (extra != null) {
            scope.setExtras(extra)
        }

        scope.level = SentryLevel.ERROR
        Sentry.captureException(throwable)
    }
}

/**
 * Log un breadcrumb (info).
 */
fun logInfo(message: String, data: Map<String, Any>? = null) {
    Sentry.addBreadcrumb(
        io.sentry.Breadcrumb().apply {
            this.message = message
            this.level = SentryLevel.INFO
            if (data != null) this.data = data
        }
    )
}

/**
 * Démarre une transaction de performance.
 */
inline fun <T> trackPerformance(
    name: String,
    operation: String = "function",
    block: () -> T,
): T {
    return Sentry.startTransaction(name, operation).let { transaction ->
        try {
            block()
        } finally {
            transaction.finish()
        }
    }
}
