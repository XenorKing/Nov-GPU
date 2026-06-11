package com.xenorking.novgpu.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NeonLineChart(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
    height: Dp = 80.dp,
    maxValue: Float = 100f,
    showGrid: Boolean = true
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        if (data.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height
        val padding = 4f

        val effectiveMax = maxValue.coerceAtLeast(data.maxOrNull() ?: 1f)

        // Grid lines
        if (showGrid) {
            val gridAlpha = 0.08f
            repeat(4) { i ->
                val y = h - (i + 1) * h / 4f
                drawLine(
                    color = color.copy(alpha = gridAlpha),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }
        }

        if (data.size < 2) return@Canvas

        val step = w / (data.size - 1).toFloat()

        // Build path
        val linePath = Path()
        val fillPath = Path()

        data.forEachIndexed { i, value ->
            val x = i * step
            val y = h - padding - ((value / effectiveMax) * (h - padding * 2)).coerceIn(0f, h - padding * 2)
            if (i == 0) {
                linePath.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                // Smooth curve
                val prevX = (i - 1) * step
                val prevY = h - padding - ((data[i - 1] / effectiveMax) * (h - padding * 2)).coerceIn(0f, h - padding * 2)
                val cpX = (prevX + x) / 2f
                linePath.cubicTo(cpX, prevY, cpX, y, x, y)
                fillPath.cubicTo(cpX, prevY, cpX, y, x, y)
            }
        }

        val lastX = (data.size - 1) * step
        fillPath.lineTo(lastX, h)
        fillPath.close()

        // Glow effect
        drawPath(
            path = linePath,
            color = color.copy(alpha = 0.4f),
            style = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Fill gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.25f), Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        // Main line
        drawPath(
            path = linePath,
            color = color,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Latest value dot
        val lastIdx = data.size - 1
        val lastY = h - padding - ((data[lastIdx] / effectiveMax) * (h - padding * 2)).coerceIn(0f, h - padding * 2)
        drawCircle(color = color.copy(alpha = 0.4f), radius = 8f, center = Offset(lastX, lastY))
        drawCircle(color = color, radius = 4f, center = Offset(lastX, lastY))
        drawCircle(color = Color.White, radius = 2f, center = Offset(lastX, lastY))
    }
}
