package com.example

import org.junit.Assert.*
import org.junit.Test

/**
 * Tests JVM du Trust Score Kotlin.
 * Miroir exact de packages/core/src/trust-score/index.test.ts
 * pour garantir la parité algorithmique Web ↔ Android.
 *
 * Run : ./gradlew test
 */
class TrustScoreCalculatorTest {

    private val optimal = TrustScoreCalculator.Inputs(
        warpOn = true,
        latencyMs = 50,
        geolocExposed = false,
        webrtcMitigated = true
    )

    // ── Scores & grades ───────────────────────────────────────────────────────

    @Test fun `optimal retourne 100 et grade A`() {
        val r = TrustScoreCalculator.calculate(optimal)
        assertEquals(100, r.score)
        assertEquals("A", r.grade)
        assertTrue(r.issues.isEmpty())
    }

    @Test fun `grade B entre 75 et 89`() {
        val r = TrustScoreCalculator.calculate(optimal.copy(geolocExposed = true))
        assertEquals(85, r.score) // 100 - 15
        assertEquals("B", r.grade)
    }

    @Test fun `grade C entre 60 et 74`() {
        // HIGH_LATENCY(-20) + GEO(-15) = -35 → 65
        val r = TrustScoreCalculator.calculate(
            optimal.copy(latencyMs = 400, geolocExposed = true)
        )
        assertEquals(65, r.score)
        assertEquals("C", r.grade)
    }

    @Test fun `grade D quand tout echoue`() {
        val r = TrustScoreCalculator.calculate(
            TrustScoreCalculator.Inputs(
                warpOn = false, latencyMs = 400,
                geolocExposed = true, webrtcMitigated = false
            )
        )
        assertEquals(0, r.score)
        assertEquals("D", r.grade)
    }

    @Test fun `score clamp a 0 jamais negatif`() {
        val r = TrustScoreCalculator.calculate(
            TrustScoreCalculator.Inputs(
                warpOn = false, latencyMs = 1000,
                geolocExposed = true, webrtcMitigated = false
            )
        )
        assertTrue(r.score >= 0)
        assertEquals(0, r.score)
    }

    // ── DNS ───────────────────────────────────────────────────────────────────

    @Test fun `DNS_LEAK penalise de 40`() {
        val r = TrustScoreCalculator.calculate(optimal.copy(warpOn = false))
        assertTrue(r.issues.any { it.code == "DNS_LEAK" })
        assertEquals(60, r.score) // 100 - 40
    }

    @Test fun `DNS_TEST_FAILED penalise 20 et masque DNS_LEAK`() {
        val r = TrustScoreCalculator.calculate(
            optimal.copy(warpOn = false, dnsTestFailed = true)
        )
        assertTrue(r.issues.any { it.code == "DNS_TEST_FAILED" })
        assertFalse(r.issues.any { it.code == "DNS_LEAK" })
        assertEquals(80, r.score) // 100 - 20
    }

    // ── WebRTC ────────────────────────────────────────────────────────────────

    @Test fun `WEBRTC_EXPOSED penalise de 25`() {
        val r = TrustScoreCalculator.calculate(optimal.copy(webrtcMitigated = false))
        assertTrue(r.issues.any { it.code == "WEBRTC_EXPOSED" })
        assertEquals(75, r.score) // 100 - 25
    }

    // ── Latence ───────────────────────────────────────────────────────────────

    @Test fun `pas de penalite sous 150ms`() {
        val r = TrustScoreCalculator.calculate(optimal.copy(latencyMs = 100))
        assertFalse(r.issues.any { it.code.contains("LATENCY") })
        assertEquals(100, r.score)
    }

    @Test fun `MODERATE_LATENCY entre 150 et 300ms`() {
        val r = TrustScoreCalculator.calculate(optimal.copy(latencyMs = 200))
        assertTrue(r.issues.any { it.code == "MODERATE_LATENCY" })
        assertEquals(90, r.score) // 100 - 10
    }

    @Test fun `HIGH_LATENCY au-dessus de 300ms`() {
        val r = TrustScoreCalculator.calculate(optimal.copy(latencyMs = 400))
        assertTrue(r.issues.any { it.code == "HIGH_LATENCY" })
        assertEquals(80, r.score) // 100 - 20
    }

    @Test fun `LATENCY_TEST_FAILED penalise 15 et masque HIGH_LATENCY`() {
        val r = TrustScoreCalculator.calculate(
            optimal.copy(latencyMs = 400, latencyTestFailed = true)
        )
        assertTrue(r.issues.any { it.code == "LATENCY_TEST_FAILED" })
        assertFalse(r.issues.any { it.code == "HIGH_LATENCY" })
        assertEquals(85, r.score) // 100 - 15
    }

    // ── Métadonnées ───────────────────────────────────────────────────────────

    @Test fun `colo propage si fourni`() {
        val r = TrustScoreCalculator.calculate(optimal.copy(colo = "CDG"))
        assertEquals("CDG", r.colo)
    }

    @Test fun `colo null si absent`() {
        val r = TrustScoreCalculator.calculate(optimal)
        assertNull(r.colo)
    }

    @Test fun `gradeLabel correct pour chaque grade`() {
        assertEquals("Excellent", TrustScoreCalculator.gradeLabel("A"))
        assertEquals("Bon",       TrustScoreCalculator.gradeLabel("B"))
        assertEquals("Moyen",     TrustScoreCalculator.gradeLabel("C"))
        assertEquals("Critique",  TrustScoreCalculator.gradeLabel("D"))
    }
}
