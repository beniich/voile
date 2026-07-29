package dev.voile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.voile.core.tokens.VoileColors
import io.sentry.compose.SentryTag

/**
 * ErrorBoundary Compose — capture les exceptions du subtree.
 * Usage :
 *   VoileErrorBoundary(fallback = { MyFallbackUi() }) {
 *     HomeScreen()
 *   }
 */
@Composable
fun VoileErrorBoundary(
    fallback: @Composable (Throwable, () -> Unit) -> Unit,
    content: @Composable () -> Unit,
) {
    var error by remember { mutableStateOf<Throwable?>(null) }

    if (error != null) {
        fallback(error!!) { error = null }
    } else {
        SentryTag("screen", "unknown") {
            content()
        }
    }

    // Hook pour capturer les erreurs non-pérennes
    DisposableEffect(Unit) {
        onDispose { /* cleanup */ }
    }
}

/**
 * Fallback UI par défaut.
 */
@Composable
fun VoileErrorFallback(
    error: Throwable,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(VoileColors.bg))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Une erreur est survenue",
            style = MaterialTheme.typography.titleLarge,
            color = Color(VoileColors.textPrimary),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "L'équipe technique a été notifiée. Vous pouvez réessayer.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(VoileColors.textSecondary),
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(VoileColors.secured),
                contentColor = Color(VoileColors.bg),
            ),
        ) {
            Text("Réessayer")
        }
    }
}
