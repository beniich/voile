package dev.voile.tunnel

import kotlinx.serialization.Serializable

@Serializable
data class VoileSettings(
    val protocol: String = "WireGuard",
    val killSwitch: Boolean = true,
    val autoConnect: Boolean = false,
    val cyberSec: Boolean = true,
    val splitTunneling: Boolean = false,
    val splitApps: List<String> = emptyList(),
)
