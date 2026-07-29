package dev.voile.tunnel

import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.voile.tunnel.TrustScoreCalculator.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests d'intégration pour le calculateur Trust Score.
 * Couvre les scénarios réalistes d'usage.
 */
@RunWith(AndroidJUnit4::class)
class TrustScoreIntegrationTest {

    @Test
    fun `scenario_optimalCloudflareConnection`() {
        // Connexion WARP+ optimale : < 50ms, DNS chiffrés
        val r = calculate(Inputs(
            warpOn = true, latencyMs = 30L,
            geolocExposed = false, webrtcMitigated = true,
        ))
        assertEquals(Grade.A, r.grade)
        assertEquals(100, r.score)
        assertTrue("Aucun problème attendu", r.issues.isEmpty())
    }

    @Test
    fun `scenario_typicalConnectionWithMinorIssues`() {
        // Connexion typique : 80ms, géoloc exposée
        val r = calculate(Inputs(
            warpOn = true, latencyMs = 80L,
            geolocExposed = true, webrtcMitigated = true,
        ))
        assertEquals(Grade.B, r.grade)
        assertEquals(85, r.score)
        assertEquals(1, r.issues.size)
        assertEquals("GEO_EXPOSED", r.issues[0].code)
    }

    @Test
    fun `scenario_degradedConnection`() {
        // Connexion dégradée : latence élevée + géoloc
        val r = calculate(Inputs(
            warpOn = true, latencyMs = 250L,
            geolocExposed = true, webrtcMitigated = false,
        ))
        assertEquals(Grade.C, r.grade)
        // 100 - 10 (moderate latency) - 15 (geo) - 25 (webrtc) = 50
        assertEquals(50, r.score)
    }

    @Test
    fun `scenario_dnsLeakCritical`() {
        // Fuite DNS : critique
        val r = calculate(Inputs(
            warpOn = false, latencyMs = 50L,
            geolocExposed = false, webrtcMitigated = true,
        ))
        assertEquals(Grade.C, r.grade)
        assertEquals(60, r.score)
        assertTrue(r.issues.any { it.severity == Severity.HIGH && it.code == "DNS_LEAK" })
    }

    @Test
    fun `scenario_offlineState`() {
        // Pas de réseau du tout
        val r = calculate(Inputs(
            warpOn = false, latencyMs = 0L,
            geolocExposed = false, webrtcMitigated = false,
            dnsTestFailed = true, latencyTestFailed = true,
        ))
        // 100 - 20 (dns fail) - 25 (webrtc) - 15 (latency fail) = 40
        assertEquals(40, r.score)
        assertEquals(Grade.D, r.grade)
    }

    @Test
    fun `issueSeverity_ordering`() {
        // Vérifie que les issues sont triées par sévérité
        val r = calculate(Inputs(
            warpOn = false, latencyMs = 400L,
            geolocExposed = true, webrtcMitigated = false,
        ))

        // Les HIGH doivent apparaître en premier
        val firstHigh = r.issues.indexOfFirst { it.severity == Severity.HIGH }
        val firstMedium = r.issues.indexOfFirst { it.severity == Severity.MEDIUM }
        val firstLow = r.issues.indexOfFirst { it.severity == Severity.LOW }

        if (firstHigh >= 0 && firstMedium >= 0) {
            assertTrue("HIGH doit apparaître avant MEDIUM", firstHigh < firstMedium)
        }
        if (firstMedium >= 0 && firstLow >= 0) {
            assertTrue("MEDIUM doit apparaître avant LOW", firstMedium < firstLow)
        }
    }
}
