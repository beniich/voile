package com.example

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: VoileViewModel,
    onNavigateToServers: () -> Unit
) {
    val state by viewModel.connectionState.collectAsState()
    val telemetry by viewModel.telemetry.collectAsState()
    val selectedServerId by viewModel.selectedServerId.collectAsState()
    val server = SERVERS.find { it.id == selectedServerId } ?: SERVERS.first()
    val haptic = LocalHapticFeedback.current
    val tunnelState by VoileTunnelService.tunnelState.collectAsState()
    val connectedInfo = tunnelState as? VoileTunnelService.TunnelState.Connected

    Box(modifier = Modifier.fillMaxSize()) {
        // Ambient background glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Secured.copy(alpha = 0.07f), Color.Transparent),
                    center = center,
                    radius = size.width * 0.8f
                ),
                radius = size.width * 0.8f,
                center = center
            )
            drawCircle(
                color = Secured.copy(alpha = 0.02f),
                radius = size.width * 0.6f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
            drawCircle(
                color = Secured.copy(alpha = 0.04f),
                radius = size.width * 0.85f,
                center = center,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Header(state)

            // Connect button
            ConnectButton(
                state = state,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.toggleConnect()
                },
                serverCity = server.city
            )

            // Server selector card
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Surface.copy(alpha = 0.8f))
                    .border(1.dp, Secured.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onNavigateToServers)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .semantics {
                        contentDescription = "Serveur actuel : ${server.city}, ${server.country}. Appuyer pour changer."
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(server.flag, fontSize = 22.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${server.city}, ${server.country}",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            connectedInfo?.let { "Edge Cloudflare ${it.colo}" } ?: "Nœud relais actuel",
                            color = if (connectedInfo != null) Secured else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = if (connectedInfo != null) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // IP before/after — affiché uniquement quand connecté
            if (state == ConnectionState.Connected && telemetry.realIp.isNotEmpty()) {
                IpBeforeAfterCard(realIp = telemetry.realIp, vpnIp = telemetry.ip)
            }

            // Telemetry grid
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TelemetryCard(
                    icon = Icons.Default.Download,
                    label = "TÉLÉCHARGEMENT",
                    value = telemetry.down,
                    unit = "Mb/s",
                    modifier = Modifier.weight(1f)
                )
                TelemetryCard(
                    icon = Icons.Default.Upload,
                    label = "ENVOI",
                    value = telemetry.up,
                    unit = "Mb/s",
                    modifier = Modifier.weight(1f)
                )
            }

            TelemetryCard(
                icon = Icons.Default.Schedule,
                label = "DURÉE DE SESSION",
                value = formatDuration(telemetry.session),
                unit = null,
                modifier = Modifier.fillMaxWidth()
            )

            // Disclaimer : simulation si pas de tunnel réel
            if (connectedInfo == null) {
                Text(
                    text = "⚠  SIMULATION — Aucune connexion réelle n'est établie",
                    fontSize = 10.sp,
                    color = TextMuted,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// IP Before / After card
// ---------------------------------------------------------------------------

@Composable
fun IpBeforeAfterCard(realIp: String, vpnIp: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SecuredDim)
            .border(1.dp, Secured.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Shield, contentDescription = null, tint = Secured, modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("IP RÉELLE MASQUÉE", fontSize = 9.sp, color = Secured, fontFamily = FontFamily.Monospace, letterSpacing = 0.8.sp)
            // IP réelle barrée
            Text(
                realIp,
                fontSize = 12.sp,
                color = TextMuted,
                fontFamily = FontFamily.Monospace,
                textDecoration = TextDecoration.LineThrough
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("VPN IP", fontSize = 9.sp, color = Secured, fontFamily = FontFamily.Monospace, letterSpacing = 0.8.sp)
            Text(vpnIp, fontSize = 12.sp, color = TextPrimary, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
        }
    }
}

// ---------------------------------------------------------------------------
// Telemetry card
// ---------------------------------------------------------------------------

@Composable
fun TelemetryCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.dp, BorderSoft, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(13.dp))
            Text(label, fontSize = 10.sp, color = TextMuted, letterSpacing = 0.3.sp)
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                value,
                fontSize = 17.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (unit != null) {
                Text(unit, fontSize = 11.sp, color = TextMuted, modifier = Modifier.padding(bottom = 1.dp))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Header & StatusPill
// ---------------------------------------------------------------------------

@Composable
fun Header(state: ConnectionState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Voile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Secured,
            letterSpacing = 0.5.sp
        )
        StatusPill(state)
    }
}

@Composable
fun StatusPill(state: ConnectionState) {
    val (statusLabel, statusColor) = when (state) {
        ConnectionState.Connected    -> "Sécurisé"   to Secured
        ConnectionState.Connecting   -> "Connexion…" to Connecting
        ConnectionState.Error        -> "Erreur"     to Danger
        ConnectionState.Disconnected -> "Déconnecté" to TextMuted
    }
    val animatedColor by animateColorAsState(targetValue = statusColor, label = "color")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Surface)
            .border(1.dp, animatedColor.copy(alpha = 0.3f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .semantics { contentDescription = "Statut : $statusLabel" }
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(animatedColor)
        )
        Text(
            text = statusLabel,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = animatedColor
        )
    }
}

fun formatDuration(totalSeconds: Int): String {
    val h = (totalSeconds / 3600).toString().padStart(2, '0')
    val m = ((totalSeconds % 3600) / 60).toString().padStart(2, '0')
    val s = (totalSeconds % 60).toString().padStart(2, '0')
    return "$h:$m:$s"
}
