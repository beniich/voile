package com.example

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

private fun formatDataAmount(mb: Float): String {
    return if (mb >= 1024f) {
        String.format("%.2f GB", mb / 1024f)
    } else {
        String.format("%.1f MB", mb)
    }
}

@Composable
fun AnalyticsScreen(viewModel: VoileViewModel) {
    val telemetry by viewModel.telemetry.collectAsState()
    val state by viewModel.connectionState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Timer Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = "Durée de session: ${formatDuration(telemetry.session)}" },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = formatDuration(telemetry.session),
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Durée de Session",
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            StatusPill(state)
        }

        // Charts
        LineChartCard(
            title = "Vitesse de Téléchargement",
            currentValue = "${telemetry.down} Mbps",
            data = telemetry.downHistory,
            lineColor = Secured,
            maxVal = 200f
        )

        LineChartCard(
            title = "Vitesse d'Envoi",
            currentValue = "${telemetry.up} Mbps",
            data = telemetry.upHistory,
            lineColor = Connecting,
            maxVal = 50f
        )

        // Summary Cards
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SummaryCard(
                title = "Total Téléchargé",
                value = formatDataAmount(telemetry.totalDown),
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                title = "Total Envoyé",
                value = formatDataAmount(telemetry.totalUp),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun LineChartCard(
    title: String,
    currentValue: String,
    data: List<Float>,
    lineColor: Color,
    maxVal: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, BorderSoft, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .semantics { contentDescription = "$title: $currentValue" }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
            Text(currentValue, fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            val width = size.width
            val height = size.height
            val points = data.mapIndexed { index, value ->
                val x = index * (width / (data.size - 1))
                val y = height - ((value / maxVal) * height).coerceIn(0f, height)
                Offset(x, y)
            }

            val path = Path().apply {
                if (points.isNotEmpty()) {
                    moveTo(points.first().x, points.first().y)
                    for (i in 0 until points.size - 1) {
                        val p1 = points[i]
                        val p2 = points[i + 1]
                        val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                        val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)
                        cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p2.x, p2.y)
                    }
                }
            }
            
            // Draw gradient below line
            val fillPath = Path().apply {
                addPath(path)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.2f), Color.Transparent)
                )
            )

            // Draw line
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Surface)
            .border(1.dp, BorderSoft, RoundedCornerShape(16.dp))
            .padding(16.dp)
            .semantics { contentDescription = "$title: $value" }
    ) {
        Text(title, fontSize = 13.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(8.dp))
        Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
