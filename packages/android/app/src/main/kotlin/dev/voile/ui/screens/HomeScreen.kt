package dev.voile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.voile.core.tokens.VoileColors
import dev.voile.tunnel.VoileTunnelService
import dev.voile.tunnel.WarpInfo
import dev.voile.tunnel.TunnelTelemetry
import dev.voile.ui.components.ConnectButton
import dev.voile.ui.components.TelemetryCard
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.text.font.FontWeight

@Composable
fun HomeScreen(
    tunnelState: VoileTunnelService.TunnelState,
    warpInfo: WarpInfo,
    realIp: String?,
    telemetry: TunnelTelemetry,
    onToggleConnect: () -> Unit,
    onGoServers: () -> Unit,
) {
    val statusColor = when (tunnelState) {
        is VoileTunnelService.TunnelState.Connected -> Color(VoileColors.secured)
        is VoileTunnelService.TunnelState.Connecting -> Color(VoileColors.connecting)
        is VoileTunnelService.TunnelState.Error -> Color(VoileColors.danger)
        else -> Color(VoileColors.textMuted)
    }

    val statusLabel = when (tunnelState) {
        is VoileTunnelService.TunnelState.Connected -> "Connexion sécurisée"
        is VoileTunnelService.TunnelState.Connecting -> "Négociation du tunnel"
        is VoileTunnelService.TunnelState.Error -> "Échec de la connexion"
        else -> "Déconnecté"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // Status pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(VoileColors.surface),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, Color(VoileColors.borderSoft)
                ),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = statusColor,
                                shape = RoundedCornerShape(50)
                            )
                            .semantics { contentDescription = null }
                    )
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(VoileColors.textSecondary),
                    )
                }
            }
        }

        // Connect button
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ConnectButton(
                state = tunnelState,
                server = warpInfo,
                onClick = onToggleConnect,
            )
        }

        // Server card (compact)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .border(
                    1.dp, Color(VoileColors.borderSoft), RoundedCornerShape(14.dp)
                )
                .clickable { onGoServers() }
                .semantics { contentDescription = "Changer de serveur, actuellement ${warpInfo.city}" },
            color = Color(VoileColors.surface),
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = warpInfo.flag, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${warpInfo.city}, ${warpInfo.country}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Color(VoileColors.textPrimary),
                    )
                    Text(
                        text = "Nœud relais actuel",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(VoileColors.textMuted),
                    )
                }
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(VoileColors.textMuted),
                )
            }
        }

        // Telemetry
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TelemetryCard(
                icon = Icons.Default.Public,
                label = "Adresse IP publique",
                value = telemetry.ip,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TelemetryCard(
                    icon = Icons.Default.ArrowDownward,
                    label = "Téléchargement",
                    value = "%.1f".format(telemetry.downloadMbps),
                    unit = "Mb/s",
                    modifier = Modifier.weight(1f),
                )
                TelemetryCard(
                    icon = Icons.Default.ArrowUpward,
                    label = "Envoi",
                    value = "%.1f".format(telemetry.uploadMbps),
                    unit = "Mb/s",
                    modifier = Modifier.weight(1f),
                )
            }
            TelemetryCard(
                icon = Icons.Default.Schedule,
                label = "Durée de session",
                value = formatDuration(telemetry.sessionDurationSec),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

private fun formatDuration(seconds: Long): String {
    val safe = seconds.coerceAtLeast(0)
    val h = safe / 3600
    val m = (safe % 3600) / 60
    val s = safe % 60
    return "%02d:%02d:%02d".format(h, m, s)
}
