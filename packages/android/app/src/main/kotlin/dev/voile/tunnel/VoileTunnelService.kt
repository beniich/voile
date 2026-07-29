package dev.voile.tunnel

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.wireguard.android.backend.*
import com.wireguard.config.*
import dev.voile.R
import dev.voile.lib.logError
import dev.voile.lib.logInfo
import dev.voile.lib.trackPerformance
import io.sentry.Sentry
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong

/**
 * Service VPN natif Voile.
 *
 * Cycle de vie :
 *   1. onCreate() → notification foreground
 *   2. ACTION_CONNECT → acquireWakeLock + fetch config + establish TUN + start WG
 *   3. Connected → heartbeat + telemetry continue
 *   4. ACTION_DISCONNECT → stop WG + close TUN + releaseWakeLock
 *   5. onRevoke() (kill switch OS) → teardown complet
 */
class VoileTunnelService : VpnService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var tunInterface: ParcelFileDescriptor? = null
    private var wgBackend: WireGuardBackend? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var heartbeatJob: Job? = null
    private val tunnelStartTime = AtomicLong(0L)

    companion object {
        private const val ACTION_CONNECT = "voile.CONNECT"
        private const val ACTION_DISCONNECT = "voile.DISCONNECT"
        private const val ACTION_HEARTBEAT = "voile.HEARTBEAT"
        private const val TUNNEL_NAME = "voile"
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_CHANNEL_ID = "voile_tunnel"
        private const val HEARTBEAT_INTERVAL_MS = 30_000L

        val state = MutableStateFlow<TunnelState>(TunnelState.Disconnected)

        fun start(context: Context, config: TunnelConfig, splitApps: Set<String>) {
            val intent = Intent(context, VoileTunnelService::class.java).apply {
                action = ACTION_CONNECT
                putExtra(EXTRA_PRIVATE_KEY, config.privateKey)
                putExtra(EXTRA_PUBLIC_KEY_SERVER, config.serverPublicKey)
                putExtra(EXTRA_ENDPOINT, config.endpoint)
                putExtra(EXTRA_ADDRESS_V4, config.addressV4)
                putExtra(EXTRA_ADDRESS_V6, config.addressV6)
                putExtra(EXTRA_DNS, config.dns)
                putExtra(EXTRA_MTU, config.mtu)
                putExtra(EXTRA_KEEPALIVE, config.persistentKeepalive)
                putStringArrayListExtra(EXTRA_SPLIT_APPS, ArrayList(splitApps))
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, VoileTunnelService::class.java).apply {
                action = ACTION_DISCONNECT
            }
            context.startService(intent)
        }
    }

    data class TunnelConfig(
        val privateKey: String,
        val serverPublicKey: String,
        val endpoint: String,
        val addressV4: String,
        val addressV6: String,
        val dns: String,
        val mtu: Int = 1280,
        val persistentKeepalive: Int = 25,
    )

    sealed class TunnelState {
        object Disconnected : TunnelState()
        object Connecting : TunnelState()
        data class Connected(val startedAt: Long) : TunnelState()
        data class Error(val message: String) : TunnelState()
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_CONNECT -> {
                val config = TunnelConfig(
                    privateKey = intent.getStringExtra(EXTRA_PRIVATE_KEY) ?: error("Missing privateKey"),
                    serverPublicKey = intent.getStringExtra(EXTRA_PUBLIC_KEY_SERVER) ?: error("Missing serverPublicKey"),
                    endpoint = intent.getStringExtra(EXTRA_ENDPOINT) ?: error("Missing endpoint"),
                    addressV4 = intent.getStringExtra(EXTRA_ADDRESS_V4) ?: error("Missing addressV4"),
                    addressV6 = intent.getStringExtra(EXTRA_ADDRESS_V6) ?: error("Missing addressV6"),
                    dns = intent.getStringExtra(EXTRA_DNS) ?: error("Missing dns"),
                    mtu = intent.getIntExtra(EXTRA_MTU, 1280),
                    persistentKeepalive = intent.getIntExtra(EXTRA_KEEPALIVE, 25),
                )
                val splitApps = intent.getStringArrayListExtra(EXTRA_SPLIT_APPS)?.toSet() ?: emptySet()
                scope.launch { establish(config, splitApps) }
            }
            ACTION_DISCONNECT -> {
                scope.launch { teardown("user_request") }
            }
            ACTION_HEARTBEAT -> {
                // Heartbeat reçu via WorkManager
                // Pas d'action ici, juste pour réveiller le service si nécessaire
            }
        }
        return START_STICKY
    }

    /**
     * Établit le tunnel complet : notification, wake lock, TUN, WireGuard.
     */
    private suspend fun establish(config: TunnelConfig, splitApps: Set<String>) {
        state.value = TunnelState.Connecting
        logInfo("Tunnel establish initiated", mapOf(
            "endpoint" to config.endpoint,
            "split_apps_count" to splitApps.size,
            "mtu" to config.mtu,
        ))

        // Sentry breadcrumb pour tracer le début
        Sentry.addBreadcrumb("vpn.connect.start", mapOf("endpoint" to config.endpoint))

        trackPerformance("tunnel.establish", "vpn.connect") {
            try {
                // 1. Notification foreground (obligatoire Android 8+)
                startForeground(NOTIFICATION_ID, buildNotification("Connexion en cours…"))

                // 2. Wake lock pour garder le CPU actif pendant le tunnel
                acquireWakeLock()

                // 3. Construit l'interface TUN
                val builder = Builder()
                    .setSession("Voile VPN")
                    .addAddress(config.addressV4, 32)
                    .addAddress(config.addressV6, 128)
                    .addDnsServer(config.dns)
                    .setMtu(config.mtu)
                    .setBlocking(true)
                    .setAllowFamily(android.system.OsConstants.AF_INET)
                    .setAllowFamily(android.system.OsConstants.AF_INET6)

                // Split tunneling
                if (splitApps.isNotEmpty()) {
                    splitApps.forEach { pkg ->
                        runCatching { builder.addDisallowedApplication(pkg) }
                            .onFailure { e ->
                                logError(e, feature = "tunnel", action = "splitTunneling",
                                    extra = mapOf("package" to pkg))
                            }
                    }
                } else {
                    // Aucun split tunneling → tout passe par le tunnel
                    // (pas de addAllowedApplication = tout autorisé)
                }

                // 4. Établit l'interface TUN
                tunInterface = try {
                    builder.establish() ?: throw IOException("Builder.establish() returned null")
                } catch (e: Throwable) {
                    state.value = TunnelState.Error("VPN establish failed: ${e.message}")
                    logError(e, feature = "tunnel", action = "tunEstablish")
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    releaseWakeLock()
                    return@trackPerformance
                }

                // 5. Configure WireGuard
                val wgConfig = try {
                    Config.Builder()
                        .setInterface(
                            Interface.Builder()
                                .parsePrivateKey(config.privateKey)
                                .addAddress(config.addressV4)
                                .addAddress(config.addressV6)
                                .addDnsServer(config.dns)
                                .setMtu(config.mtu)
                        )
                        .addPeer(
                            Peer.Builder()
                                .parsePublicKey(config.serverPublicKey)
                                .setEndpoint(InetEndpoint.parse(config.endpoint))
                                .setPersistentKeepalive(config.persistentKeepalive)
                                .addAllowedIp("0.0.0.0/0")
                                .addAllowedIp("::/0")
                        )
                        .build()
                } catch (e: Throwable) {
                    state.value = TunnelState.Error("WG config parse failed: ${e.message}")
                    logError(e, feature = "tunnel", action = "parseConfig",
                        extra = mapOf("private_key_fp" to config.privateKey.take(8)))
                    teardown("parse_error")
                    return@trackPerformance
                }

                // 6. Démarre le backend WireGuard (userspace Go)
                wgBackend = GoBackend(this)
                try {
                    wgBackend?.setState(TUNNEL_NAME, Tunnel.State.UP, wgConfig)
                } catch (e: Throwable) {
                    state.value = TunnelState.Error("WG backend failed: ${e.message}")
                    logError(e, feature = "tunnel", action = "wgBackend.setState")
                    teardown("backend_error")
                    return@trackPerformance
                }

                // 7. Tunnel actif !
                val startedAt = System.currentTimeMillis()
                tunnelStartTime.set(startedAt)
                state.value = TunnelState.Connected(startedAt)

                // 8. Met à jour la notification
                startForeground(NOTIFICATION_ID, buildNotification("Tunnel chiffré actif"))

                // 9. Démarre le heartbeat (vérifie que le tunnel est toujours vivant)
                startHeartbeat()

                logInfo("Tunnel established successfully", mapOf(
                    "endpoint" to config.endpoint,
                    "started_at" to startedAt,
                ))

                Sentry.addBreadcrumb("vpn.connect.success", mapOf("endpoint" to config.endpoint))
            } catch (e: Throwable) {
                state.value = TunnelState.Error("Unexpected error: ${e.message}")
                logError(e, feature = "tunnel", action = "establish")
                teardown("unexpected_error")
            }
        }
    }

    /**
     * Démarre un heartbeat qui vérifie périodiquement la santé du tunnel.
     */
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = scope.launch {
            while (isActive) {
                delay(HEARTBEAT_INTERVAL_MS)
                try {
                    val tunOk = tunInterface?.fileDescriptor?.valid() == true
                    val backendOk = wgBackend?.runningTunnelNames?.contains(TUNNEL_NAME) == true

                    if (!tunOk || !backendOk) {
                        logError(
                            RuntimeException("Tunnel health check failed"),
                            feature = "tunnel",
                            action = "heartbeat",
                            extra = mapOf(
                                "tun_valid" to tunOk,
                                "backend_running" to backendOk,
                            ),
                        )
                        teardown("health_check_failed")
                        break
                    }

                    val uptime = System.currentTimeMillis() - tunnelStartTime.get()
                    logInfo("Tunnel heartbeat ok", mapOf("uptime_ms" to uptime))
                } catch (e: Throwable) {
                    logError(e, feature = "tunnel", action = "heartbeat")
                }
            }
        }
    }

    /**
     * Détruit le tunnel proprement.
     */
    private suspend fun teardown(reason: String) {
        val startedAt = tunnelStartTime.get()
        val durationMs = if (startedAt > 0) System.currentTimeMillis() - startedAt else 0L

        logInfo("Tunnel teardown started", mapOf(
            "reason" to reason,
            "duration_ms" to durationMs,
        ))

        try {
            heartbeatJob?.cancel()
            heartbeatJob = null

            wgBackend?.setState(TUNNEL_NAME, Tunnel.State.DOWN, null)
            wgBackend = null

            tunInterface?.close()
            tunInterface = null

            releaseWakeLock()
            stopForeground(STOP_FOREGROUND_REMOVE)

            state.value = TunnelState.Disconnected

            logInfo("Tunnel teardown completed", mapOf(
                "duration_ms" to durationMs,
            ))

            Sentry.addBreadcrumb("vpn.disconnect.success", mapOf(
                "reason" to reason,
                "duration_ms" to durationMs,
            ))
        } catch (e: Throwable) {
            logError(e, feature = "tunnel", action = "teardown",
                extra = mapOf("reason" to reason))
        } finally {
            stopSelf()
        }
    }

    /**
     * Acquiert un wake lock partiel pour garder le CPU actif.
     */
    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Voile::TunnelWakeLock"
        ).apply {
            setReferenceCounted(false)
            // Timeout de sécurité : 1 heure max
            acquire(60 * 60 * 1000L)
        }

        logInfo("Wake lock acquired")
    }

    /**
     * Libère le wake lock.
     */
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                logInfo("Wake lock released")
            }
        }
        wakeLock = null
    }

    /**
     * Appelé par le système quand le VPN est révoqué
     * (kill switch OS, économie de batterie, etc.)
     */
    override fun onRevoke() {
        logInfo("Tunnel revoked by system", mapOf("reason" to "vpn_revoked"))
        scope.launch { teardown("system_revoke") }
    }

    override fun onDestroy() {
        scope.cancel()
        releaseWakeLock()
        super.onDestroy()
    }

    /**
     * Crée le channel de notification (obligatoire Android 8+).
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Tunnel VPN",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification du tunnel chiffré actif"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Construit la notification foreground.
     */
    private fun buildNotification(text: String): Notification {
        val intent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = if (intent != null) {
            PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else null

        val disconnectAction = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val disconnectIntent = Intent(this, VoileTunnelService::class.java).apply {
                action = ACTION_DISCONNECT
            }
            val disconnectPending = PendingIntent.getService(
                this, 1, disconnectIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            NotificationCompat.Action.Builder(
                R.drawable.ic_shield,
                "Déconnecter",
                disconnectPending,
            ).build()
        } else null

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_shield)
            .setContentTitle("Voile VPN")
            .setContentText(text)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .also { builder ->
                disconnectAction?.let { builder.addAction(it) }
            }
            .build()
    }

    // Keys pour les extras d'Intent
    private companion object {
        const val EXTRA_PRIVATE_KEY = "private_key"
        const val EXTRA_PUBLIC_KEY_SERVER = "server_public_key"
        const val EXTRA_ENDPOINT = "endpoint"
        const val EXTRA_ADDRESS_V4 = "address_v4"
        const val EXTRA_ADDRESS_V6 = "address_v6"
        const val EXTRA_DNS = "dns"
        const val EXTRA_MTU = "mtu"
        const val EXTRA_KEEPALIVE = "keepalive"
        const val EXTRA_SPLIT_APPS = "split_apps"
    }
}
