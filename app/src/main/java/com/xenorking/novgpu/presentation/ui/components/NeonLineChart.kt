package com.xenorking.novgpu.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NeonLineChart(
    data: List<Float>,
    maxValue: Float = 100f,
    color: Color,
    label: String = "",
    height: Dp = 80.dp,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue = if (data.isEmpty()) 0f else 1f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "chartAnim"
    )

    val glowAlpha by rememberInfiniteTransition(label = "chartGlow").animateFloat(
        initialValue = 0.08f, targetValue = 0.18f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Reverse),
        label = "chartGlowAlpha"
    )

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(text = label, color = color.copy(alpha = 0.7f), fontSize = 11.sp, letterSpacing = 0.8.sp)
            Spacer(Modifier.height(4.dp))
        }
        Canvas(modifier = Modifier.fillMaxWidth().height(height)) {
            if (data.size < 2) return@Canvas
            val w = size.width
            val h = size.height
            val visibleCount = (data.size * animProgress).toInt().coerceAtLeast(2).coerceAtMost(data.size)
            val visible = data.takeLast(visibleCount)
            val step = w / (visible.size - 1).toFloat()

            fun yFor(v: Float) = h - (v / maxValue.coerceAtLeast(1f)).coerceIn(0f, 1f) * h * 0.9f

            val points = visible.mapIndexed { i, v -> Offset(i * step, yFor(v)) }

            // Область под кривой
            val path = Path().apply {
                moveTo(points.first().x, h)
                points.forEach { lineTo(it.x, it.y) }
                lineTo(points.last().x, h)
                close()
            }
            drawPath(path, Brush.verticalGradient(listOf(color.copy(alpha = 0.15f), Color.Transparent)))

            // Мягкое свечение линии
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val cp = Offset((points[i-1].x + points[i].x) / 2f, (points[i-1].y + points[i].y) / 2f)
                    quadraticTo(points[i-1].x, points[i-1].y, cp.x, cp.y)
                }
                lineTo(points.last().x, points.last().y)
            }
            drawPath(linePath, color.copy(alpha = glowAlpha), style = Stroke(width = 6f, cap = StrokeCap.Round))
            drawPath(linePath, color, style = Stroke(width = 2f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            // Последняя точка
            points.lastOrNull()?.let { pt ->
                drawCircle(color.copy(alpha = 0.25f), radius = 8f, center = pt)
                drawCircle(color, radius = 4f, center = pt)
            }
        }
    }
}
