package com.diagnostics.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.diagnostics.model.DetectedComponent
import com.diagnostics.ui.components.ActionButton
import com.diagnostics.ui.theme.*
import com.diagnostics.viewmodel.BoardScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScannerScreen(
    modifier: Modifier = Modifier,
    viewModel: BoardScannerViewModel = viewModel()
) {
    val scannedImage by viewModel.scannedImage.collectAsState()
    val detectedComponents by viewModel.detectedComponents.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // ✅ حالة تحليل AI
    val aiAnalysis by viewModel.aiAnalysis.collectAsState()
    val ocrText by viewModel.ocrText.collectAsState()

    val context = LocalContext.current

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let { viewModel.analyzeImageWithAI(it) }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.analyzeImageFromUri(it, context) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ماسح بورد iPhone") },
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
            // معاينة الصورة
            if (scannedImage != null) {
                ScannedImagePreview(
                    image = scannedImage!!,
                    components = detectedComponents,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .padding(16.dp)
                )
            } else {
                EmptyScannerState()
            }

            // أزرار التحكم
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionButton(
                    icon = Icons.Default.CameraAlt,
                    label = "كاميرا",
                    color = PrimaryBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { cameraLauncher.launch(null) }
                )
                ActionButton(
                    icon = Icons.Default.PhotoLibrary,
                    label = "معرض الصور",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { galleryLauncher.launch("image/*") }
                )
            }

            // ✅ نتائج تحليل AI
            AnimatedVisibility(visible = aiAnalysis != null || ocrText.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // نص OCR
                    if (ocrText.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = PrimaryBlue.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🔍 نص OCR المكتشف:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PrimaryBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ocrText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // تحليل AI
                    aiAnalysis?.let { analysis ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = SuccessGreen.copy(alpha = 0.1f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🤖 تحليل الذكاء الاصطناعي:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SuccessGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = analysis,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // قائمة المكونات
            if (detectedComponents.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "المكونات المكتشفة (${detectedComponents.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(detectedComponents, key = { it.id }) { component ->
                        ComponentCard(component = component)
                    }
                }
            }
        }

        // مؤشر التحميل
        if (isAnalyzing) {
            LoadingOverlay("جاري تحليل البورد بالذكاء الاصطناعي...")
        }
    }
}

@Composable
private fun ScannedImagePreview(
    image: android.graphics.Bitmap,
    components: List<DetectedComponent>,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.clip(RoundedCornerShape(16.dp))) {
        Image(
            bitmap = image.asImageBitmap(),
            contentDescription = "Scanned board",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        Box(modifier = Modifier.fillMaxSize()) {
            components.forEach { component ->
                ComponentBoundingBox(component = component)
            }
        }
    }
}

@Composable
private fun ComponentBoundingBox(component: DetectedComponent) {
    val color = if (component.isFaulty) ErrorRed else SuccessGreen

    Box(
        modifier = Modifier
            .offset(
                x = (component.position.x * 300).dp,
                y = (component.position.y * 300).dp
            )
    ) {
        Surface(
            color = color.copy(alpha = 0.2f),
            shape = RoundedCornerShape(4.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, color)
        ) {
            Column(modifier = Modifier.padding(4.dp)) {
                Text(
                    text = component.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
                component.partNumber?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun ComponentCard(component: DetectedComponent) {
    val icon = when (component.type) {
        DetectedComponent.ComponentType.CPU -> Icons.Default.Memory
        DetectedComponent.ComponentType.RAM -> Icons.Default.Storage
        DetectedComponent.ComponentType.BATTERY -> Icons.Default.BatteryFull
        DetectedComponent.ComponentType.CAMERA -> Icons.Default.CameraAlt
        DetectedComponent.ComponentType.WIFI_MODULE -> Icons.Default.Wifi
        else -> Icons.Default.Devices
    }

    val confidenceColor = when {
        component.confidence > 0.8 -> SuccessGreen
        component.confidence > 0.5 -> WarningOrange
        else -> ErrorRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (component.isFaulty) ErrorRed.copy(alpha = 0.1f) else PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (component.isFaulty) ErrorRed else PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = component.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = component.type.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                component.partNumber?.let {
                    Text(
                        text = "رقم القطعة: $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryBlue
                    )
                }
                if (component.notes.isNotBlank()) {
                    Text(
                        text = component.notes,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (component.isFaulty) ErrorRed else WarningOrange
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(component.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = confidenceColor
                )
                if (component.isFaulty) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Faulty",
                        tint = ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyScannerState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Devices,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "امسح بورد iPhone",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "استخدم الكاميرا لتصوير بورد iPhone. سيتم التحليل بالذكاء الاصطناعي للتعرف على المكونات",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "🤖 يدعم: ML Kit (مجاني) | OCR.space | Google Vision | Azure",
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryBlue,
            textAlign = TextAlign.Center
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
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "قد يستغرق بضع ثوانٍ...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
