package com.diagnostics.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diagnostics.service.APIHealthStatus
import com.diagnostics.service.OCRTestResult
import com.diagnostics.service.TestStatus
import com.diagnostics.ui.theme.*
import com.diagnostics.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = viewModel()
) {
    val config by viewModel.config.collectAsState()
    val showSaved by viewModel.showSaved.collectAsState()
    val showAPIHelp by viewModel.showAPIHelp.collectAsState()

    // ✅ حالة اختبار API
    val apiTestResults by viewModel.apiTestResults.collectAsState()
    val isTestingAPI by viewModel.isTestingAPI.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("الإعدادات") },
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
                .verticalScroll(rememberScrollState())
        ) {
            // ✅ قسم اختبار API - ميزة جديدة
            SettingsSection(title = "🔧 اختبار API الذكاء الاصطناعي") {
                Text(
                    text = "اختبر الاتصال بـ Backend وخدمات OCR للتأكد من عملها",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Button(
                    onClick = { viewModel.runAPITests() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isTestingAPI,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (apiTestResults.isNotEmpty() && apiTestResults.all { it.status == TestStatus.PASSED }) 
                            SuccessGreen else PrimaryBlue
                    )
                ) {
                    if (isTestingAPI) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("جاري الاختبار...")
                    } else {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اختبر الاتصال الآن")
                    }
                }

                // عرض نتائج الاختبار
                AnimatedVisibility(visible = apiTestResults.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(top = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        apiTestResults.forEach { result ->
                            APITestResultCard(result = result)
                        }
                    }
                }
            }

            // قسم إعدادات AI
            SettingsSection(title = "إعدادات الذكاء الاصطناعي") {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = config.selectedOCRProvider.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("مزود OCR") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        com.diagnostics.model.APIConfiguration.OCRProvider.values().forEach { provider ->
                            DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(provider.label)
                                        if (provider == com.diagnostics.model.APIConfiguration.OCRProvider.ML_KIT_FREE) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                color = SuccessGreen,
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    "مجاني",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.updateProvider(provider)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            // مفاتيح API
            SettingsSection(title = "مفاتيح API (اختياري)") {
                Text(
                    text = "أدخل مفاتيح API للخدمات التي تريد استخدامها. ML Kit مجاني ولا يحتاج مفتاح.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                APIKeyField(
                    label = "OCR.space API Key",
                    value = config.ocrSpaceKey,
                    onValueChange = viewModel::updateOCRSpaceKey
                )
                APIKeyField(
                    label = "Google Vision API Key",
                    value = config.googleVisionKey,
                    onValueChange = viewModel::updateGoogleVisionKey
                )
                APIKeyField(
                    label = "Azure Vision Key",
                    value = config.azureVisionKey,
                    onValueChange = viewModel::updateAzureVisionKey
                )
                OutlinedTextField(
                    value = config.azureEndpoint,
                    onValueChange = viewModel::updateAzureEndpoint,
                    label = { Text("Azure Endpoint") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                APIKeyField(
                    label = "OpenAI API Key",
                    value = config.openAIKey,
                    onValueChange = viewModel::updateOpenAIKey
                )
            }

            // عنوان الخادم
            SettingsSection(title = "خادم Backend") {
                OutlinedTextField(
                    value = config.backendURL,
                    onValueChange = viewModel::updateBackendURL,
                    label = { Text("عنوان الخادم") },
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        Column {
                            Text("للمحاكي: http://10.0.2.2:8000")
                            Text("للجهاز: http://192.168.1.x:8000 (IP الكمبيوتر)")
                        }
                    }
                )
            }

            // معلومات
            SettingsSection(title = "معلومات") {
                InfoRow("الإصدار", "1.1.0")
                InfoRow("نظام التشغيل المدعوم", "Android 7+ (API 24+)")
                InfoRow("الترخيص", "MIT License")
                InfoRow("الميزات الجديدة", "اختبار API + تحليل AI للصور + دعم TXT")
            }

            // أزرار
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.saveSettings() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("حفظ الإعدادات")
                }

                OutlinedButton(
                    onClick = { viewModel.showAPIHelpDialog() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Help, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("كيفية الحصول على API Keys")
                }

                TextButton(
                    onClick = viewModel::resetSettings,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("إعادة تعيين")
                }
            }
        }
    }

    // رسالة تم الحفظ
    if (showSaved) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            viewModel.dismissSaved()
        }
    }

    // محاورة مساعدة API
    if (showAPIHelp) {
        APIHelpDialog(onDismiss = { viewModel.dismissAPIHelp() })
    }
}

@Composable
private fun APITestResultCard(result: com.diagnostics.service.APITestResult) {
    val (icon, color) = when (result.status) {
        TestStatus.PASSED -> Icons.Default.CheckCircle to SuccessGreen
        TestStatus.FAILED -> Icons.Default.Error to ErrorRed
        TestStatus.WARNING -> Icons.Default.Warning to WarningOrange
        TestStatus.SKIPPED -> Icons.Default.SkipNext to Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = color
                )
                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                color = color,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = when (result.status) {
                        TestStatus.PASSED -> "✓"
                        TestStatus.FAILED -> "✗"
                        TestStatus.WARNING -> "!"
                        TestStatus.SKIPPED -> "-"
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryBlue,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        content()
    }
    Divider()
}

@Composable
private fun APIKeyField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        singleLine = true
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun APIHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("كيفية الحصول على API Keys") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                APIProviderHelp(
                    name = "Google ML Kit (مجاني - لا يحتاج مفتاح)",
                    url = "مدمج في التطبيق",
                    steps = listOf(
                        "اختر ML Kit من القائمة",
                        "يعمل على الجهاز مباشرة",
                        "لا يحتاج إنترنت أو مفتاح"
                    ),
                    color = SuccessGreen
                )
                APIProviderHelp(
                    name = "OCR.space (مجاني - 25,000/شهر)",
                    url = "ocr.space",
                    steps = listOf(
                        "سجل حساباً مجانياً",
                        "انسخ API Key من لوحة التحكم",
                        "الحد: 25,000 طلب/شهر مجاناً"
                    ),
                    color = PrimaryBlue
                )
                APIProviderHelp(
                    name = "Google Vision (1,000/شهر مجاناً)",
                    url = "cloud.google.com/vision",
                    steps = listOf(
                        "أنشئ مشروعاً في Google Cloud",
                        "فعّل Vision API",
                        "أنشئ API Key"
                    ),
                    color = Color(0xFF4285F4)
                )
                APIProviderHelp(
                    name = "Azure Vision (5,000/شهر)",
                    url = "azure.microsoft.com",
                    steps = listOf(
                        "سجل في Azure Portal",
                        "أنشئ Computer Vision resource",
                        "انسخ Key و Endpoint"
                    ),
                    color = Color(0xFF0089D6)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("فهمت")
            }
        }
    )
}

@Composable
private fun APIProviderHelp(
    name: String,
    url: String,
    steps: List<String>,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = color.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = name,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = color
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            steps.forEachIndexed { index, step ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = color,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
