package com.example

import android.app.Application
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.jan.tennert.supabase.auth.auth
import io.github.jan.tennert.supabase.auth.providers.builtin.Email
import io.github.jan.tennert.supabase.auth.user.User
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Data classes
// ---------------------------------------------------------------------------

data class Server(
    val id: Int,
    val country: String,
    val city: String,
    val flag: String,
    val ping: Int,
    val load: Int
)

val SERVERS = listOf(
    Server(1, "France", "Paris", "🇫🇷", 12, 34),
    Server(2, "Pays-Bas", "Amsterdam", "🇳🇱", 19, 21),
    Server(3, "Allemagne", "Francfort", "🇩🇪", 24, 58),
    Server(4, "Maroc", "Casablanca", "🇲🇦", 8, 42),
    Server(5, "Canada", "Montréal", "🇨🇦", 87, 15),
    Server(6, "Singapour", "Singapour", "🇸🇬", 145, 63),
    Server(7, "Japon", "Tokyo", "🇯🇵", 168, 29)
)

data class Telemetry(
    val ip: String = "—.—.—.—",
    val realIp: String = "",
    val down: String = "0.0",
    val up: String = "0.0",
    val session: Int = 0,
    val downHistory: List<Float> = List(20) { 0f },
    val upHistory: List<Float> = List(20) { 0f },
    val totalDown: Float = 0f,
    val totalUp: Float = 0f,
    val _rawDown: Float = 20f,
    val _rawUp: Float = 5f
)

data class Settings(
    val protocol: String = "WireGuard",
    val killSwitch: Boolean = true,
    val autoConnect: Boolean = false,
    val cyberSec: Boolean = true,
    val splitTunneling: Boolean = false,
    val splitApps: Set<String> = emptySet()
)

enum class ConnectionState {
    Disconnected, Connecting, Connected, Error
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun jitter(prev: Float, min: Float, max: Float, step: Float = 0.35f): Float {
    val delta = (Random.nextFloat() - 0.5f) * 2f * step * (max - min)
    return (prev + delta).coerceIn(min, max)
}

private fun randomIp(): String {
    val seg = { Random.nextInt(1, 255) }
    return "${seg()}.${seg()}.${seg()}.${seg()}"
}

// ---------------------------------------------------------------------------
// ViewModel
// ---------------------------------------------------------------------------

class VoileViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = SettingsDataStore(application)

    // Connection state (in-memory)
    private val _connectionState = MutableStateFlow(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _needsVpnPermission = MutableStateFlow<Intent?>(null)
    val needsVpnPermission: StateFlow<Intent?> = _needsVpnPermission.asStateFlow()

    // Telemetry (in-memory, reset on connect/disconnect)
    private val _telemetry = MutableStateFlow(Telemetry())
    val telemetry: StateFlow<Telemetry> = _telemetry.asStateFlow()

    // Persistent state backed by DataStore
    val selectedServerId: StateFlow<Int> = dataStore.selectedServerIdFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 1)

