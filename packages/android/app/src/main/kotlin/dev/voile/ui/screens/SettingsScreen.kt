package dev.voile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.voile.core.tokens.VoileColors
import dev.voile.tunnel.VoileSettings

@Composable
fun SettingsScreen(
    settings: VoileSettings,
    onSettingsChange: (VoileSettings) -> Unit,
    onOpenSplitTunneling: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column {
            Text(
                text = "Paramètres",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color(VoileColors.textPrimary),
            )
            Text(
                text = "Sécurité et comportement du tunnel",
                style = MaterialTheme.typography.bodySmall,
                color = Color(VoileColors.textMuted),
            )
        }

        // Protocol selector
        Text(
            text = "PROTOCOLE",
            style = MaterialTheme.typography.labelSmall,
            color = Color(VoileColors.textMuted),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ProtocolButton(
                label = "WireGuard",
                icon = Icons.Outlined.Bolt,
                selected = settings.protocol == "WireGuard",
                onClick = { onSettingsChange(settings.copy(protocol = "WireGuard")) },
                modifier = Modifier.weight(1f),
            )
            ProtocolButton(
                label = "OpenVPN",
                icon = Icons.Outlined.Lock,
                selected = settings.protocol == "OpenVPN",
                onClick = { onSettingsChange(settings.copy(protocol = "OpenVPN")) },
                modifier = Modifier.weight(1f),
            )
        }

        // Protection toggles
        Text(
            text = "PROTECTION",
            style = MaterialTheme.typography.labelSmall,
            color = Color(VoileColors.textMuted),
        )
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(VoileColors.surface),
            border = androidx.compose.foundation.BorderStroke(
                1.dp, Color(VoileColors.borderSoft), RoundedCornerShape(14.dp)
            ),
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                SettingsRow(
                    icon = Icons.Outlined.Shield,
                    title = "Kill Switch",
                    subtitle = "Coupe l'accès internet si le tunnel se déconnecte",
                    checked = settings.killSwitch,
                    onCheckedChange = { onSettingsChange(settings.copy(killSwitch = it)) },
                )
                SettingsRow(
                    icon = Icons.Outlined.Wifi,
                    title = "Connexion automatique",
                    subtitle = "Se connecte au serveur le plus proche au démarrage",
                    checked = settings.autoConnect,
                    onCheckedChange = { onSettingsChange(settings.copy(autoConnect = it)) },
                )
                SettingsRow(
                    icon = Icons.Outlined.Radar,
                    title = "CyberSec Shield",
                    subtitle = "Bloque les fuites DNS et le suivi publicitaire",
                    checked = settings.cyberSec,
                    onCheckedChange = { onSettingsChange(settings.copy(cyberSec = it)) },
                )
                SettingsRow(
                    icon = Icons.Outlined.Public,
                    title = "Split tunneling",
                    subtitle = if (settings.splitTunneling)
                        "${settings.splitApps.size} application(s) exclue(s)"
                    else "Choisir les apps qui contournent le VPN",
                    checked = settings.splitTunneling,
                    onCheckedChange = { onSettingsChange(settings.copy(splitTunneling = it)) },
                    trailing = if (settings.splitTunneling) {
                        {
                            TextButton(onClick = onOpenSplitTunneling) {
                                Text("Configurer", color = Color(VoileColors.textSecondary))
                            }
                        }
                    } else null,
                    isLast = true,
                )
            }
        }
    }
}

@Composable
private fun ProtocolButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (selected) Color(VoileColors.secured)
                      else Color(VoileColors.borderSoft)
    val bgColor = if (selected) Color(VoileColors.secured).copy(alpha = 0.14f)
                  else Color(VoileColors.surface)
    val textColor = if (selected) Color(VoileColors.secured)
                    else Color(VoileColors.textSecondary)

    Surface(
        modifier = modifier
            .clickable(onClick = onClick)
            .semantics {
                role = Role.RadioButton
                contentDescription = "Protocole $label${if (selected) ", sélectionné" else ""}"
            },
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(11.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = textColor,
                modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = textColor, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    trailing: (@Composable () -> Unit)? = null,
    isLast: Boolean = false,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(VoileColors.surfaceElevated),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color(VoileColors.textSecondary),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(VoileColors.textPrimary),
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(VoileColors.textMuted),
                )
            }

            if (trailing != null) trailing()

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.semantics { contentDescription = title },
            )
        }
        if (!isLast) {
            HorizontalDivider(color = Color(VoileColors.borderSoft))
        }
    }
}
