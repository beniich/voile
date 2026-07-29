package dev.voile.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.voile.core.tokens.VoileColors
import dev.voile.ui.VoileTab

@Composable
fun VoileBottomNav(
    current: VoileTab,
    onSelect: (VoileTab) -> Unit,
) {
    Surface(
        color = Color(VoileColors.bg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            androidx.compose.material3.HorizontalDivider(
                color = Color(VoileColors.borderSoft)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                NavItem(
                    tab = VoileTab.HOME,
                    icon = Icons.Default.Home,
                    selected = current == VoileTab.HOME,
                    onClick = { onSelect(VoileTab.HOME) },
                    modifier = Modifier.weight(1f),
                )
                NavItem(
                    tab = VoileTab.SERVERS,
                    icon = Icons.Default.Public,
                    selected = current == VoileTab.SERVERS,
                    onClick = { onSelect(VoileTab.SERVERS) },
                    modifier = Modifier.weight(1f),
                )
                NavItem(
                    tab = VoileTab.SETTINGS,
                    icon = Icons.Default.Settings,
                    selected = current == VoileTab.SETTINGS,
                    onClick = { onSelect(VoileTab.SETTINGS) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NavItem(
    tab: VoileTab,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = if (selected) Color(VoileColors.secured)
               else Color(VoileColors.textMuted)

    Surface(
        modifier = modifier
            .semantics { contentDescription = tab.label },
        color = Color.Transparent,
        onClick = onClick,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(vertical = 6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = tab.label,
                style = MaterialTheme.typography.labelSmall,
                color = tint,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}
