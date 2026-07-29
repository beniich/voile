package dev.voile.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.voile.core.tokens.VoileColors
import dev.voile.tunnel.VoileTunnelService
import dev.voile.tunnel.WarpInfo
import dev.voile.ui.components.ConnectButton
import dev.voile.ui.components.VoileBottomNav
import dev.voile.ui.screens.HomeScreen
import dev.voile.ui.screens.ServersScreen
import dev.voile.ui.screens.SettingsScreen
import dev.voile.ui.viewmodel.VoileViewModel

enum class VoileTab(val label: String) {
    HOME("Accueil"),
    SERVERS("Serveurs"),
    SETTINGS("Réglages"),
}

@Composable
fun VoileApp(
    viewModel: VoileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = { VoileTopBar(state.currentServer) },
        bottomBar = {
            VoileBottomNav(
                current = state.currentTab,
                onSelect = viewModel::selectTab,
            )
        },
        containerColor = Color(VoileColors.bg),
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (state.currentTab) {
                VoileTab.HOME -> HomeScreen(
                    tunnelState = state.tunnelState,
                    warpInfo = state.currentServer,
                    realIp = state.realIp,
                    telemetry = state.telemetry,
                    onToggleConnect = { viewModel.toggleConnection(context) },
                    onGoServers = { viewModel.selectTab(VoileTab.SERVERS) },
                )
                VoileTab.SERVERS -> ServersScreen(
                    selectedId = state.selectedServerId,
                    favorites = state.favorites,
                    onSelect = viewModel::selectServer,
                    onToggleFavorite = viewModel::toggleFavorite,
                )
                VoileTab.SETTINGS -> SettingsScreen(
                    settings = state.settings,
                    onSettingsChange = viewModel::updateSettings,
                    onOpenSplitTunneling = viewModel::openSplitTunneling,
                )
            }
        }
    }
}

@Composable
private fun VoileTopBar(server: WarpInfo) {
    Surface(
        color = Color(VoileColors.bg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color(VoileColors.secured).copy(alpha = 0.14f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = Color(VoileColors.secured),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "Voile",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(VoileColors.textPrimary),
                )
            }
            Text(
                text = server.city,
                style = MaterialTheme.typography.labelMedium,
                color = Color(VoileColors.textMuted),
            )
        }
    }
}
