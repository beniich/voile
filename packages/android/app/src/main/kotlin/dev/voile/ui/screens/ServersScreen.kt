package dev.voile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.voile.core.servers
import dev.voile.core.tokens.VoileColors
import dev.voile.tunnel.WarpInfo

@Composable
fun ServersScreen(
    selectedId: Int,
    favorites: Set<Int>,
    onSelect: (Int) -> Unit,
    onToggleFavorite: (Int) -> Unit,
) {
    var query by remember { mutableStateOf("") }

    val filtered = servers.filter { s ->
        query.isBlank() ||
        s.city.contains(query, ignoreCase = true) ||
        s.country.contains(query, ignoreCase = true)
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column {
                Text(
                    text = "Serveurs",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(VoileColors.textPrimary),
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${filtered.size} nœuds disponibles",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(VoileColors.textMuted),
                )
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Rechercher") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
        }

        items(filtered, key = { it.id }) { server ->
            ServerRow(
                server = WarpInfo(
                    id = server.id,
                    country = server.country,
                    city = server.city,
                    flag = server.flag,
                    ping = server.ping,
                    load = server.load,
                ),
                isSelected = server.id == selectedId,
                isFavorite = server.id in favorites,
                onSelect = { onSelect(server.id) },
                onToggleFavorite = { onToggleFavorite(server.id) },
            )
        }
    }
}

@Composable
private fun ServerRow(
    server: WarpInfo,
    isSelected: Boolean,
    isFavorite: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val borderColor = if (isSelected) Color(VoileColors.secured)
                      else Color(VoileColors.borderSoft)
    val bgColor = if (isSelected) Color(VoileColors.secured).copy(alpha = 0.14f)
                  else Color(VoileColors.surface)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .semantics {
                contentDescription = "Serveur ${server.city}, ping ${server.ping} ms"
            },
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Star
                                  else Icons.Outlined.StarBorder,
                    contentDescription = if (isFavorite)
                        "Retirer ${server.city} des favoris"
                    else "Ajouter ${server.city} aux favoris",
                    tint = if (isFavorite) Color(VoileColors.connecting)
                           else Color(VoileColors.textMuted),
                )
            }

            Spacer(Modifier.width(4.dp))

            Text(server.flag, style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = server.city,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(VoileColors.textPrimary),
                )
                Text(
                    text = server.country,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(VoileColors.textMuted),
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${server.ping} ms",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(VoileColors.textSecondary),
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .background(
                                color = loadColor(server.load),
                                shape = CircleShape
                            )
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "${server.load}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(VoileColors.textMuted),
                    )
                }
            }

            if (isSelected) {
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = CircleShape,
                    color = Color(VoileColors.secured),
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(VoileColors.bg),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun loadColor(load: Int): Color = when {
    load < 35 -> Color(VoileColors.secured)
    load < 65 -> Color(VoileColors.connecting)
    else -> Color(VoileColors.danger)
}
