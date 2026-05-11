package com.diagnostics.repairguide

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.diagnostics.ui.theme.*

data class RepairStep(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val warnings: List<String> = emptyList(),
    val tools: List<String> = emptyList(),
    val difficulty: String = "EASY",
    val estimatedTime: String = "5 دقائق"
)

data class RepairGuide(
    val id: String,
    val title: String,
    val description: String,
    val component: String,
    val difficulty: String,
    val estimatedTime: String,
    val steps: List<RepairStep>,
    val requiredTools: List<String>,
    val videoUrl: String = ""
)

object RepairGuidesData {
    val guides = listOf(
        RepairGuide(
            id = "1",
            title = "استبدال بطارية iPhone 14",
            description = "دليل كامل لاستبدال بطارية iPhone 14 بشكل آمن",
            component = "بطارية",
            difficulty = "EASY",
            estimatedTime = "15-20 دقيقة",
            requiredTools = listOf(
                "مفك Pentalobe",
                "مفك Tri-wing Y000",
                "مسدس هواء ساخن",
                "شفاط شاشة",
                "ملقط ESD",
                "لاصق بطارية جديد"
            ),
            steps = listOf(
                RepairStep(
                    stepNumber = 1,
                    title = "إيقاف الجهاز",
                    description = "أغلق الجهاز تماماً. اضغط على زر الطاقة + زر الصوت حتى يظهر شريط التمرير، ثم اسحب لإيقاف التشغيل.",
                    warnings = listOf("لا تبدأ العمل والجهاز مشغل"),
                    tools = listOf("لا يحتاج أدوات"),
                    estimatedTime = "1 دقيقة"
                ),
                RepairStep(
                    stepNumber = 2,
                    title = "فك براغي الجزء السفلي",
                    description = "استخدم مفك Pentalobe لفك البراغي الموجودة على جانبي منفذ الشحن. احتفظ بالبراغي في مكان آمن.",
                    warnings = listOf("لا تفقد البراغي", "تأكد من استخدام المفك المناسب"),
                    tools = listOf("مفك Pentalobe"),
                    estimatedTime = "2 دقائق"
                ),
                RepairStep(
                    stepNumber = 3,
                    title = "فتح الشاشة",
                    description = "سخن حواف الشاشة بالمسدس الحراري حتى 60°C. استخدم شفاط الشاشة لفتحها برفق من الأسفل.",
                    warnings = listOf("لا تسخن أكثر من 80°C", "كن حذراً مع كابل الشاشة"),
                    tools = listOf("مسدس حراري", "شفاط شاشة"),
                    estimatedTime = "5 دقائق"
                ),
                RepairStep(
                    stepNumber = 4,
                    title = "فصل كابل البطارية",
                    description = "افتح الغطاء المعدني فوق كابل البطارية بمفك Y000. ارفع الكابل برفق باستخدام الملقط.",
                    warnings = listOf("لا تستخدم قوة زائدة", "تأكد من فصل البطارية أولاً"),
                    tools = listOf("مفك Y000", "ملقط ESD"),
                    estimatedTime = "3 دقائق"
                ),
                RepairStep(
                    stepNumber = 5,
                    title = "إزالة البطارية القديمة",
                    description = "اسحب ألسنة اللاصق من البطارية ببطء. إذا صعبت، استخدم كحول إيزوبروبيل 99% لتذويب اللاصق.",
                    warnings = listOf("لا تثقب البطارية", "البطارية المتضخمة خطيرة"),
                    tools = listOf("ملقط", "كحول 99%"),
                    estimatedTime = "5 دقائق"
                ),
                RepairStep(
                    stepNumber = 6,
                    title = "تركيب البطارية الجديدة",
                    description = "أزل الغشاء الواقي من اللاصق الجديد. ضع البطارية في مكانها الصحيح واضغط برفق.",
                    warnings = listOf("تأكد من توجيه البطارية الصحيح"),
                    tools = listOf("بطارية جديدة"),
                    estimatedTime = "3 دقائق"
                ),
                RepairStep(
                    stepNumber = 7,
                    title = "إعادة التجميع",
                    description = "أعد توصيل كابل البطارية. أغلق الغطاء المعدني. أعد تركيب الشاشة والبراغي.",
                    warnings = listOf("تأكد من توصيل كل الكابلات"),
                    tools = listOf("مفك Pentalobe", "مفك Y000"),
                    estimatedTime = "5 دقائق"
                )
            )
        ),
        RepairGuide(
            id = "2",
            title = "إصلاح دائرة الشحن",
            description = "دليل فحص وإصلاح دائرة الشحن في iPhone",
            component = "CHARGING_IC",
            difficulty = "HARD",
            estimatedTime = "45-60 دقيقة",
            requiredTools = listOf(
                "مجهر",
                "محطة لحام",
                "سلك لحام",
                "فلكس",
                "مقياس متعدد"
            ),
            steps = listOf(
                RepairStep(
                    stepNumber = 1,
                    title = "فحص الجهد",
                    description = "استخدم مقياس متعدد لفحص الجهد على أرجل دائرة الشحن. يجب أن يكون 5V على VBUS.",
                    warnings = listOf("لا تقصر الدائرة"),
                    tools = listOf("مقياس متعدد"),
                    estimatedTime = "5 دقائق"
                ),
                RepairStep(
                    stepNumber = 2,
                    title = "فحص دائرة الشحن",
                    description = "افحص دائرة الشحن SN2012010 بمجهر. ابحث عن تلف ظاهر أو حروق.",
                    warnings = listOf("لا تلمس الدائرة بدون ESD"),
                    tools = listOf("مجهر"),
                    estimatedTime = "10 دقائق"
                ),
                RepairStep(
                    stepNumber = 3,
                    title = "إزالة الدائرة التالفة",
                    description = "استخدم محطة لحام لإزالة الدائرة التالفة. سخن بشكل متساوٍ حتى تذوب اللحامات.",
                    warnings = listOf("لا تسخن أكثر من اللازم", "قد تتلف الدائرة المجاورة"),
                    tools = listOf("محطة لحام", "ملقط"),
                    estimatedTime = "15 دقيقة"
                ),
                RepairStep(
                    stepNumber = 4,
                    title = "تنظيف المساحة",
                    description = "نظف المساحة بشاشة التنظيف. تأكد من عدم وجود كريات لحام قصيرة.",
                    warnings = listOf("تأكد من عدم وجود قصر"),
                    tools = listOf("شاشة تنظيف", "كحول"),
                    estimatedTime = "10 دقائق"
                ),
                RepairStep(
                    stepNumber = 5,
                    title = "تركيب الدائرة الجديدة",
                    description = "ضع الدائرة الجديدة بمحاذاة دقيقة. استخدم محطة لحام مع فلكس لإعادة اللحام.",
                    warnings = listOf("المحاذاة مهمة جداً", "لا تتحرك أثناء اللحام"),
                    tools = listOf("محطة لحام", "فلكس", "سلك لحام"),
                    estimatedTime = "20 دقيقة"
                )
            )
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepairGuideScreen() {
    var selectedGuide by remember { mutableStateOf<RepairGuide?>(null) }
    var currentStep by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📖 دليل الإصلاح") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (selectedGuide == null) {
            // قائمة الأدلة
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(RepairGuidesData.guides) { guide ->
                    RepairGuideCard(guide = guide, onClick = {
                        selectedGuide = guide
                        currentStep = 0
                    })
                }
            }
        } else {
            // عرض الدليل
            GuideDetailView(
                guide = selectedGuide!!,
                currentStep = currentStep,
                onStepChange = { currentStep = it },
                onBack = { selectedGuide = null }
            )
        }
    }
}

