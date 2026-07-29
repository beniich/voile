package com.example

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun SettingsScreen(viewModel: VoileViewModel) {
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Title
        Column {
            Text(
                text = "Paramètres de Sécurité",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Protocoles de connexion et préférences de sécurité.",
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // ── Protocole ────────────────────────────────────────────────────────
        SettingsSection(title = "Protocole") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ProtocolItem(
                    title = "WireGuard",
                    description = "Le plus rapide, moderne et sécurisé. Recommandé pour la majorité des utilisateurs.",
                    isSelected = settings.protocol == "WireGuard",
                    onClick = { viewModel.updateSettings { it.copy(protocol = "WireGuard") } }
                )
                ProtocolItem(
                    title = "OpenVPN",
                    description = "Protocole traditionnel, hautement configurable. Utile sur les réseaux restreints.",
                    isSelected = settings.protocol == "OpenVPN",
                    onClick = { viewModel.updateSettings { it.copy(protocol = "OpenVPN") } }
                )
            }
        }

        // OpenVPN warning banner
        if (settings.protocol == "OpenVPN") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ConnectingDim)
                    .border(1.dp, Connecting.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Connecting, modifier = Modifier.size(16.dp).padding(top = 1.dp))
                Text(
                    "OpenVPN est plus lent que WireGuard. Latence et débit peuvent être impactés.",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    lineHeight = 17.sp
                )
            }
        }

        // ── Protection ───────────────────────────────────────────────────────
        SettingsSection(title = "Protection") {
            Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                SettingsSwitchRow(
                    icon = Icons.Outlined.Shield,
                    title = "Kill Switch",
                    description = "Bloque l'accès internet si le tunnel tombe pour éviter les fuites.",
                    checked = settings.killSwitch,
                    onCheckedChange = { viewModel.updateSettings { it.copy(killSwitch = !it.killSwitch) } },
                    showDivider = true
                )
                SettingsSwitchRow(
                    icon = Icons.Outlined.Wifi,
                    title = "Connexion automatique",
                    description = "Se connecte automatiquement sur les réseaux Wi-Fi non sécurisés.",
                    checked = settings.autoConnect,
                    onCheckedChange = { viewModel.updateSettings { it.copy(autoConnect = !it.autoConnect) } },
                    showDivider = true
                )
                SettingsSwitchRow(
                    icon = Icons.Outlined.Security,
                    title = "CyberSec Shield",
                    description = "Bloque les fuites DNS, le pistage publicitaire et les domaines malveillants.",
                    checked = settings.cyberSec,
                    onCheckedChange = { viewModel.updateSettings { it.copy(cyberSec = !it.cyberSec) } },
                    showDivider = false
                )
            }
        }

        // ── Split Tunneling ──────────────────────────────────────────────────
        SettingsSection(title = "Split Tunneling") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Activer le split tunneling",
                            fontSize = 15.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "Certaines apps contournent le VPN.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    CustomSwitch(
                        checked = settings.splitTunneling,
                        onCheckedChange = { viewModel.updateSettings { it.copy(splitTunneling = !it.splitTunneling) } }
                    )
                }

                if (settings.splitTunneling) {
                    SplitTunnelAppsSection(
                        splitApps = settings.splitApps,
                        onToggleApp = { appId ->
                            viewModel.updateSettings { s ->
                                val newApps = if (appId in s.splitApps) s.splitApps - appId else s.splitApps + appId
                                s.copy(splitApps = newApps)
                            }
                        }
                    )
                }
            }
        }

        // ── Compte / Déconnexion ─────────────────────────────────────────────
        val user by viewModel.currentUser.collectAsState()
        user?.let { u ->
            SettingsSection(title = "Compte") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Connecté en tant que :",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = u.email ?: "Utilisateur",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.signOut() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Danger,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Se déconnecter", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Split Tunnel Apps
// ---------------------------------------------------------------------------

private data class SampleApp(val id: String, val name: String, val icon: ImageVector)

private val SAMPLE_APPS = listOf(
    SampleApp("browser",   "Navigateur web",  Icons.Outlined.Language),
    SampleApp("streaming", "Streaming vidéo", Icons.Outlined.PlayArrow),
    SampleApp("banking",   "App bancaire",    Icons.Outlined.Lock),
    SampleApp("mail",      "Client email",    Icons.Outlined.Email),
)

@Composable
fun SplitTunnelAppsSection(
    splitApps: Set<String>,
    onToggleApp: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Background)
            .border(1.dp, BorderSoft, RoundedCornerShape(12.dp))
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            "Applications qui contournent le VPN",
            fontSize = 11.sp,
            color = TextMuted,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
        SAMPLE_APPS.forEach { app ->
            val isExcluded = app.id in splitApps
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onToggleApp(app.id) }
                    .background(if (isExcluded) ConnectingDim else Color.Transparent)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .semantics { contentDescription = "${app.name}, ${if (isExcluded) "exclue du VPN" else "dans le VPN"}" },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(app.icon, contentDescription = null, tint = if (isExcluded) Connecting else TextMuted, modifier = Modifier.size(18.dp))
                Text(app.name, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                if (isExcluded) {
                    Text("Bypass", fontSize = 11.sp, color = Connecting, fontWeight = FontWeight.Medium)
                }
                Checkbox(
                    checked = isExcluded,
                    onCheckedChange = { onToggleApp(app.id) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Connecting,
                        uncheckedColor = TextMuted,
                        checkmarkColor = Color.White
                    )
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Reusable atoms
// ---------------------------------------------------------------------------

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, BorderSoft, RoundedCornerShape(16.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = title, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        content()
    }
}

@Composable
fun ProtocolItem(title: String, description: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) SecuredDim else Color.Transparent)
            .border(1.dp, if (isSelected) Secured.copy(alpha = 0.5f) else BorderSoft, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .semantics { contentDescription = "$title${if (isSelected) ", sélectionné" else ""}" },
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Secured,
                unselectedColor = TextMuted
            ),
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Column {
            Text(title, fontSize = 15.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, fontSize = 12.sp, color = TextMuted, lineHeight = 17.sp)
        }
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = true
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(17.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                Text(description, fontSize = 11.sp, color = TextMuted, lineHeight = 15.sp, modifier = Modifier.padding(top = 2.dp))
            }
            CustomSwitch(checked = checked, onCheckedChange = onCheckedChange)
        }
        if (showDivider) {
            Divider(color = BorderSoft, thickness = 1.dp)
        }
    }
}

/** Toggle knob avec translateX animé (pas de téléportation) */
@Composable
fun CustomSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val offset by animateFloatAsState(
        targetValue = if (checked) 22f else 0f,
        animationSpec = tween(250, easing = androidx.compose.animation.core.FastOutSlowInEasing),
        label = "knob"
    )

    Box(
        modifier = Modifier
            .size(width = 50.dp, height = 28.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (checked) Secured else BorderSoft)   // Turquoise (Secured) au lieu de AccentBlue
            .clickable { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer { translationX = offset }
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}
