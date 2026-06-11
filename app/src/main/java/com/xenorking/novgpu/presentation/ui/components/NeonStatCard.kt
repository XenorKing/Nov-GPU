package com.xenorking.novgpu.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NeonStatCard(
    title: String,
    value: String,
    subtitle: String = "",
    color: Color,
    modifier: Modifier = Modifier
) {
    val animColor by animateColorAsState(
        targetValue = color, animationSpec = tween(400), label = "cardColor"
    )

    val glowAlpha by rememberInfiniteTransition(label = "cardGlow").animateFloat(
        initialValue = 0.06f, targetValue = 0.14f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "cardGlowAlpha"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        animColor.copy(alpha = 0.10f),
                        animColor.copy(alpha = 0.04f),
                        Color(0xFF0A0A1A)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(animColor.copy(alpha = glowAlpha * 2.5f), animColor.copy(alpha = glowAlpha))
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Column {
            Text(
                text = title,
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 11.sp,
                letterSpacing = 0.8.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                color = animColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
            if (subtitle.isNotEmpty()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun NeonProgressBar(
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 6.dp
) {
    val animProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "progress"
    )
    Box(
        modifier = modifier.height(height).clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier.fillMaxHeight()
                .fillMaxWidth(animProgress)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(listOf(color.copy(alpha = 0.6f), color))
                )
        )
    }
}
