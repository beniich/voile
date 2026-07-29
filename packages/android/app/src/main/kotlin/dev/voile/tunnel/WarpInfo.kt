package dev.voile.tunnel

import kotlinx.serialization.Serializable

@Serializable
data class WarpInfo(
    val id: Int,
    val country: String,
    val city: String,
    val flag: String,
    val ping: Int,
    val load: Int,
)

@Serializable
data class TunnelTelemetry(
    val ip: String,
    val colo: String,
    val downloadMbps: Double,
    val uploadMbps: Double,
    val sessionDurationSec: Long,
)
