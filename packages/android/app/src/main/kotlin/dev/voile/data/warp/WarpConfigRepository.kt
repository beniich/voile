package dev.voile.data.warp

import com.wireguard.config.InetEndpoint
import dev.voile.lib.logError
import dev.voile.lib.logInfo
import dev.voile.tunnel.VoileTunnelService
import dev.voile.tunnel.crypto.CryptoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.security.SecureRandom
import java.util.concurrent.TimeUnit

/**
 * Repository pour la configuration Cloudflare WARP.
 *
 * Responsabilités :
 *   - Générer une paire de clés WireGuard X25519 (BouncyCastle)
 *   - S'enregistrer auprès de Cloudflare /reg
 *   - Retourner un TunnelConfig prêt pour le service VPN
 *   - Tester la latence et la disponibilité de WARP
 */
class WarpConfigRepository {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Récupère (ou régénère) une config WARP.
     * En cas d'échec Cloudflare, retente une fois avec un délai.
     */
    suspend fun fetchConfig(): VoileTunnelService.TunnelConfig =
        withContext(Dispatchers.IO) {
            try {
                fetchConfigInternal()
            } catch (e: IOException) {
                logError(e, feature = "warp", action = "fetchConfig", extra = mapOf(
                    "attempt" to 1,
                ))
                // Retry une fois après 500ms
                kotlinx.coroutines.delay(500)
                fetchConfigInternal()
            }
        }

    private fun fetchConfigInternal(): VoileTunnelService.TunnelConfig {
        // 1. Génère une clé Curve25519
        val keyPair = CryptoUtils.generateKeyPair()
        val publicKeyB64 = CryptoUtils.encodeBase64(keyPair.publicKey)
        logInfo("Generated X25519 keypair", mapOf(
            "public_key_fp" to publicKeyB64.take(8) + "...",
        ))

        // 2. Construit l'ID d'installation (UUID-like)
        val installId = generateInstallId()

        // 3. Construit le payload JSON
        val payload = buildString {
            append("{")
            append("\"install_id\":\"$installId\",")
            append("\"tos\":\"${java.time.Instant.now()}\",")
            append("\"key\":\"$publicKeyB64\",")
            append("\"type\":\"Android\",")
            append("\"locale\":\"fr_FR\",")
            append("\"warp_enabled\":true,")
            append("\"referer\":\"voile.app\"")
            append("}")
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = payload.toRequestBody(mediaType)

        // 4. POST vers Cloudflare /reg
        val request = Request.Builder()
            .url("$WARP_BASE/reg/$installId")
            .post(body)
            .header("User-Agent", "okhttp/4.12.0")
            .header("Accept", "application/json")
            .build()

        val response = httpClient.newCall(request).execute()
        response.use { resp ->
            if (!resp.isSuccessful) {
                throw IOException("Cloudflare /reg returned ${resp.code}")
            }

            val responseBody = resp.body?.string()
                ?: throw IOException("Empty response from Cloudflare /reg")

            val reg = json.decodeFromString<CloudflareReg>(responseBody)
            val peer = reg.config.peers.firstOrNull()
                ?: throw IOException("Cloudflare returned no peers")

            // 5. Construit le TunnelConfig pour Android
            val endpoint = InetEndpoint.parse("${peer.endpoint.host}:${peer.endpoint.port}")

            logInfo("WARP config fetched", mapOf(
                "endpoint" to "${peer.endpoint.host}:${peer.endpoint.port}",
                "colo" to (reg.config.interface_.addresses.v4),
            ))

            return VoileTunnelService.TunnelConfig(
                privateKey = CryptoUtils.encodeBase64(keyPair.privateKey),
                serverPublicKey = peer.publicKey,
                endpoint = "${peer.endpoint.host}:${peer.endpoint.port}",
                addressV4 = "${reg.config.interface_.addresses.v4}/32",
                addressV6 = "${reg.config.interface_.addresses.v6}/128",
                dns = reg.config.interface_.dns,
                mtu = 1280,
                persistentKeepalive = 25,
            )
        }
    }

    /**
     * Mesure la latence vers Cloudflare (1.1.1.1).
     * @return Latence en ms, ou -1 si échec.
     */
    suspend fun measureLatency(): Long = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url("https://1.1.1.1/cdn-cgi/trace")
            .header("Cache-Control", "no-cache")
            .build()

        val start = System.currentTimeMillis()
        try {
            val response = httpClient.newCall(request).execute()
            response.use { it.isSuccessful }
            System.currentTimeMillis() - start
        } catch (e: IOException) {
            logError(e, feature = "warp", action = "measureLatency")
            -1L
        }
    }

    /**
     * Interroge /cdn-cgi/trace pour détecter si WARP est actif.
     */
    suspend fun isWarpActive(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://1.1.1.1/cdn-cgi/trace")
                .header("Cache-Control", "no-cache")
                .build()
            val response = httpClient.newCall(request).execute()
            response.use { resp ->
                if (!resp.isSuccessful) return@withContext false
                val text = resp.body?.string() ?: return@withContext false
                text.contains("warp=on") || text.contains("warp=plus")
            }
        } catch (e: IOException) {
            false
        }
    }

    /**
     * Génère un install_id unique (UUIDv4-like).
     */
    private fun generateInstallId(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        // UUID format v4
        bytes[6] = (bytes[6].toInt() and 0x0f or 0x40).toByte()
        bytes[8] = (bytes[8].toInt() and 0x3f or 0x80).toByte()

        val hex = StringBuilder(36)
        for (i in bytes.indices) {
            hex.append(String.format("%02x", bytes[i]))
            if (i == 3 || i == 5 || i == 7 || i == 9) hex.append('-')
        }
        return hex.toString()
    }

    @Serializable
    private data class CloudflareReg(
        val config: ConfigData
    ) {
        @Serializable
        data class ConfigData(
            val interface_: InterfaceData,
            val peers: List<PeerData>,
        )

        @Serializable
        data class InterfaceData(
            val addresses: AddressesData,
            val dns: String,
        )

        @Serializable
        data class AddressesData(
            val v4: String,
            val v6: String,
        )

        @Serializable
        data class PeerData(
            val publicKey: String,
            val endpoint: EndpointData,
        )

        @Serializable
        data class EndpointData(
            val host: String,
            val port: Int,
        )
    }

    companion object {
        private const val WARP_BASE = "https://api.cloudflareclient.com/v0a2157"
    }
}