@Composable
private fun RepairGuideCard(guide: RepairGuide, onClick: () -> Unit) {
    val difficultyColor = when (guide.difficulty) {
        "EASY" -> SuccessGreen
        "MEDIUM" -> WarningOrange
        "HARD" -> ErrorRed
        else -> Color.Gray
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = guide.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = guide.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    color = difficultyColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = when (guide.difficulty) {
                            "EASY" -> "سهل"
                            "MEDIUM" -> "متوسط"
                            "HARD" -> "صعب"
                            else -> guide.difficulty
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = difficultyColor,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InfoChip(Icons.Default.Schedule, guide.estimatedTime, PrimaryBlue)
                InfoChip(Icons.Default.List, "${guide.steps.size} خطوة", SuccessGreen)
                InfoChip(Icons.Default.Build, "${guide.requiredTools.size} أداة", WarningOrange)
            }
        }
    }
}

@Composable
private fun GuideDetailView(
    guide: RepairGuide,
    currentStep: Int,
    onStepChange: (Int) -> Unit,
    onBack: () -> Unit
) {
    val step = guide.steps[currentStep]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // شريط التقدم
        LinearProgressIndicator(
            progress = (currentStep + 1).toFloat() / guide.steps.size,
            modifier = Modifier.fillMaxWidth(),
            color = PrimaryBlue
        )

        Text(
            text = "الخطوة ${currentStep + 1} من ${guide.steps.size}",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // عنوان الخطوة
        Text(
            text = "${step.stepNumber}. ${step.title}",
            style = MaterialTheme.typography.headlineSmall,
            color = PrimaryBlue
        )

        Spacer(modifier = Modifier.height(16.dp))

        // محتوى الخطوة
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge
                )

                // التحذيرات
                if (step.warnings.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = ErrorRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⚠️ تحذيرات:",
                                color = ErrorRed,
                                style = MaterialTheme.typography.labelLarge
                            )
                            step.warnings.forEach { warning ->
                                Text(
                                    text = "• $warning",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ErrorRed
                                )
                            }
                        }
                    }
                }

                // الأدوات
                if (step.tools.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "🛠️ الأدوات المطلوبة:",
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryBlue
                    )
                    step.tools.forEach { tool ->
                        Text("• $tool", style = MaterialTheme.typography.bodySmall)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "⏱️ الوقت المقدر: ${step.estimatedTime}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // أزرار التنقل
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { if (currentStep > 0) onStepChange(currentStep - 1) },
                enabled = currentStep > 0,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Default.ArrowBack, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("السابق")
            }

            if (currentStep < guide.steps.size - 1) {
                Button(onClick = { onStepChange(currentStep + 1) }) {
                    Text("التالي")
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, null)
                }
            } else {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                ) {
                    Icon(Icons.Default.CheckCircle, null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تم!")
                }
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
