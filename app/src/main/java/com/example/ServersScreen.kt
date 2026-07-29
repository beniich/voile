package com.example

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/** Retourne 1 (mauvais) / 2 (moyen) / 3 (bon) selon le ping */
private fun pingLevel(ping: Int): Int = when {
    ping < 50  -> 3
    ping < 120 -> 2
    else       -> 1
}

private fun loadColor(load: Int) = when {
    load < 35 -> Secured
    load < 65 -> Connecting
    else      -> Danger
}

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@Composable
fun ServersScreen(viewModel: VoileViewModel) {
    val selectedId by viewModel.selectedServerId.collectAsState()
    val favorites  by viewModel.favorites.collectAsState()
    var searchQuery       by remember { mutableStateOf("") }
    var favoritesOnly     by remember { mutableStateOf(false) }

    val filteredServers = SERVERS.filter { s ->
        val matchQuery = searchQuery.isBlank()
            || s.city.contains(searchQuery, ignoreCase = true)
            || s.country.contains(searchQuery, ignoreCase = true)
        val matchFav = !favoritesOnly || s.id in favorites
        matchQuery && matchFav
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Title
        Column {
            Text(
                text = "Sélection de serveur",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "${filteredServers.size} nœud${if (filteredServers.size > 1) "s" else ""} disponible${if (filteredServers.size > 1) "s" else ""}",
                fontSize = 12.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Rechercher un pays ou une ville…", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Secured,
                unfocusedBorderColor = BorderSoft,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface,
                cursorColor = Secured
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // Favorites filter chip
        FilterChip(
            selected = favoritesOnly,
            onClick = { favoritesOnly = !favoritesOnly },
            label = { Text("Favoris uniquement", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    if (favoritesOnly) Icons.Default.Star else Icons.Outlined.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = SecuredDim,
                selectedLabelColor = Secured,
                selectedLeadingIconColor = Secured,
                containerColor = Surface,
                labelColor = TextMuted,
                iconColor = TextMuted
            ),
            border = FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = favoritesOnly,
                selectedBorderColor = Secured,
                borderColor = BorderSoft
            )
        )

        // Server list
        if (filteredServers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("Aucun serveur ne correspond à votre recherche", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(filteredServers, key = { it.id }) { server ->
                    ServerListItem(
                        server = server,
                        isSelected = server.id == selectedId,
                        isFavorite = server.id in favorites,
                        onSelect = { viewModel.selectServer(server.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(server.id) }
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Server list item
// ---------------------------------------------------------------------------

@Composable
fun ServerListItem(
    server: Server,
    isSelected: Boolean,
    isFavorite: Boolean,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val animatedBorder by animateColorAsState(
        targetValue = if (isSelected) Secured else BorderSoft,
        label = "border"
    )
    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) SecuredDim else Color.Transparent,
        label = "bg"
    )
    val bars = pingLevel(server.ping)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .border(1.5.dp, animatedBorder, RoundedCornerShape(14.dp))
            .background(animatedBg)
            .clickable(onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .semantics {
                contentDescription = buildString {
                    append("${server.city}, ${server.country}")
                    append(", ping ${server.ping} ms")
                    append(", charge ${server.load} pourcent")
                    if (isFavorite) append(", favori")
                    if (isSelected) append(", sélectionné")
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Flag
        Text(server.flag, fontSize = 26.sp)

        // Name + country
        Column(modifier = Modifier.weight(1f)) {
            Text(server.city, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
            Text(server.country, color = TextMuted, fontSize = 11.sp)
        }

        // Ping bars + ms + load
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // 3-bar ping indicator
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val barColors = listOf(Danger, Connecting, Secured)
                for (b in 1..3) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height((4 + b * 4).dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (b <= bars) barColors[b - 1] else Border)
                    )
                }
            }
            Text(
                "${server.ping} ms",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(loadColor(server.load))
                )
                Text("${server.load}%", color = TextMuted, fontSize = 10.sp)
            }
        }

        // Favorite toggle
        IconButton(
            onClick = onToggleFavorite,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.Star,
                contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris",
                tint = if (isFavorite) Connecting else TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }

        // Selected checkmark
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Secured),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Background, modifier = Modifier.size(14.dp))
            }
        }
    }
}
