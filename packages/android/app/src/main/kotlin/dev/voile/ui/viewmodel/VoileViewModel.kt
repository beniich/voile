package dev.voile.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.VpnService
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import dev.voile.data.prefs.VoilePrefs
import dev.voile.data.prefs.WarpInfoData
import dev.voile.data.warp.WarpConfigRepository
import dev.voile.tunnel.VoileSettings
import dev.voile.tunnel.VoileTunnelService
import dev.voile.tunnel.WarpInfo
import dev.voile.tunnel.TunnelTelemetry
import dev.voile.ui.VoileTab
import dev.voile.work.TrustScoreWorker
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.TimeUnit

/**
 * ViewModel principal de l'app Voile.
 * Orchestre : tunnel, settings, serveurs, auth, télémétrie.
 */
class VoileViewModel(
    application: Application,
    private val prefs: VoilePrefs,
    private val warpRepo: WarpConfigRepository,
) : AndroidViewModel(application) {

    data class UiState(
        val currentTab: VoileTab = VoileTab.HOME,
        val selectedServerId: Int = 1,
        val favorites: Set<Int> = emptySet(),
        val settings: VoileSettings = VoileSettings(),
        val tunnelState: VoileTunnelService.TunnelState =
            VoileTunnelService.TunnelState.Disconnected,
        val currentServer: WarpInfo = WarpInfo(
            id = 1, country = "France", city = "Paris",
            flag = "🇫🇷", ping = 12, load = 34,
        ),
        val telemetry: TunnelTelemetry = TunnelTelemetry(
            ip = "—.—.—.—", colo = "—",
            downloadMbps = 0.0, uploadMbps = 0.0,
            sessionDurationSec = 0L,
        ),
        val realIp: String? = null,
        val pendingVpnIntent: Intent? = null,
        val error: String? = null,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val telemetryJob = AtomicReference<Job?>(null)
    private val sessionStartTime = AtomicLong(0L)

    init {
        observePreferences()
        observeTunnelState()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                prefs.selectedServerFlow,
                prefs.favoritesFlow,
                prefs.settingsFlow,
                prefs.warpInfoFlow,
                prefs.realIpFlow,
            ) { serverId, favorites, settings, warpInfo, realIp ->
                _uiState.update {
                    it.copy(
                        selectedServerId = serverId,
                        favorites = favorites,
                        settings = settings,
                        realIp = realIp,
                        telemetry = if (warpInfo != null) {
                            TunnelTelemetry(
                                ip = warpInfo.ip,
                                colo = warpInfo.colo,
                                downloadMbps = warpInfo.downloadMbps,
                                uploadMbps = warpInfo.uploadMbps,
                                sessionDurationSec =
                                    ((System.currentTimeMillis() - warpInfo.startedAt) / 1000L),
                            )
                        } else it.telemetry,
                    )
                }
            }.collect()
        }
    }

    private fun observeTunnelState() {
        viewModelScope.launch {
            VoileTunnelService.state.collect { state ->
                _uiState.update { it.copy(tunnelState = state) }
                when (state) {
                    is VoileTunnelService.TunnelState.Connected -> {
                        sessionStartTime.set(System.currentTimeMillis())
                        startTelemetryLoop()
                        scheduleTrustScore()
                    }
                    is VoileTunnelService.TunnelState.Disconnected,
                    is VoileTunnelService.TunnelState.Error -> {
                        stopTelemetryLoop()
                        cancelTrustScore()
                        prefs.saveWarpInfo(null)
                    }
                    else -> {}
                }
            }
        }
    }

    // --- Navigation ---

    fun selectTab(tab: VoileTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    // --- Server selection ---

    fun selectServer(serverId: Int) {
        viewModelScope.launch {
            prefs.setSelectedServer(serverId)
            // Si connecté, reconnecter au nouveau serveur
            if (_uiState.value.tunnelState is VoileTunnelService.TunnelState.Connected) {
                toggleConnection(getApplication())
            }
        }
    }

    fun toggleFavorite(serverId: Int) {
        viewModelScope.launch {
            prefs.toggleFavorite(serverId)
        }
    }

    // --- Settings ---

    fun updateSettings(settings: VoileSettings) {
        viewModelScope.launch {
            prefs.saveSettings(settings)
        }
    }

    fun openSplitTunneling() {
        // TODO : ouvrir la bottom-sheet de configuration
    }

    // --- Connection ---

    fun toggleConnection(context: android.content.Context) {
        when (_uiState.value.tunnelState) {
            is VoileTunnelService.TunnelState.Disconnected -> connect(context)
            is VoileTunnelService.TunnelState.Connecting -> cancelConnection()
            is VoileTunnelService.TunnelState.Connected -> disconnect()
            is VoileTunnelService.TunnelState.Error -> connect(context)
        }
    }

    private fun connect(context: android.content.Context) {
        // 1. Vérifie la permission VPN
        val vpnIntent = VpnService.prepare(context)
        if (vpnIntent != null) {
            _uiState.update { it.copy(pendingVpnIntent = vpnIntent) }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(error = null) }

                // 2. Récupère la config WARP
                val config = warpRepo.fetchConfig()

                // 3. Calcule les apps à exclure si split tunneling actif
                val splitApps = if (_uiState.value.settings.splitTunneling)
                    _uiState.value.settings.splitApps.toSet()
                else emptySet()

                // 4. Démarre le service
                VoileTunnelService.start(context, config, splitApps)

                // 5. Sauvegarde l'IP réelle pour comparaison
                captureRealIp()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Erreur de connexion : ${e.message}")
                }
            }
        }
    }

    fun onVpnPermissionGranted(context: android.content.Context) {
        _uiState.update { it.copy(pendingVpnIntent = null) }
        connect(context)
    }

    fun onVpnPermissionDenied() {
        _uiState.update { it.copy(pendingVpnIntent = null) }
    }

    private fun cancelConnection() {
        viewModelScope.launch {
            VoileTunnelService.stop(getApplication())
        }
    }

    private fun disconnect() {
        viewModelScope.launch {
            VoileTunnelService.stop(getApplication())
        }
    }

    private suspend fun captureRealIp() {
        try {
            val ip = withTimeout(5_000) {
                // Appel à un service d'IP publique (ex: ipify)
                kotlinx.coroutines.flow.flow {
                    val response = io.ktor.client.HttpClient()
                        .get("https://api.ipify.org?format=json")
                    val text = response.bodyAsText()
                    val regex = Regex("\"ip\":\"([^\"]+)\"")
                    val match = regex.find(text)
                    emit(match?.groupValues?.get(1) ?: "—")
                }.first()
            }
            prefs.saveRealIp(ip)
        } catch (e: Exception) {
            prefs.saveRealIp("—")
        }
    }

    // --- Telemetry loop ---

    private fun startTelemetryLoop() {
        telemetryJob.get()?.cancel()
        val job = viewModelScope.launch {
            while (isActive) {
                val elapsed = (System.currentTimeMillis() - sessionStartTime.get()) / 1000L
                val warpInfo = prefs.warpInfoFlow.first()
                _uiState.update {
                    it.copy(
                        telemetry = TunnelTelemetry(
                            ip = warpInfo?.ip ?: "via Cloudflare",
                            colo = warpInfo?.colo ?: "—",
                            downloadMbps = warpInfo?.downloadMbps ?: 0.0,
                            uploadMbps = warpInfo?.uploadMbps ?: 0.0,
                            sessionDurationSec = elapsed,
                        ),
                    )
                }
                delay(1000)
            }
        }
        telemetryJob.set(job)
    }

    private fun stopTelemetryLoop() {
        telemetryJob.get()?.cancel()
        telemetryJob.set(null)
    }

    // --- Trust Score scheduling ---

    private fun scheduleTrustScore() {
        val request = PeriodicWorkRequestBuilder<TrustScoreWorker>(
            15, TimeUnit.MINUTES,
        ).build()

        WorkManager.getInstance(getApplication()).enqueueUniquePeriodicWork(
            "trust_score",
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun cancelTrustScore() {
        WorkManager.getInstance(getApplication()).cancelUniqueWork("trust_score")
    }

    override fun onCleared() {
        super.onCleared()
        stopTelemetryLoop()
    }
}
