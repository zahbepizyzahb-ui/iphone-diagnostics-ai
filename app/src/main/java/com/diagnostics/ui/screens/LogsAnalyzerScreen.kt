package com.diagnostics.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.diagnostics.model.LogAnalysisResult
import com.diagnostics.ui.components.ActionButton
import com.diagnostics.ui.theme.*
import com.diagnostics.utils.FileUtils
import com.diagnostics.viewmodel.LogsAnalyzerViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsAnalyzerScreen(
    modifier: Modifier = Modifier,
    viewModel: LogsAnalyzerViewModel = viewModel()
) {
    val results by viewModel.results.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showPasteDialog by remember { mutableStateOf(false) }
    var pastedText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ Launcher لاستيراد الملفات (.ips, .txt, .crash)
    val filePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val fileName = FileUtils.getFileName(it, context.contentResolver)
                when {
                    fileName.endsWith(".ips") || fileName.endsWith(".ipsync") -> {
                        val content = FileUtils.readTextFromUri(it, context.contentResolver)
                        viewModel.analyzeFile(it, content)
                    }
                    fileName.endsWith(".txt") || fileName.endsWith(".crash") || fileName.endsWith(".log") -> {
                        // ✅ دعم ملفات TXT
                        val content = FileUtils.readTextFromUri(it, context.contentResolver)
                        viewModel.analyzeText(content)
                    }
                    else -> {
                        viewModel.showError("صيغة الملف غير مدعومة. استخدم .ips, .txt, .crash, أو .log")
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تحليل سجلات iPhone") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                ),
                actions = {
                    // ✅ زر مسح الكل
                    if (results.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearAllResults() }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "مسح الكل",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // أزرار الاستيراد
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    icon = Icons.Default.FileOpen,
                    label = "استيراد ملف",
                    color = PrimaryBlue,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        filePickerLauncher.launch("*/*") // ✅ دعم كل الصيغ
                    }
                )
                ActionButton(
                    icon = Icons.Default.ContentPaste,
                    label = "لصق نص",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { showPasteDialog = true }
                )
            }

            // ✅ تلميح للصيغ المدعومة
            Text(
                text = "الصيغ المدعومة: .ips | .txt | .crash | .log",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                textAlign = TextAlign.Center
            )

            // قائمة النتائج أو حالة فارغة
            if (results.isEmpty()) {
                EmptyLogsState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(results, key = { it.id }) { result ->
                        LogResultCard(
                            result = result,
                            onDelete = { viewModel.deleteResult(result) }
                        )
                    }
                }
            }
        }

        // محاورة لصق النص
        if (showPasteDialog) {
            AlertDialog(
                onDismissRequest = { showPasteDialog = false },
                title = { Text("لصق سجل الأعطال") },
                text = {
                    OutlinedTextField(
                        value = pastedText,
                        onValueChange = { pastedText = it },
                        label = { Text("نص السجل (.ips, .txt, .crash)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 10
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.analyzeText(pastedText)
                            showPasteDialog = false
                            pastedText = ""
                        },
                        enabled = pastedText.isNotBlank()
                    ) {
                        Text("تحليل")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPasteDialog = false }) {
                        Text("إلغاء")
                    }
                }
            )
        }

        // مؤشر التحميل
        if (isLoading) {
            LoadingOverlay("جاري تحليل السجل...")
        }

        // رسالة خطأ
        errorMessage?.let { msg ->
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                viewModel.clearError()
            }
        }
    }
}

@Composable
private fun LogResultCard(
    result: LogAnalysisResult,
    onDelete: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val severityColor = when (result.severity) {
        LogAnalysisResult.SeverityLevel.CRITICAL -> SeverityCritical
        LogAnalysisResult.SeverityLevel.HIGH -> SeverityHigh
        LogAnalysisResult.SeverityLevel.MEDIUM -> SeverityMedium
        LogAnalysisResult.SeverityLevel.LOW -> SeverityLow
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // الرأس
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SeverityBadge(severity = result.severity.label, color = severityColor)
                Text(
                    text = android.text.format.DateFormat.format("yyyy-MM-dd HH:mm", result.timestamp).toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // نوع الاستثناء
            Text(
                text = result.exceptionType,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (result.exceptionSubtype.isNotBlank()) {
                Text(
                    text = result.exceptionSubtype,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // المكون المتأثر
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "المتأثر: ${result.affectedComponent}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // السبب
            Text(
                text = "السبب: ${result.crashReason}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            // التفاصيل الموسعة
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "الحل المقترح:",
                    style = MaterialTheme.typography.titleSmall,
                    color = SuccessGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.suggestedFix,
                    style = MaterialTheme.typography.bodyMedium
                )

                // ✅ عرض السجل الخام
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "السجل الخام (أول 500 حرف):",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = result.rawLog.take(500),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { isExpanded = !isExpanded }) {
                    Text(if (isExpanded) "إخفاء التفاصيل" else "عرض التفاصيل")
                }

                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف")
                }
            }
        }
    }
}

@Composable
private fun SeverityBadge(severity: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = severity,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@Composable
private fun EmptyLogsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "لا توجد سجلات محللة",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "استورد ملف .ips أو .txt من iPhone أو الصق نص السجل لتحليله",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(horizontalAlignment = Alignment.Start) {
            GuideStep("1", "الإعدادات > الخصوصية > التحليل (لـ .ips)")
            GuideStep("2", "أو استورد ملف .txt / .crash / .log")
            GuideStep("3", "استيراده هنا للتحليل")
        }
    }
}

@Composable
private fun GuideStep(number: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = PrimaryBlue,
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = number,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
