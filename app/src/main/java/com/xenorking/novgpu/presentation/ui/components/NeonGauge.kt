package com.xenorking.novgpu.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
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
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "gauge"
    )

    val sweepAngle = 240f
    val startAngle = 150f
    val progress   = (animatedValue / maxValue).coerceIn(0f, 1f)

    // Пульсирующий блик
    val glowAlpha by rememberInfiniteTransition(label = "glow").animateFloat(
        initialValue = 0.10f, targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowAlpha"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val sw     = this.size.width * 0.07f
            val radius = (this.size.minDimension - sw) / 2f
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)

            // Фоновая дорожка
            drawArc(
                color = color.copy(alpha = 0.10f),
                startAngle = startAngle, sweepAngle = sweepAngle, useCenter = false,
                topLeft = topLeft, size = arcSize, style = Stroke(width = sw, cap = StrokeCap.Round)
            )

            if (progress > 0f) {
                // Мягкое свечение (широкий, прозрачный)
                drawArc(
                    color = color.copy(alpha = glowAlpha),
                    startAngle = startAngle, sweepAngle = sweepAngle * progress, useCenter = false,
                    topLeft = topLeft, size = arcSize,
                    style = Stroke(width = sw * 2.2f, cap = StrokeCap.Round)
                )
                // Основная дуга
                drawArc(
                    brush = Brush.sweepGradient(
                        colorStops = arrayOf(0f to color.copy(alpha = 0.4f), progress to color),
                        center = center
                    ),
                    startAngle = startAngle, sweepAngle = sweepAngle * progress, useCenter = false,
                    topLeft = topLeft, size = arcSize, style = Stroke(width = sw, cap = StrokeCap.Round)
                )
                // Точка конца
                val endAngle = Math.toRadians((startAngle + sweepAngle * progress).toDouble())
                val dotX = center.x + radius * cos(endAngle).toFloat()
                val dotY = center.y + radius * sin(endAngle).toFloat()
                drawCircle(color = color.copy(alpha = 0.5f), radius = sw * 0.9f, center = Offset(dotX, dotY))
                drawCircle(color = color, radius = sw * 0.5f, center = Offset(dotX, dotY))
                drawCircle(color = Color.White, radius = sw * 0.22f, center = Offset(dotX, dotY))
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (unit == "%") "${animatedValue.toInt()}" else "%.1f".format(animatedValue),
                color = color, fontSize = (size.value * 0.22f).sp, fontWeight = FontWeight.Bold
            )
            Text(text = unit, color = color.copy(alpha = 0.7f), fontSize = (size.value * 0.10f).sp)
            Text(text = label, color = Color.White.copy(alpha = 0.45f), fontSize = (size.value * 0.09f).sp, letterSpacing = 0.8.sp)
        }
    }
}
