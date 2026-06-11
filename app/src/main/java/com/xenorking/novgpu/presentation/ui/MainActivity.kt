package com.xenorking.novgpu.presentation.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.xenorking.novgpu.presentation.state.MonitorIntent
import com.xenorking.novgpu.presentation.ui.screens.*
import com.xenorking.novgpu.presentation.ui.theme.*
import com.xenorking.novgpu.presentation.viewmodel.MonitorViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MonitorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NovGpuTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                NovGPUApp(
                    state       = state,
                    onTabSelect = { viewModel.handleIntent(MonitorIntent.SelectTab(it)) }
                )
            }
        }
    }
}

@Composable
fun NovGPUApp(
    state: com.xenorking.novgpu.presentation.state.MonitorState,
    onTabSelect: (Int) -> Unit
) {
    val tabs = listOf(
        TabItem("ГЛАВНАЯ",   "⬡"),
        TabItem("ПРОЦЕССОР", "◈"),
        TabItem("GPU/ОЗУ",   "◉"),
        TabItem("СЕТЬ",      "◎"),
        TabItem("О НАС",     "ℹ")
    )

    Scaffold(
        containerColor = Color(0xFF080810),
        bottomBar = {
            NovGPUBottomBar(
                tabs        = tabs,
                selectedTab = state.selectedTab,
                onTabSelect = onTabSelect
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF0A0A1A), Color(0xFF060610), Color(0xFF080818))
                    )
                )
                .padding(padding)
        ) {
            when (state.selectedTab) {
                0 -> DashboardScreen(stats = state.stats)
                1 -> CpuScreen(stats = state.stats)
                2 -> GpuRamScreen(stats = state.stats)
                3 -> NetworkTempScreen(stats = state.stats)
                4 -> AboutScreen()
            }
        }
    }
}

@Composable
private fun NovGPUBottomBar(
    tabs: List<TabItem>,
    selectedTab: Int,
    onTabSelect: (Int) -> Unit
) {
    val tabColors = listOf(NeonCyan, NeonCyan, NeonGreen, NeonBlue, NeonPurple)

    NavigationBar(
        containerColor = Color(0xFF0C0C1A),
        tonalElevation = 0.dp,
        modifier = Modifier.height(62.dp)
    ) {
        tabs.forEachIndexed { index, tab ->
            val selected = selectedTab == index
            val color = tabColors.getOrElse(index) { NeonCyan }

            val scale by animateFloatAsState(
                targetValue = if (selected) 1.15f else 1f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "scale_$index"
            )

            NavigationBarItem(
                selected = selected,
                onClick  = { onTabSelect(index) },
                icon = {
                    Text(
                        text = tab.icon,
                        fontSize = (16f * scale).sp,
                        color = if (selected) color else Color.White.copy(alpha = 0.3f)
                    )
                },
                label = {
                    Text(
                        text = tab.label,
                        fontSize = 7.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) color else Color.White.copy(alpha = 0.3f),
                        letterSpacing = 0.2.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIndicatorColor = color.copy(alpha = 0.14f),
                    unselectedIconColor    = Color.White.copy(alpha = 0.3f),
                    unselectedTextColor    = Color.White.copy(alpha = 0.3f),
                    selectedIconColor      = color,
                    selectedTextColor      = color
                )
            )
        }
    }
}

data class TabItem(val label: String, val icon: String)
