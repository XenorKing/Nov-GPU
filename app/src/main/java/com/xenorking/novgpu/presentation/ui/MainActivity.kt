package com.xenorking.novgpu.presentation.ui

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
        enableEdgeToEdge()
        setContent {
            NovGpuTheme {
                val state by viewModel.state.collectAsStateWithLifecycle()
                NovGpuApp(
                    state = state,
                    onIntent = viewModel::processIntent
                )
            }
        }
    }
}

data class NavTab(val label: String, val icon: ImageVector)

val navTabs = listOf(
    NavTab("DASH", Icons.Default.Dashboard),
    NavTab("CPU", Icons.Default.Memory),
    NavTab("GPU/RAM", Icons.Default.Storage),
    NavTab("NET/TEMP", Icons.Default.NetworkCheck)
)

@Composable
fun NovGpuApp(
    state: com.xenorking.novgpu.presentation.state.MonitorState,
    onIntent: (MonitorIntent) -> Unit
) {
    Scaffold(
        containerColor = DarkBg,
        topBar = {
            NovGpuTopBar()
        },
        bottomBar = {
            NovGpuBottomBar(
                selectedTab = state.selectedTab,
                onTabSelected = { onIntent(MonitorIntent.SelectTab(it)) }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(DarkBg)
        ) {
            if (state.isLoading) {
                LoadingScreen()
            } else {
                AnimatedContent(
                    targetState = state.selectedTab,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                    },
                    label = "screen_transition"
                ) { tab ->
                    when (tab) {
                        0 -> DashboardScreen(stats = state.stats)
                        1 -> CpuScreen(cpu = state.stats.cpu)
                        2 -> GpuRamScreen(gpu = state.stats.gpu, ram = state.stats.ram)
                        3 -> NetworkTempScreen(network = state.stats.network, temperature = state.stats.temperature)
                        else -> DashboardScreen(stats = state.stats)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NovGpuTopBar() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "nov",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text = "GPU",
                    color = NeonCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(NeonGreen.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("LIVE", color = NeonGreen, fontSize = 9.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        actions = {
            Text(
                text = "v1.0",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 10.sp,
                modifier = Modifier.padding(end = 16.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkSurface,
            titleContentColor = Color.White
        )
    )
}

@Composable
private fun NovGpuBottomBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = DarkSurface,
        contentColor = Color.White
    ) {
        navTabs.forEachIndexed { index, tab ->
            val selected = selectedTab == index
            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        tab.icon,
                        contentDescription = tab.label,
                        tint = if (selected) NeonCyan else Color.White.copy(alpha = 0.4f)
                    )
                },
                label = {
                    Text(
                        tab.label,
                        color = if (selected) NeonCyan else Color.White.copy(alpha = 0.4f),
                        fontSize = 9.sp,
                        letterSpacing = 1.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NeonCyan,
                    indicatorColor = NeonCyan.copy(alpha = 0.15f)
                )
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize().background(DarkBg), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = NeonCyan, strokeWidth = 2.dp)
            Spacer(Modifier.height(16.dp))
            Text("INITIALIZING MONITORS...", color = NeonCyan.copy(alpha = 0.7f), fontSize = 12.sp, letterSpacing = 2.sp)
        }
    }
}
