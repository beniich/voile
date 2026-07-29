package com.example

import kotlin.math.max

/**
 * Port Kotlin de packages/core/src/trust-score/index.ts
 * Algorithme identique — seuls les I/O changent (pas de Date.now, pas de fetch).
 * Tests : TrustScoreCalculatorTest.kt
 */
object TrustScoreCalculator {

    // ── Pénalités (miroir des constantes TS) ──────────────────────────────────
    private const val PENALTY_DNS_LEAK            = 40
    private const val PENALTY_DNS_TEST_FAILED     = 20
    private const val PENALTY_WEBRTC_EXPOSED      = 25
    private const val PENALTY_GEO_EXPOSED         = 15
    private const val PENALTY_HIGH_LATENCY        = 20
    private const val PENALTY_MODERATE_LATENCY    = 10
    private const val PENALTY_LATENCY_TEST_FAILED = 15

    // ── Types ─────────────────────────────────────────────────────────────────

    data class Inputs(
        val warpOn: Boolean,
        val latencyMs: Long,
        val geolocExposed: Boolean,
        val webrtcMitigated: Boolean,
        val dnsTestFailed: Boolean = false,
        val latencyTestFailed: Boolean = false,
        val colo: String? = null
    )

    data class Result(
        val score: Int,
        val grade: String,      // "A" | "B" | "C" | "D"
        val issues: List<Issue>,
        val warpOn: Boolean,
        val colo: String?,
        val latencyMs: Long,
        val timestamp: Long = System.currentTimeMillis()
    )

    data class Issue(
        val code: String,
        val severity: String,   // "high" | "medium" | "low"
        val message: String
    )

    // ── Algorithme ────────────────────────────────────────────────────────────

    fun calculate(inputs: Inputs): Result {
        var score = 100
        val issues = mutableListOf<Issue>()

        // ① DNS / WARP
        when {
            inputs.dnsTestFailed -> {
                score -= PENALTY_DNS_TEST_FAILED
                issues += Issue(
                    "DNS_TEST_FAILED", "medium",
                    "Impossible de vérifier le routage DNS"
                )
            }
            !inputs.warpOn -> {
                score -= PENALTY_DNS_LEAK
                issues += Issue(
                    "DNS_LEAK", "high",
                    "Les requêtes DNS ne passent pas par Cloudflare WARP"
                )
            }
        }

        // ② WebRTC
        if (!inputs.webrtcMitigated) {
            score -= PENALTY_WEBRTC_EXPOSED
            issues += Issue(
                "WEBRTC_EXPOSED", "high",
                "WebRTC peut exposer votre IP réelle aux sites visités"
            )
        }

        // ③ Géolocalisation
        if (inputs.geolocExposed) {
            score -= PENALTY_GEO_EXPOSED
            issues += Issue(
                "GEO_EXPOSED", "low",
                "L'API Geolocation est accessible pour les sites visités"
            )
        }

        // ④ Latence
        when {
            inputs.latencyTestFailed -> {
                score -= PENALTY_LATENCY_TEST_FAILED
                issues += Issue(
                    "LATENCY_TEST_FAILED", "medium",
                    "Test de latence inaccessible — réseau potentiellement instable"
                )
            }
            inputs.latencyMs > 300 -> {
                score -= PENALTY_HIGH_LATENCY
                issues += Issue(
                    "HIGH_LATENCY", "medium",
                    "Latence très élevée : ${inputs.latencyMs} ms"
                )
            }
            inputs.latencyMs > 150 -> {
                score -= PENALTY_MODERATE_LATENCY
                issues += Issue(
                    "MODERATE_LATENCY", "low",
                    "Latence moyenne : ${inputs.latencyMs} ms"
                )
            }
        }

        val finalScore = max(0, score)
        return Result(
            score    = finalScore,
            grade    = scoreToGrade(finalScore),
            issues   = issues,
            warpOn   = inputs.warpOn,
            colo     = inputs.colo,
            latencyMs = inputs.latencyMs
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun scoreToGrade(score: Int): String = when {
        score >= 90 -> "A"
        score >= 75 -> "B"
        score >= 60 -> "C"
        else        -> "D"
    }

    fun gradeLabel(grade: String): String = when (grade) {
        "A"  -> "Excellent"
        "B"  -> "Bon"
        "C"  -> "Moyen"
        else -> "Critique"
    }
}