    val settings: StateFlow<Settings> = dataStore.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, Settings())

    val favorites: StateFlow<Set<Int>> = dataStore.favoritesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, setOf(1, 4))

    // Supabase Auth states
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private var connectJob: Job? = null
    private var telemetryJob: Job? = null

    init {
        // Observe Supabase session
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.sessionFlow.collect { session ->
                    _currentUser.value = session?.user
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Observe real tunnel state from VoileTunnelService
        viewModelScope.launch {
            VoileTunnelService.tunnelState.collect { ts ->
                _connectionState.value = when (ts) {
                    is VoileTunnelService.TunnelState.Connected    -> ConnectionState.Connected
                    is VoileTunnelService.TunnelState.Connecting   -> ConnectionState.Connecting
                    is VoileTunnelService.TunnelState.Error        -> ConnectionState.Error
                    is VoileTunnelService.TunnelState.Disconnected -> ConnectionState.Disconnected
                }
            }
        }
    }

    // ---------------------------------------------------------------------------
    // Auth actions
    // ---------------------------------------------------------------------------

    suspend fun signIn(emailInput: String, passwordInput: String) {
        SupabaseClient.client.auth.signInWith(Email) {
            email = emailInput
            password = passwordInput
        }
    }

    suspend fun signUp(emailInput: String, passwordInput: String) {
        SupabaseClient.client.auth.signUpWith(Email) {
            email = emailInput
            password = passwordInput
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                SupabaseClient.client.auth.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ---------------------------------------------------------------------------
    // Connection Session
    // ---------------------------------------------------------------------------

    private fun startSession() {
        val vpnIp = randomIp()
        val realIp = randomIp()
        _telemetry.update {
            it.copy(
                ip = vpnIp,
                realIp = realIp,
                session = 0,
                down = "0.0",
                up = "0.0",
                downHistory = List(20) { 0f },
                upHistory = List(20) { 0f },
                totalDown = 0f,
                totalUp = 0f,
                _rawDown = 20f,
                _rawUp = 5f
            )
        }
        telemetryJob?.cancel()
        telemetryJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _telemetry.update {
                    val newRawDown = jitter(it._rawDown, 2f, 180f, step = 0.3f)
                    val newRawUp = jitter(it._rawUp, 0.5f, 40f, step = 0.3f)
                    it.copy(
                        down = String.format("%.1f", newRawDown),
                        up = String.format("%.1f", newRawUp),
                        session = it.session + 1,
                        downHistory = it.downHistory.drop(1) + newRawDown,
                        upHistory = it.upHistory.drop(1) + newRawUp,
                        totalDown = it.totalDown + (newRawDown / 8f / 1000f), // MB
                        totalUp = it.totalUp + (newRawUp / 8f / 1000f),
                        _rawDown = newRawDown,
                        _rawUp = newRawUp
                    )
                }
            }
        }
    }

    private fun clearTimers() {
        connectJob?.cancel()
        telemetryJob?.cancel()
    }

    // ---------------------------------------------------------------------------
    // Public actions
    // ---------------------------------------------------------------------------

    fun toggleConnect() {
        when (_connectionState.value) {
            ConnectionState.Disconnected, ConnectionState.Error -> {
                val intent = VpnService.prepare(getApplication())
                if (intent != null) {
                    _needsVpnPermission.value = intent
                } else {
                    startConnectingProcess()
                }
            }
            ConnectionState.Connecting -> {
                clearTimers()
                _connectionState.value = ConnectionState.Disconnected
            }
            ConnectionState.Connected -> {
                clearTimers()
                VoileTunnelService.stop(getApplication())
                _telemetry.value = Telemetry()
            }
        }
    }

    private fun startConnectingProcess() {
        val ctx = getApplication<Application>()
        val splitApps = settings.value.splitApps.toList()
        VoileTunnelService.start(ctx, splitApps)
        // La télémétrie de simulation démarre en parallèle
        connectJob?.cancel()
        connectJob = viewModelScope.launch {
            delay(1800)
            startSession()
        }
    }

    fun onVpnPermissionGranted() {
        _needsVpnPermission.value = null
        startConnectingProcess()
    }

    fun onVpnPermissionDenied() {
        _needsVpnPermission.value = null
        _connectionState.value = ConnectionState.Error
    }

    fun selectServer(id: Int) {
        viewModelScope.launch {
            dataStore.saveSelectedServerId(id)
        }
        if (_connectionState.value == ConnectionState.Connected) {
            clearTimers()
            _connectionState.value = ConnectionState.Connecting
            connectJob = viewModelScope.launch {
                delay(1200)
                _connectionState.value = ConnectionState.Connected
                startSession()
            }
        }
    }

    fun toggleFavorite(id: Int) {
        viewModelScope.launch {
            dataStore.toggleFavorite(id)
        }
    }

    fun updateSettings(update: (Settings) -> Settings) {
        viewModelScope.launch {
            dataStore.updateSettings(update)
        }
    }

    override fun onCleared() {
        super.onCleared()
        clearTimers()
    }
}
