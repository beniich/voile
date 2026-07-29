package com.example

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*

// ---------------------------------------------------------------------------
// Custom icons (vector paths)
// ---------------------------------------------------------------------------

val AntennaIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Antenna",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).path(
        stroke = androidx.compose.ui.graphics.SolidColor(Color.White),
        strokeLineWidth = 1.5f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    ) {
        moveTo(12f, 18f)
        verticalLineTo(6f)
        moveTo(12f, 6f)
        lineTo(9f, 9f)
        moveTo(12f, 6f)
        lineTo(15f, 9f)
        moveTo(5.636f, 15.364f)
        arcToRelative(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 0f, dy1 = -12.728f)
        moveTo(18.364f, 15.364f)
        arcToRelative(9f, 9f, 0f, isMoreThanHalf = false, isPositiveArc = false, dx1 = 0f, dy1 = -12.728f)
        moveTo(8.464f, 12.536f)
        arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, dx1 = 0f, dy1 = -5.656f)
        moveTo(15.536f, 12.536f)
        arcToRelative(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = false, dx1 = 0f, dy1 = -5.656f)
    }.build()

// ---------------------------------------------------------------------------
// ConnectButton
// ---------------------------------------------------------------------------

@Composable
fun ConnectButton(state: ConnectionState, onClick: () -> Unit, serverCity: String = "") {
    val color = when (state) {
        ConnectionState.Connected   -> Secured
        ConnectionState.Connecting  -> Connecting
        ConnectionState.Error       -> Danger
        ConnectionState.Disconnected -> Idle
    }

    val semanticLabel = when (state) {
        ConnectionState.Connected    -> "Déconnecter du serveur $serverCity"
        ConnectionState.Connecting   -> "Annuler la connexion au serveur $serverCity"
        ConnectionState.Error        -> "Réessayer la connexion au serveur $serverCity"
        ConnectionState.Disconnected -> "Se connecter au serveur $serverCity"
    }

    val animatedColor by animateColorAsState(targetValue = color, label = "color")
    val infiniteTransition = rememberInfiniteTransition(label = "rings")

    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "spin"
    )

    val buttonSize = 160.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .semantics { contentDescription = semanticLabel },
        contentAlignment = Alignment.Center
    ) {
        // Hardware-accelerated radar/glow
        Canvas(modifier = Modifier.size(300.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            if (state != ConnectionState.Disconnected) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(animatedColor.copy(alpha = 0.15f), Color.Transparent),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
            }

            if (state == ConnectionState.Connecting || state == ConnectionState.Connected) {
                val sweepAlpha = if (state == ConnectionState.Connecting) 0.4f else 0.2f
                val sweep = Brush.sweepGradient(
                    0.0f to Color.Transparent,
                    0.5f to Color.Transparent,
                    0.95f to animatedColor.copy(alpha = sweepAlpha * 0.6f),
                    1.0f to animatedColor.copy(alpha = sweepAlpha),
                    center = center
                )
                rotate(degrees = spinRotation, pivot = center) {
                    drawCircle(
                        brush = sweep,
                        radius = radius * 0.8f,
                        center = center
                    )
                }
            }
        }

        // Expanding rings
        // Connecting → 3 rings rapides | Connected → 1 ring lent (respiration) | Error → 0
        val ringCount = when (state) {
            ConnectionState.Connecting   -> 3
            ConnectionState.Connected    -> 1
            else                         -> 0
        }

        if (ringCount > 0) {
            for (i in 0 until ringCount) {
                val duration = if (state == ConnectionState.Connecting) 1800 else 4000
                val delay   = if (state == ConnectionState.Connecting) i * 600 else 0

                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (state == ConnectionState.Connecting) 1.6f else 1.5f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(duration, delayMillis = delay, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ), label = "scale_$i"
                )
                val alpha by infiniteTransition.animateFloat(
                    initialValue = if (state == ConnectionState.Connecting) 0.6f else 0.25f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(duration, delayMillis = delay, easing = LinearOutSlowInEasing),
                        repeatMode = RepeatMode.Restart
                    ), label = "alpha_$i"
                )

                Box(
                    modifier = Modifier
                        .size(buttonSize)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .border(2.dp, animatedColor, CircleShape)
                )
            }
        }

        // Main button
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(animatedColor.copy(alpha = 0.15f))
                .border(6.dp, animatedColor, CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(buttonSize * 0.65f)
                    .clip(CircleShape)
                    .border(2.dp, animatedColor.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dotRadius = 3.dp.toPx()
                    drawCircle(animatedColor, radius = dotRadius, center = Offset(size.width, 0f))
                    drawCircle(animatedColor, radius = dotRadius, center = Offset(0f, size.height))
                }

                when (state) {
                    ConnectionState.Error -> Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = animatedColor,
                        modifier = Modifier.size(48.dp)
                    )
                    else -> Icon(
                        imageVector = AntennaIcon,
                        contentDescription = null,
                        tint = animatedColor,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}
