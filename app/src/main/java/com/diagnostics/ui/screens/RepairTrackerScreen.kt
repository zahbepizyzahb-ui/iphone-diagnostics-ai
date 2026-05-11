package com.diagnostics.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.diagnostics.data.database.RepairRecord
import com.diagnostics.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairTrackerScreen() {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedStatus by remember { mutableStateOf<RepairRecord.RepairStatus?>(null) }

    // بيانات وهمية للعرض
    val repairs = remember {
        listOf(
            RepairRecord(
                deviceModel = "iPhone 14 Pro",
                customerName = "أحمد محمد",
                problem = "الشاشة سوداء",
                diagnosis = "تلف دائرة الباك لايت",
                componentsReplaced = "[\"U3700 - Backlight IC\"]",
                cost = 150.0,
                status = RepairRecord.RepairStatus.COMPLETED,
                notes = "تم الإصلاح بنجاح"
            ),
            RepairRecord(
                deviceModel = "iPhone 13",
                customerName = "خالد العلي",
                problem = "لا يشحن",
                diagnosis = "تلف دائرة الشحن",
                componentsReplaced = "[\"SN2012010\"]",
                cost = 80.0,
                status = RepairRecord.RepairStatus.IN_PROGRESS,
                notes = "في انتظار القطعة"
            ),
            RepairRecord(
                deviceModel = "iPhone 14 Pro Max",
                customerName = "فاطمة الزهراء",
                problem = "سخونة مفرطة",
                diagnosis = "تسريب تيار في Power IC",
                componentsReplaced = "[]",
                cost = 0.0,
                status = RepairRecord.RepairStatus.PENDING,
                notes = "بانتظار الفحص"
            )
        )
    }

    val filteredRepairs = if (selectedStatus != null) {
        repairs.filter { it.status == selectedStatus }
    } else repairs

    val stats = remember {
        mapOf(
            "إجمالي" to repairs.size,
            "مكتمل" to repairs.count { it.status == RepairRecord.RepairStatus.COMPLETED },
            "قيد العمل" to repairs.count { it.status == RepairRecord.RepairStatus.IN_PROGRESS },
            "معلق" to repairs.count { it.status == RepairRecord.RepairStatus.PENDING }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📋 تتبع التصليحات") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = PrimaryBlue
            ) {
                Icon(Icons.Default.Add, "إضافة", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // إحصائيات
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard("إجمالي", stats["إجمالي"] ?: 0, PrimaryBlue)
                StatCard("مكتمل", stats["مكتمل"] ?: 0, SuccessGreen)
                StatCard("قيد العمل", stats["قيد العمل"] ?: 0, WarningOrange)
                StatCard("معلق", stats["معلق"] ?: 0, Color.Gray)
            }

            // فلترة
            ScrollableTabRow(
                selectedTabIndex = if (selectedStatus == null) 0 else 
                    RepairRecord.RepairStatus.values().indexOf(selectedStatus) + 1
            ) {
                Tab(selected = selectedStatus == null, onClick = { selectedStatus = null }) {
                    Text("الكل")
                }
                RepairRecord.RepairStatus.values().forEach { status ->
                    Tab(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status }
                    ) {
                        Text(status.arabicName)
                    }
                }
            }

            // قائمة التصليحات
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredRepairs) { repair ->
                    RepairCard(repair = repair)
                }
            }
        }
    }

    if (showAddDialog) {
        AddRepairDialog(onDismiss = { showAddDialog = false })
    }
}

private val RepairRecord.RepairStatus.arabicName: String
    get() = when (this) {
        RepairRecord.RepairStatus.PENDING -> "معلق"
        RepairRecord.RepairStatus.IN_PROGRESS -> "قيد العمل"
        RepairRecord.RepairStatus.WAITING_PARTS -> "بانتظار القطع"
        RepairRecord.RepairStatus.COMPLETED -> "مكتمل"
        RepairRecord.RepairStatus.CANCELLED -> "ملغي"
    }

@Composable
private fun StatCard(label: String, value: Int, color: Color) {
    Card(
        modifier = Modifier.size(80.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RepairCard(repair: RepairRecord) {
    val statusColor = when (repair.status) {
        RepairRecord.RepairStatus.COMPLETED -> SuccessGreen
        RepairRecord.RepairStatus.IN_PROGRESS -> WarningOrange
        RepairRecord.RepairStatus.PENDING -> Color.Gray
        RepairRecord.RepairStatus.WAITING_PARTS -> PrimaryBlue
        RepairRecord.RepairStatus.CANCELLED -> Color.Red
    }

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
                        text = repair.deviceModel,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = repair.customerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = repair.status.arabicName,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = statusColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "المشكلة: ${repair.problem}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "التشخيص: ${repair.diagnosis}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "💰 $${repair.cost}",
                    style = MaterialTheme.typography.labelLarge,
                    color = SuccessGreen
                )
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(repair.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AddRepairDialog(onDismiss: () -> Unit) {
    var deviceModel by remember { mutableStateOf("") }
    var customerName by remember { mutableStateOf("") }
    var problem by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة تصليح جديد") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = deviceModel,
                    onValueChange = { deviceModel = it },
                    label = { Text("موديل الجهاز") }
                )
                OutlinedTextField(
                    value = customerName,
                    onValueChange = { customerName = it },
                    label = { Text("اسم العميل") }
                )
                OutlinedTextField(
                    value = problem,
                    onValueChange = { problem = it },
                    label = { Text("المشكلة") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                enabled = deviceModel.isNotBlank() && problem.isNotBlank()
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
