package dev.voile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import dev.voile.core.tokens.VoileColors
import dev.voile.core.tokens.VoileColorsAlpha

private val DarkColors = darkColorScheme(
    primary = Color(VoileColors.secured),
    onPrimary = Color(VoileColors.bg),
    secondary = Color(VoileColors.connecting),
    onSecondary = Color(VoileColors.bg),
    background = Color(VoileColors.bg),
    onBackground = Color(VoileColors.textPrimary),
    surface = Color(VoileColors.surface),
    onSurface = Color(VoileColors.textPrimary),
    surfaceVariant = Color(VoileColors.surfaceElevated),
    onSurfaceVariant = Color(VoileColors.textSecondary),
    error = Color(VoileColors.danger),
    outline = Color(VoileColors.border),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1A9B8A),
    background = Color(0xFFF7F8FB),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF0A0F1C),
    onSurface = Color(0xFF0A0F1C),
)

@Composable
fun VoileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = VoileTypography,
        content = content
    )
}
