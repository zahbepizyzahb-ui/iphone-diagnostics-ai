package com.diagnostics.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.diagnostics.ai.chatbot.AIChatbotScreen
import com.diagnostics.repairguide.RepairGuideScreen
import com.diagnostics.ui.theme.PrimaryBlue
import com.diagnostics.ui.screens.RepairTrackerScreen
import com.diagnostics.ui.screens.SettingsScreen

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Logs : BottomNavItem("logs", "السجلات", Icons.Filled.Home, Icons.Outlined.Home)
    object Scanner : BottomNavItem("scanner", "الماسح", Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt)
    object Diagnosis : BottomNavItem("diagnosis", "التشخيص", Icons.Filled.MedicalServices, Icons.Outlined.MedicalServices)
    object Charts : BottomNavItem("charts", "مخططات", Icons.Filled.BarChart, Icons.Outlined.BarChart)
    object Components : BottomNavItem("components", "القطع", Icons.Filled.Inventory, Icons.Outlined.Inventory)
    object More : BottomNavItem("more", "المزيد", Icons.Filled.Menu, Icons.Outlined.Menu)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    val items = listOf(
        BottomNavItem.Logs,
        BottomNavItem.Scanner,
        BottomNavItem.Diagnosis,
        BottomNavItem.Charts,
        BottomNavItem.Components,
        BottomNavItem.More
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selectedItem == index) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selectedItem == index,
                        onClick = { selectedItem = index }
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedItem) {
            0 -> LogsAnalyzerScreen(modifier = Modifier.padding(innerPadding))
            1 -> BoardScannerScreen(modifier = Modifier.padding(innerPadding))
            2 -> DiagnosisScreen(modifier = Modifier.padding(innerPadding))
            3 -> ChartsScreen(modifier = Modifier.padding(innerPadding))
            4 -> ComponentsDatabaseScreen(modifier = Modifier.padding(innerPadding))
            5 -> MoreScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(modifier: Modifier = Modifier) {
    var selectedScreen by rememberSaveable { mutableIntStateOf(0) }

    val screens = listOf(
        "🤖 مساعد AI",
        "📖 دليل الإصلاح",
        "📋 تتبع التصليحات",
        "⚙️ الإعدادات"
    )

    if (selectedScreen == 0) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("المزيد") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PrimaryBlue,
                        titleContentColor = androidx.compose.ui.graphics.Color.White
                    )
                )
            },
            modifier = modifier
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(screens) { index, title ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedScreen = index + 1 },
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = PrimaryBlue
                            )
                        }
                    }
                }
            }
        }
    } else {
        Box(modifier = modifier) {
            when (selectedScreen) {
                1 -> AIChatbotScreen()
                2 -> RepairGuideScreen()
                3 -> RepairTrackerScreen()
                4 -> SettingsScreen()
            }
            
            // إضافة زر العودة (اختياري ولكن مفيد للتجربة)
            IconButton(
                onClick = { selectedScreen = 0 },
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "عودة")
            }
        }
    }
}
