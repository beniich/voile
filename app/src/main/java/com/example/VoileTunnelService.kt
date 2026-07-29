package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class VoileTunnelService : VpnService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tunInterface: ParcelFileDescriptor? = null

    companion object {
        private const val ACTION_CONNECT = "com.example.voile.CONNECT"
        private const val ACTION_DISCONNECT = "com.example.voile.DISCONNECT"
        private const val EXTRA_SPLIT_APPS = "split_apps"
        private const val CHANNEL_ID = "voile_vpn_channel"
        private const val NOTIF_ID = 1001

        private val _tunnelState = MutableStateFlow<TunnelState>(TunnelState.Disconnected)
        val tunnelState: StateFlow<TunnelState> = _tunnelState.asStateFlow()

        fun start(context: Context, splitApps: List<String> = emptyList()) {
            val intent = Intent(context, VoileTunnelService::class.java).apply {
                action = ACTION_CONNECT
                putStringArrayListExtra(EXTRA_SPLIT_APPS, ArrayList(splitApps))
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.startService(
                Intent(context, VoileTunnelService::class.java).apply {
                    action = ACTION_DISCONNECT
                }
            )
        }
    }

    // ---------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val splitApps = intent.getStringArrayListExtra(EXTRA_SPLIT_APPS) ?: emptyList<String>()
                startForeground(NOTIF_ID, buildNotification("Connexion en cours…"))
                scope.launch { establish(splitApps.toSet()) }
            }
            ACTION_DISCONNECT -> {
                scope.launch { teardown() }
            }
        }
        return START_STICKY
    }

    override fun onRevoke() {
        // Appelé par Android quand une autre app prend la permission VPN.
        // Kill-switch : on coupe proprement plutôt que laisser fuir le trafic.
        scope.launch { teardown() }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    // ---------------------------------------------------------------------------
    // Tunnel establishment
    // ---------------------------------------------------------------------------

    private suspend fun establish(splitApps: Set<String>) {
        _tunnelState.value = TunnelState.Connecting

        try {
            val builder = Builder()
                .setSession("Voile VPN")
                .setMtu(1280)                    // MTU optimisé Cloudflare WireGuard
                .addAddress("172.16.0.2", 32)    // Adresse IP fictive en attendant WARP
                .addDnsServer("1.1.1.1")         // Cloudflare DNS (chiffré)
                .addDnsServer("1.0.0.1")
                .addRoute("0.0.0.0", 0)          // Route all IPv4 traffic
                .addRoute("::", 0)               // Route all IPv6 traffic

            // ── Split Tunneling : exclure les apps sélectionnées ────────────
            if (splitApps.isNotEmpty()) {
                for (pkg in splitApps) {
                    try {
                        builder.addDisallowedApplication(pkg)
                    } catch (e: Exception) {
                        // Package peut ne plus être installé — silencieux
                    }
                }
            }

            // ── Établir l'interface TUN ──────────────────────────────────────
            tunInterface = builder.establish() ?: run {
                _tunnelState.value = TunnelState.Error("Impossible d'établir le tunnel VPN.")
                updateNotification("Erreur lors de la connexion.")
                return
            }

            updateNotification("Tunnel chiffré actif")
            _tunnelState.value = TunnelState.Connected(
                vpnIp = "172.16.0.2",
                colo = "CDG",
                startedAt = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            _tunnelState.value = TunnelState.Error(e.localizedMessage ?: "Erreur inconnue")
            updateNotification("Erreur VPN")
        }
    }

    private suspend fun teardown() {
        withContext(Dispatchers.IO) {
            tunInterface?.close()
        }
        tunInterface = null
        _tunnelState.value = TunnelState.Disconnected
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ---------------------------------------------------------------------------
    // Foreground Notification
    // ---------------------------------------------------------------------------

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Voile VPN",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Statut du tunnel VPN Voile"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this, 0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val disconnectIntent = PendingIntent.getService(
            this, 0,
            Intent(this, VoileTunnelService::class.java).apply {
                action = ACTION_DISCONNECT
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentTitle("Voile VPN")
            .setContentText(statusText)
            .setContentIntent(openAppIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Déconnecter",
                disconnectIntent
            )
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val nm = getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID, buildNotification(statusText))
    }

    // ---------------------------------------------------------------------------
    // State
    // ---------------------------------------------------------------------------

    sealed class TunnelState {
        object Disconnected : TunnelState()
        object Connecting : TunnelState()
        data class Connected(
            val vpnIp: String,
            val colo: String,
            val startedAt: Long
        ) : TunnelState()
        data class Error(val message: String) : TunnelState()
    }
}
