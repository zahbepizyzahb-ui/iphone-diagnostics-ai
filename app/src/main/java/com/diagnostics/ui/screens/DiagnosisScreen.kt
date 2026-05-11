package com.diagnostics.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diagnostics.model.DetectedComponent
import com.diagnostics.model.DiagnosisResult
import com.diagnostics.ui.theme.*
import com.diagnostics.viewmodel.DiagnosisViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisScreen(
    modifier: Modifier = Modifier,
    viewModel: DiagnosisViewModel = viewModel()
) {
    val diagnoses by viewModel.diagnoses.collectAsState()
    val isDiagnosing by viewModel.isDiagnosing.collectAsState()
    val overallHealth by viewModel.overallHealth.collectAsState()
    val batteryHealth by viewModel.batteryHealth.collectAsState()
    val performanceHealth by viewModel.performanceHealth.collectAsState()
    val storageHealth by viewModel.storageHealth.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("التشخيص الذكي") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // بطاقة صحة الجهاز
            item {
                HealthStatusCard(
                    overallHealth = overallHealth,
                    batteryHealth = batteryHealth,
                    performanceHealth = performanceHealth,
                    storageHealth = storageHealth
                )
            }

            // زر تشخيص جديد
            item {
                Button(
                    onClick = { viewModel.startFullDiagnosis() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "تشخيص شامل جديد",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            // نتائج سابقة
            if (diagnoses.isNotEmpty()) {
                item {
                    Text(
                        text = "نتائج التشخيص السابقة",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(diagnoses, key = { it.id }) { diagnosis ->
                    DiagnosisCard(diagnosis = diagnosis)
                }
            }

            // نصائح صيانة
            item {
                MaintenanceTipsSection()
            }
        }

        // مؤشر التحميل
        if (isDiagnosing) {
            LoadingOverlay("جاري التشخيص الشامل...")
        }
    }
}

@Composable
private fun HealthStatusCard(
    overallHealth: Double,
    batteryHealth: Double,
    performanceHealth: Double,
    storageHealth: Double
) {
    val animatedHealth by animateFloatAsState(
        targetValue = overallHealth.toFloat(),
        label = "health"
    )

    val healthColor = when {
        overallHealth > 0.8 -> SuccessGreen
        overallHealth > 0.5 -> WarningOrange
        else -> ErrorRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // دائرة التقدم
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = animatedHealth,
                    modifier = Modifier.size(120.dp),
                    color = healthColor,
                    strokeWidth = 12.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${(overallHealth * 100).toInt()}%",
                        style = MaterialTheme.typography.headlineMedium,
                        color = healthColor
                    )
                    Text(
                        text = "صحة الجهاز",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // مؤشرات فرعية
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                HealthIndicator("البطارية", batteryHealth, Icons.Default.BatteryFull)
                HealthIndicator("الأداء", performanceHealth, Icons.Default.Speed)
                HealthIndicator("التخزين", storageHealth, Icons.Default.Storage)
            }
        }
    }
}

@Composable
private fun HealthIndicator(title: String, value: Double, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val color = when {
        value > 0.7 -> SuccessGreen
        value > 0.4 -> WarningOrange
        else -> ErrorRed
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}

@Composable
private fun DiagnosisCard(diagnosis: DiagnosisResult) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = diagnosis.problem,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "${diagnosis.deviceModel} - iOS ${diagnosis.iosVersion}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ConfidenceBadge(confidence = diagnosis.confidence)
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // المكونات
                Text(
                    text = "المكونات المكتشفة:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                diagnosis.components.forEach { component ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (component.isFaulty) Icons.Default.Warning else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (component.isFaulty) ErrorRed else SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = component.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "${(component.confidence * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "التوصيات:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                diagnosis.recommendations.forEach { rec ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = rec,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { isExpanded = !isExpanded }) {
                Text(if (isExpanded) "إخفاء" else "عرض التفاصيل")
            }
        }
    }
}

@Composable
private fun ConfidenceBadge(confidence: Double) {
    val color = if (confidence > 0.8) SuccessGreen else WarningOrange
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "${(confidence * 100).toInt()}%",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@Composable
private fun MaintenanceTipsSection() {
    val tips = listOf(
        Triple("تجنب الشحن ليلاً", Icons.Default.BatteryChargingFull, "الشحن المستمر يؤثر على عمر البطارية"),
        Triple("تحديث iOS", Icons.Default.SystemUpdate, "التحديثات تحتوي على إصلاحات أمان"),
        Triple("تنظيف التخزين", Icons.Default.DeleteSweep, "الذاكرة الممتلئة تبطئ الجهاز"),
        Triple("إعادة التشغيل", Icons.Default.RestartAlt, "إعادة التشغيل الأسبوعية تحسن الأداء")
    )

    Column {
        Text(
            text = "نصائح صيانة",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tips.forEach { (title, icon, desc) ->
                TipCard(title = title, icon = icon, description = desc, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun TipCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = PrimaryBlue,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LoadingOverlay(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
