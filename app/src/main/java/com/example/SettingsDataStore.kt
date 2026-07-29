package com.example

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "voile_settings")

class SettingsDataStore(private val context: Context) {

    private object PreferencesKeys {
        val PROTOCOL = stringPreferencesKey("protocol")
        val KILL_SWITCH = booleanPreferencesKey("kill_switch")
        val AUTO_CONNECT = booleanPreferencesKey("auto_connect")
        val CYBER_SEC = booleanPreferencesKey("cyber_sec")
        val SPLIT_TUNNELING = booleanPreferencesKey("split_tunneling")
        val SPLIT_APPS = stringSetPreferencesKey("split_apps")
        val SELECTED_SERVER_ID = intPreferencesKey("selected_server_id")
        val FAVORITES = stringSetPreferencesKey("favorites")
    }

    val settingsFlow: Flow<Settings> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            Settings(
                protocol = preferences[PreferencesKeys.PROTOCOL] ?: "WireGuard",
                killSwitch = preferences[PreferencesKeys.KILL_SWITCH] ?: true,
                autoConnect = preferences[PreferencesKeys.AUTO_CONNECT] ?: false,
                cyberSec = preferences[PreferencesKeys.CYBER_SEC] ?: true,
                splitTunneling = preferences[PreferencesKeys.SPLIT_TUNNELING] ?: false,
                splitApps = preferences[PreferencesKeys.SPLIT_APPS] ?: emptySet()
            )
        }

    val selectedServerIdFlow: Flow<Int> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[PreferencesKeys.SELECTED_SERVER_ID] ?: 1
        }

    val favoritesFlow: Flow<Set<Int>> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            val favStrings = preferences[PreferencesKeys.FAVORITES] ?: setOf("1", "4")
            favStrings.mapNotNull { it.toIntOrNull() }.toSet()
        }

    suspend fun updateSettings(update: (Settings) -> Settings) {
        context.settingsDataStore.edit { preferences ->
            val current = Settings(
                protocol = preferences[PreferencesKeys.PROTOCOL] ?: "WireGuard",
                killSwitch = preferences[PreferencesKeys.KILL_SWITCH] ?: true,
                autoConnect = preferences[PreferencesKeys.AUTO_CONNECT] ?: false,
                cyberSec = preferences[PreferencesKeys.CYBER_SEC] ?: true,
                splitTunneling = preferences[PreferencesKeys.SPLIT_TUNNELING] ?: false,
                splitApps = preferences[PreferencesKeys.SPLIT_APPS] ?: emptySet()
            )
            val next = update(current)
            preferences[PreferencesKeys.PROTOCOL] = next.protocol
            preferences[PreferencesKeys.KILL_SWITCH] = next.killSwitch
            preferences[PreferencesKeys.AUTO_CONNECT] = next.autoConnect
            preferences[PreferencesKeys.CYBER_SEC] = next.cyberSec
            preferences[PreferencesKeys.SPLIT_TUNNELING] = next.splitTunneling
            preferences[PreferencesKeys.SPLIT_APPS] = next.splitApps
        }
    }

    suspend fun saveSelectedServerId(id: Int) {
        context.settingsDataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_SERVER_ID] = id
        }
    }

    suspend fun toggleFavorite(serverId: Int) {
        context.settingsDataStore.edit { preferences ->
            val currentFavs = preferences[PreferencesKeys.FAVORITES] ?: setOf("1", "4")
            val serverIdStr = serverId.toString()
            val nextFavs = if (currentFavs.contains(serverIdStr)) {
                currentFavs - serverIdStr
            } else {
                currentFavs + serverIdStr
            }
            preferences[PreferencesKeys.FAVORITES] = nextFavs
        }
    }
}
