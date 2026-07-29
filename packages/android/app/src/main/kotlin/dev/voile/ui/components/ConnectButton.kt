package dev.voile.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.voile.core.tokens.VoileColors
import dev.voile.tunnel.VoileTunnelService
import dev.voile.tunnel.WarpInfo

@Composable
fun ConnectButton(
    state: VoileTunnelService.TunnelState,
    server: WarpInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = when (state) {
        is VoileTunnelService.TunnelState.Connected -> Color(VoileColors.secured)
        is VoileTunnelService.TunnelState.Connecting -> Color(VoileColors.connecting)
        is VoileTunnelService.TunnelState.Error -> Color(VoileColors.danger)
        else -> Color(VoileColors.idle)
    }

    val ringCount = when (state) {
        is VoileTunnelService.TunnelState.Connecting -> 3
        is VoileTunnelService.TunnelState.Connected -> 1
        else -> 0
    }

    val label = when (state) {
        is VoileTunnelService.TunnelState.Connected -> "Sécurisé"
        is VoileTunnelService.TunnelState.Connecting -> "Négociation"
        is VoileTunnelService.TunnelState.Error -> "Erreur"
        else -> "Se connecter"
    }

    val ariaLabel = when (state) {
        is VoileTunnelService.TunnelState.Connected ->
            "Déconnecter du serveur ${server.city}"
        is VoileTunnelService.TunnelState.Connecting ->
            "Annuler la connexion à ${server.city}"
        is VoileTunnelService.TunnelState.Error ->
            "Réessayer la connexion à ${server.city}"
        else -> "Se connecter au serveur ${server.city}"
    }

    Box(
        modifier = modifier
            .size(220.dp)
            .semantics { contentDescription = ariaLabel },
        contentAlignment = Alignment.Center
    ) {
        // Sonar rings
        repeat(ringCount) { i ->
            SonarRing(color = color, delayMs = i * 500L)
        }

        // Bouton central
        Surface(
            modifier = Modifier
                .size(152.dp)
                .clickable(onClick = onClick),
            shape = CircleShape,
            border = BorderStroke(1.5.dp, color),
            color = Color(VoileColors.surface),
            tonalElevation = 4.dp,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                val icon = when (state) {
                    is VoileTunnelService.TunnelState.Connected -> Icons.Filled.Shield
                    is VoileTunnelService.TunnelState.Connecting -> Icons.Filled.WifiTethering
                    is VoileTunnelService.TunnelState.Error -> Icons.Outlined.Error
                    else -> Icons.Filled.Shield
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(38.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(VoileColors.textMuted),
                )
            }
        }
    }
}

@Composable
private fun SonarRing(color: Color, delayMs: Long) {
    val infiniteTransition = rememberInfiniteTransition(label = "sonar")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.72f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, delayMillis = delayMs.toInt()),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, delayMillis = delayMs.toInt()),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale)
            .border(
                width = 1.5.dp,
                color = color.copy(alpha = alpha),
                shape = CircleShape
            )
    )
}
