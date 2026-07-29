package dev.voile.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dev.voile.tunnel.VoileSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "voile_prefs")

/**
 * Préférences locales Voile.
 * Équivalent Android du hook `usePersistedState` de la PWA.
 */
class VoilePrefs(private val context: Context) {

    private object Keys {
        val SELECTED_SERVER = intPreferencesKey("selected_server")
        val FAVORITES = stringSetPreferencesKey("favorites")
        val SETTINGS_JSON = stringPreferencesKey("settings_json")
        val WARP_INFO_JSON = stringPreferencesKey("warp_info_json")
        val REAL_IP = stringPreferencesKey("real_ip")
    }

    // --- Selected server ---

    val selectedServerFlow: Flow<Int> =
        context.dataStore.data.map { it[Keys.SELECTED_SERVER] ?: 1 }

    suspend fun setSelectedServer(id: Int) {
        context.dataStore.edit { it[Keys.SELECTED_SERVER] = id }
    }

    // --- Favorites ---

    val favoritesFlow: Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            prefs[Keys.FAVORITES]?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
        }

    suspend fun toggleFavorite(serverId: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITES] ?: emptySet()
            val idStr = serverId.toString()
            prefs[Keys.FAVORITES] = if (idStr in current) current - idStr else current + idStr
        }
    }

    suspend fun setFavorites(ids: Set<Int>) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FAVORITES] = ids.map { it.toString() }.toSet()
        }
    }

    // --- Settings (sérialisé JSON) ---

    val settingsFlow: Flow<VoileSettings> =
        context.dataStore.data.map { prefs ->
            prefs[Keys.SETTINGS_JSON]?.let {
                runCatching { Json.decodeFromString<VoileSettings>(it) }.getOrNull()
            } ?: VoileSettings()
        }

    suspend fun saveSettings(settings: VoileSettings) {
        context.dataStore.edit { it[Keys.SETTINGS_JSON] = Json.encodeToString(settings) }
    }

    // --- Warp info (IP, colo, etc.) ---

    val warpInfoFlow: Flow<WarpInfoData?> =
        context.dataStore.data.map { prefs ->
            prefs[Keys.WARP_INFO_JSON]?.let {
                runCatching { Json.decodeFromString<WarpInfoData>(it) }.getOrNull()
            }
        }

    suspend fun saveWarpInfo(info: WarpInfoData?) {
        context.dataStore.edit { prefs ->
            if (info == null) prefs.remove(Keys.WARP_INFO_JSON)
            else prefs[Keys.WARP_INFO_JSON] = Json.encodeToString(info)
        }
    }

    // --- Real IP (pour comparaison avant/après) ---

    val realIpFlow: Flow<String?> =
        context.dataStore.data.map { it[Keys.REAL_IP] }

    suspend fun saveRealIp(ip: String?) {
        context.dataStore.edit { prefs ->
            if (ip == null) prefs.remove(Keys.REAL_IP)
            else prefs[Keys.REAL_IP] = ip
        }
    }
}

@kotlinx.serialization.Serializable
data class WarpInfoData(
    val ip: String,
    val colo: String,
    val downloadMbps: Double,
    val uploadMbps: Double,
    val sessionDurationSec: Long,
    val startedAt: Long,
)
