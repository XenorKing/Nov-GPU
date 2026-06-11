package com.xenorking.novgpu.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Neon Gaming Palette 2026
val NeonCyan = Color(0xFF00F5FF)
val NeonGreen = Color(0xFF00FF88)
val NeonPurple = Color(0xFFBB00FF)
val NeonOrange = Color(0xFFFF6600)
val NeonPink = Color(0xFFFF0080)
val NeonYellow = Color(0xFFFFFF00)
val NeonBlue = Color(0xFF0080FF)

val DarkBg = Color(0xFF050A0F)
val DarkSurface = Color(0xFF0D1520)
val DarkCard = Color(0xFF111C2A)
val DarkBorder = Color(0xFF1A2A3A)

val CpuColor = NeonCyan
val GpuColor = NeonPurple
val RamColor = NeonGreen
val TempColor = NeonOrange
val NetDownColor = NeonBlue
val NetUpColor = NeonPink

private val NeonColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonPurple,
    tertiary = NeonGreen,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onBackground = Color.White,
    onSurface = Color.White,
    onPrimary = DarkBg,
    outline = DarkBorder,
    error = NeonOrange
)

@Composable
fun NovGpuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NeonColorScheme,
        typography = NeonTypography,
        content = content
    )
}
