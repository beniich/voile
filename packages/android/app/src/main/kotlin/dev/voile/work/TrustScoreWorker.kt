package dev.voile.work

import android.content.Context
import androidx.work.*
import dev.voile.data.auth.AuthRepository
import dev.voile.core.supabase.SupabaseClient
import dev.voile.data.warp.WarpConfigRepository
import dev.voile.lib.logError
import dev.voile.lib.logInfo
import dev.voile.tunnel.TrustScoreCalculator
import io.sentry.Sentry
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

/**
 * Worker périodique qui calcule le Trust Score et le log dans Supabase.
 *
 * Planifié par VoileViewModel quand le tunnel devient actif.
 * Exécuté toutes les 15 minutes en background.
 */
class TrustScoreWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    private val warpRepo = WarpConfigRepository()

    override suspend fun doWork(): Result {
        return try {
            logInfo("TrustScoreWorker started")

            // Mesures avec timeout (ne pas bloquer le worker > 30s)
            val warpOn = withTimeoutOrNull(5_000) { warpRepo.isWarpActive() } ?: false
            val latencyMs = withTimeoutOrNull(5_000) { warpRepo.measureLatency() } ?: -1L
            val geolocExposed = isGeolocExposed(applicationContext)
            val webrtcMitigated = false // Pas de WebRTC sur Android

            val result = TrustScoreCalculator.calculate(
                TrustScoreCalculator.Inputs(
                    warpOn = warpOn,
                    latencyMs = latencyMs.coerceAtLeast(0),
                    geolocExposed = geolocExposed,
                    webrtcMitigated = webrtcMitigated,
                    dnsTestFailed = !warpOn && latencyMs < 0,
                    latencyTestFailed = latencyMs < 0,
                )
            )

            logInfo("TrustScore calculated", mapOf(
                "score" to result.score,
                "grade" to result.grade.name,
                "warp_on" to warpOn,
                "latency_ms" to latencyMs,
                "issues_count" to result.issues.size,
            ))

            // Log dans Supabase si authentifié (best effort)
            try {
                logTrustScoreToSupabase(result, latencyMs, warpOn)
            } catch (e: Throwable) {
                // Silent fail : on a le score local, c'est OK
                Sentry.addBreadcrumb("trust_score.supabase_log_failed")
            }

            Result.success(
                workDataOf(
                    "score" to result.score,
                    "grade" to result.grade.name,
                )
            )
        } catch (e: Throwable) {
            logError(e, feature = "trust_score", action = "worker")
            // Ne pas retry indéfiniment (peut flood Sentry si Cloudflare down)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun isGeolocExposed(context: Context): Boolean {
        return try {
            val pm = context.packageManager
            pm.hasSystemFeature(android.content.pm.PackageManager.FEATURE_LOCATION_GPS)
        } catch (e: Throwable) {
            false
        }
    }

    private suspend fun logTrustScoreToSupabase(
        result: TrustScoreCalculator.Result,
        latencyMs: Long,
        warpOn: Boolean,
    ) {
        val authRepo = AuthRepository(SupabaseClient.instance)
        val token = authRepo.getAccessToken() ?: return // Pas authentifié = skip

        val payload = mapOf(
            "p_score" to result.score,
            "p_grade" to result.grade.name,
            "p_issues" to Json.encodeToString(
                result.issues.map { issue ->
                    mapOf(
                        "code" to issue.code,
                        "severity" to issue.severity.name.lowercase(),
                        "message" to issue.message,
                    )
                }
            ),
            "p_latency_ms" to latencyMs.toInt(),
            "p_warp_on" to warpOn,
            "p_colo" to "",
        )

        SupabaseClient.instance.postgrest.rpc(
            function = "log_trust_score",
            parameters = payload,
        )
    }

    companion object {
        const val WORK_NAME = "trust_score_worker"

        /**
         * Planifie le worker périodique (toutes les 15 min).
         */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<TrustScoreWorker>(
                15, TimeUnit.MINUTES,
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS,
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        /**
         * Annule le worker.
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
