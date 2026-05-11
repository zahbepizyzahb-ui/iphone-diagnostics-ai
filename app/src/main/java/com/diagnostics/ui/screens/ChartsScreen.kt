package com.diagnostics.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.diagnostics.R
import com.diagnostics.ui.theme.PrimaryBlue

data class ChartItem(
    val title: String,
    val description: String,
    val imageRes: String,
    val category: ChartCategory
) {
    enum class ChartCategory {
        FAULTS, PRICES, VOLTAGE, COMPARISON, PINOUT, FLOW, STATISTICS, THERMAL, COSTS, TIMES
    }
}

object ChartsData {
    val charts = listOf(
        ChartItem("أعطال iPhone الأكثر شيوعاً", "إحصائيات الأعطال الأكثر تكراراً", "iphone_common_faults", ChartItem.ChartCategory.FAULTS),
        ChartItem("مقارنة أسعار المكونات", "OEM vs Third Party", "component_prices", ChartItem.ChartCategory.PRICES),
        ChartItem("مخطط جهد البورد", "خريطة الجهد للمكونات", "voltage_map", ChartItem.ChartCategory.VOLTAGE),
        ChartItem("مقارنة موديلات iPhone", "جدول مقارنة المعالج والبطارية", "iphone_models_comparison", ChartItem.ChartCategory.COMPARISON),
        ChartItem("Pinout دائرة الشحن", "أرجل دائرة الشحن SN2012010", "charging_ic_pinout", ChartItem.ChartCategory.PINOUT),
        ChartItem("تدفق التشخيص", "مخطط تدفق التشخيص والإصلاح", "diagnosis_flowchart", ChartItem.ChartCategory.FLOW),
        ChartItem("إحصائيات الإصلاحات", "توزيع الإصلاحات والإيرادات", "repair_statistics", ChartItem.ChartCategory.STATISTICS),
        ChartItem("جدول جهد القياس", "قيم الجهد الطبيعية", "voltage_measurement_table", ChartItem.ChartCategory.VOLTAGE),
        ChartItem("تأثير صحة البطارية", "علاقة صحة البطارية بالأداء", "battery_health_impact", ChartItem.ChartCategory.COMPARISON),
        ChartItem("مشاكل iPhone حسب الموديل", "مقارنة الأعطال بين الموديلات", "iphone_models_issues", ChartItem.ChartCategory.FAULTS),
        ChartItem("خريطة حرارية للبورد", "درجات الحرارة لكل منطقة", "thermal_map", ChartItem.ChartCategory.THERMAL),
        ChartItem("تكلفة الإصلاحات", "تكلفة يد العمل + القطع", "repair_costs", ChartItem.ChartCategory.COSTS),
        ChartItem("أوقات الإصلاح", "الوقت المقدر لكل عملية", "repair_times", ChartItem.ChartCategory.TIMES)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(modifier: Modifier = Modifier) {
    var selectedCategory by remember { mutableStateOf<ChartItem.ChartCategory?>(null) }
    var selectedChart by remember { mutableStateOf<ChartItem?>(null) }

    val categories = ChartItem.ChartCategory.values()

    val filteredCharts = if (selectedCategory != null) {
        ChartsData.charts.filter { it.category == selectedCategory }
    } else {
        ChartsData.charts
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📊 المخططات والبيانات") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = androidx.compose.ui.graphics.Color.White
                )
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = if (selectedCategory == null) 0 else categories.indexOf(selectedCategory) + 1
            ) {
                Tab(selected = selectedCategory == null, onClick = { selectedCategory = null }) {
                    Text("الكل")
                }
                categories.forEach { cat ->
                    Tab(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    ) {
                        Text(
                            when (cat) {
                                ChartItem.ChartCategory.FAULTS -> "أعطال"
                                ChartItem.ChartCategory.PRICES -> "أسعار"
                                ChartItem.ChartCategory.VOLTAGE -> "جهد"
                                ChartItem.ChartCategory.COMPARISON -> "مقارنة"
                                ChartItem.ChartCategory.PINOUT -> "Pinout"
                                ChartItem.ChartCategory.FLOW -> "تدفق"
                                ChartItem.ChartCategory.STATISTICS -> "إحصائيات"
                                ChartItem.ChartCategory.THERMAL -> "حراري"
                                ChartItem.ChartCategory.COSTS -> "تكلفة"
                                ChartItem.ChartCategory.TIMES -> "وقت"
                            }
                        )
                    }
                }
            }

            Text(
                text = "${filteredCharts.size} مخطط",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredCharts) { chart ->
                    ChartCard(chart = chart, onClick = { selectedChart = chart })
                }
            }
        }
    }

    selectedChart?.let { chart ->
        AlertDialog(
            onDismissRequest = { selectedChart = null },
            title = { Text(chart.title) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = chart.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "📁 اسم الملف: ${chart.imageRes}.png",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedChart = null }) {
                    Text("إغلاق")
                }
            }
        )
    }
}

@Composable
private fun ChartCard(chart: ChartItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chart.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = chart.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryBlue.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "📊",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}
