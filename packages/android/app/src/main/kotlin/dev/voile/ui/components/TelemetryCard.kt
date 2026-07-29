package dev.voile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import dev.voile.core.tokens.VoileColors
import androidx.compose.ui.text.font.FontWeight

@Composable
fun TelemetryCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String? = null,
    modifier: Modifier = Modifier,
) {
    val fullDescription = "$label : $value${unit?.let { " $it" } ?: ""}"

    Surface(
        modifier = modifier
            .border(
                1.dp, Color(VoileColors.borderSoft), RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        color = Color(VoileColors.surface),
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(VoileColors.textMuted),
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(VoileColors.textMuted),
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(3.dp),
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(VoileColors.textPrimary),
                    modifier = Modifier.semantics { contentDescription = fullDescription }
                )
                if (unit != null) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(VoileColors.textMuted),
                    )
                }
            }
        }
    }
}
