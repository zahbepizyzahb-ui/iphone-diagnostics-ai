package com.diagnostics.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.diagnostics.model.ComponentInfo
import com.diagnostics.model.DefaultComponents
import com.diagnostics.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentsDatabaseScreen(
    modifier: Modifier = Modifier,
    onComponentClick: (ComponentInfo) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<String?>(null) }
    var selectedComponent by remember { mutableStateOf<ComponentInfo?>(null) }

    val allComponents = remember { DefaultComponents.components }

    val filteredComponents = allComponents.filter { component ->
        val matchesSearch = searchQuery.isBlank() || 
            component.name.contains(searchQuery, ignoreCase = true) ||
            component.partNumber.contains(searchQuery, ignoreCase = true) ||
            component.description.contains(searchQuery, ignoreCase = true)

        val matchesType = selectedType == null || component.type == selectedType

        matchesSearch && matchesType
    }

    val componentTypes = allComponents.map { it.type }.distinct()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📚 قاعدة بيانات المكونات") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
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
            // شريط البحث
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("ابحث عن قطعة...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            // فلترة حسب النوع
            ScrollableTabRow(
                selectedTabIndex = if (selectedType == null) 0 else componentTypes.indexOf(selectedType) + 1,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedType == null,
                    onClick = { selectedType = null },
                    text = { Text("الكل") }
                )
                componentTypes.forEach { type ->
                    Tab(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        text = { Text(type) }
                    )
                }
            }

            // عدد النتائج
            Text(
                text = "${filteredComponents.size} قطعة",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // قائمة المكونات
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredComponents) { component ->
                    ComponentDatabaseCard(
                        component = component,
                        onClick = { selectedComponent = component }
                    )
                }
            }
        }
    }

    // تفاصيل القطعة
    selectedComponent?.let { component ->
        ComponentDetailDialog(
            component = component,
            onDismiss = { selectedComponent = null }
        )
    }
}

@Composable
private fun ComponentDatabaseCard(
    component: ComponentInfo,
    onClick: () -> Unit
) {
    val difficultyColor = when (component.replacementDifficulty) {
        ComponentInfo.Difficulty.EASY -> SuccessGreen
        ComponentInfo.Difficulty.MEDIUM -> WarningOrange
        ComponentInfo.Difficulty.HARD -> ErrorRed
        ComponentInfo.Difficulty.EXPERT -> Color(0xFF9C27B0)
    }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = component.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = component.partNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryBlue
                    )
                }

                Surface(
                    color = difficultyColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (component.replacementDifficulty) {
                            ComponentInfo.Difficulty.EASY -> "سهل"
                            ComponentInfo.Difficulty.MEDIUM -> "متوسط"
                            ComponentInfo.Difficulty.HARD -> "صعب"
                            ComponentInfo.Difficulty.EXPERT -> "خبير"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = difficultyColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = component.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoChip(
                    icon = Icons.Default.AttachMoney,
                    text = "$${component.averagePrice}",
                    color = SuccessGreen
                )
                InfoChip(
                    icon = Icons.Default.PhoneAndroid,
                    text = "${component.compatibleModels.size} موديل",
                    color = PrimaryBlue
                )
                InfoChip(
                    icon = Icons.Default.Warning,
                    text = "${component.commonFaults.size} عطل",
                    color = WarningOrange
                )
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    color: Color
) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun ComponentDetailDialog(
    component: ComponentInfo,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(component.name) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // رقم القطعة
                DetailRow("رقم القطعة", component.partNumber, PrimaryBlue)

                // الوصف
                Text(
                    text = component.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                HorizontalDivider()

                // الموديلات المتوافقة
                Text(
                    text = "📱 الموديلات المتوافقة:",
                    style = MaterialTheme.typography.titleSmall
                )
                component.compatibleModels.forEach { model ->
                    Text("   • $model", style = MaterialTheme.typography.bodySmall)
                }

                HorizontalDivider()

                // الأعطال الشائعة
                Text(
                    text = "⚠️ الأعطال الشائعة:",
                    style = MaterialTheme.typography.titleSmall,
                    color = WarningOrange
                )
                component.commonFaults.forEach { fault ->
                    Text("   • $fault", style = MaterialTheme.typography.bodySmall)
                }

                HorizontalDivider()

                // البدائل
                if (component.alternatives.isNotEmpty() && component.alternatives.first() != "لا يوجد") {
                    Text(
                        text = "🔄 البدائل المتاحة:",
                        style = MaterialTheme.typography.titleSmall,
                        color = SuccessGreen
                    )
                    component.alternatives.forEach { alt ->
                        Text("   • $alt", style = MaterialTheme.typography.bodySmall)
                    }
                }

                HorizontalDivider()

                // السعر والصعوبة
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailRow("السعر المتوسط", "$${component.averagePrice}", SuccessGreen)
                    DetailRow(
                        "الصعوبة",
                        when (component.replacementDifficulty) {
                            ComponentInfo.Difficulty.EASY -> "سهل"
                            ComponentInfo.Difficulty.MEDIUM -> "متوسط"
                            ComponentInfo.Difficulty.HARD -> "صعب"
                            ComponentInfo.Difficulty.EXPERT -> "خبير"
                        },
                        when (component.replacementDifficulty) {
                            ComponentInfo.Difficulty.EASY -> SuccessGreen
                            ComponentInfo.Difficulty.MEDIUM -> WarningOrange
                            ComponentInfo.Difficulty.HARD -> ErrorRed
                            ComponentInfo.Difficulty.EXPERT -> Color(0xFF9C27B0)
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = color
        )
    }
}
