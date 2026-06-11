package com.xenorking.novgpu.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun NeonProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    barHeight: Dp = 8.dp
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "progress_anim"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(barHeight)
    ) {
        val w = size.width
        val h = size.height
        val radius = CornerRadius(h / 2f)

        // Background track
        drawRoundRect(
            color = color.copy(alpha = 0.12f),
            cornerRadius = radius
        )

        if (animatedProgress > 0f) {
            val fillWidth = w * animatedProgress

            // Glow
            drawRoundRect(
                color = color.copy(alpha = 0.25f),
                size = Size(fillWidth, h * 1.5f),
                topLeft = Offset(0f, -h * 0.25f),
                cornerRadius = radius
            )

            // Main fill
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(color.copy(alpha = 0.6f), color),
                    startX = 0f,
                    endX = fillWidth
                ),
                size = Size(fillWidth, h),
                cornerRadius = radius
            )

            // Shine highlight
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White.copy(alpha = 0.3f), Color.Transparent),
                    startY = 0f,
                    endY = h / 2f
                ),
                size = Size(fillWidth, h / 2f),
                cornerRadius = radius
            )
        }
    }
}
