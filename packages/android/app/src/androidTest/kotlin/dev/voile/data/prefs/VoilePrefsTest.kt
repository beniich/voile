package dev.voile.data.prefs

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.voile.tunnel.VoileSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoilePrefsTest {

    private val prefs = VoilePrefs(ApplicationProvider.getApplicationContext())

    @Test
    fun `selectedServer_persistsAcrossReads`() = runTest {
        prefs.setSelectedServer(5)
        val read = prefs.selectedServerFlow.first()
        assertEquals(5, read)
    }

    @Test
    fun `toggleFavorite_addsAndRemoves`() = runTest {
        val serverId = 42
        prefs.toggleFavorite(serverId)
        assertTrue(prefs.favoritesFlow.first().contains(serverId))

        prefs.toggleFavorite(serverId)
        assertFalse(prefs.favoritesFlow.first().contains(serverId))
    }

    @Test
    fun `settings_serializeAndDeserialize`() = runTest {
        val custom = VoileSettings(
            protocol = "OpenVPN",
            killSwitch = false,
            autoConnect = true,
            cyberSec = false,
            splitTunneling = true,
            splitApps = listOf("com.browser", "com.streaming"),
        )
        prefs.saveSettings(custom)
        val read = prefs.settingsFlow.first()
        assertEquals(custom, read)
    }

    @Test
    fun `warpInfo_persistsWithJson`() = runTest {
        val info = WarpInfoData(
            ip = "1.2.3.4", colo = "PAR",
            downloadMbps = 50.0, uploadMbps = 10.0,
            sessionDurationSec = 300L, startedAt = 1234567890L,
        )
        prefs.saveWarpInfo(info)
        val read = prefs.warpInfoFlow.first()
        assertEquals(info, read)
    }

    @Test
    fun `warpInfo_nullRemovesEntry`() = runTest {
        prefs.saveWarpInfo(null)
        assertNull(prefs.warpInfoFlow.first())
    }
}
