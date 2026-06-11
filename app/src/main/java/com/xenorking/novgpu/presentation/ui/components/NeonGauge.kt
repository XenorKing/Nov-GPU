package com.xenorking.novgpu.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NeonGauge(
    value: Float,
    maxValue: Float = 100f,
    color: Color,
    label: String,
    unit: String = "%",
    size: Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    val animatedValue by animateFloatAsState(
        targetValue = value,
        animationSpec = tween(durationMillis = 500),
        label = "gauge_anim"
    )

    val sweepAngle = 240f
    val startAngle = 150f
    val progress = (animatedValue / maxValue).coerceIn(0f, 1f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = this.size.width * 0.08f
            val radius = (this.size.minDimension - strokeWidth) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val arcSize = Size(radius * 2, radius * 2)
            val arcTopLeft = Offset(center.x - radius, center.y - radius)

            // Background track
            drawArc(
                color = color.copy(alpha = 0.12f),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Glow layer
            if (progress > 0f) {
                drawArc(
                    color = color.copy(alpha = 0.3f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * progress,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth * 1.8f, cap = StrokeCap.Round)
                )
            }

            // Main arc
            if (progress > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0f to color.copy(alpha = 0.4f),
                        progress to color,
                        colors = listOf(color.copy(alpha = 0.5f), color)
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * progress,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Needle dot at end
            if (progress > 0f) {
                val angleRad = Math.toRadians((startAngle + sweepAngle * progress).toDouble())
                val dotX = center.x + radius * cos(angleRad).toFloat()
                val dotY = center.y + radius * sin(angleRad).toFloat()
                drawCircle(color = color, radius = strokeWidth * 0.7f, center = Offset(dotX, dotY))
                drawCircle(color = Color.White, radius = strokeWidth * 0.3f, center = Offset(dotX, dotY))
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (unit == "%") "${animatedValue.toInt()}" else "%.1f".format(animatedValue),
                color = color,
                fontSize = (size.value * 0.22f).sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unit,
                color = color.copy(alpha = 0.7f),
                fontSize = (size.value * 0.1f).sp
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = (size.value * 0.09f).sp,
                letterSpacing = 1.sp
            )
        }
    }
}
