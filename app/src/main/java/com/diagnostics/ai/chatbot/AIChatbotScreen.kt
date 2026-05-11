package com.diagnostics.ai.chatbot

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.diagnostics.ui.theme.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class AIChatbotEngine {

    private val knowledgeBase = mapOf(
        "شاشة سوداء" to """⚠️ الشاشة السوداء - الأسباب المحتملة:

1️⃣ خلل في دائرة الباك لايت (Backlight IC)
   - الحل: فحص دائرة U5600 أو U3700
   - الصعوبة: صعبة

2️⃣ مشكلة في كابل الشاشة
   - الحل: إعادة تركيب كابل الشاشة
   - الصعوبة: سهلة

3️⃣ تلف الشاشة نفسها
   - الحل: استبدال الشاشة
   - الصعوبة: متوسطة

4️⃣ خلل في دائرة العرض (Display IC)
   - الحل: فحص دائرة U3701
   - الصعوبة: صعبة

💡 نصيحة: جرب إعادة تشغيل الجهاز أولاً (Hard Reset)""",

        "لا يشحن" to """🔌 مشكلة الشحن - التشخيص:

1️⃣ فحص الكابل والشاحن
   - جرب شاحن أصلي
   - فحص كابل USB-C

2️⃣ فحص منفذ الشحن
   - تنظيف المنفذ من الغبار
   - فحص الأرجل اللحامية

3️⃣ دائرة الشحن (Charging IC)
   - رقم القطعة: SN2012010
   - الحل: إعادة اللحام أو الاستبدال
   - الصعوبة: متوسطة

4️⃣ دائرة إدارة الطاقة (Power IC)
   - رقم القطعة: 338S00817
   - الحل: فحص الجهد على الأرجل
   - الصعوبة: صعبة

5️⃣ البطارية
   - فحص صحة البطارية
   - استبدال إذا كانت متضخمة""",

        "سخونة" to """🌡️ مشكلة السخونة المفرطة:

1️⃣ استهلاك مفرط للمعالج
   - إغلاق التطبيقات في الخلفية
   - إيقاف تحديثات التطبيقات

2️⃣ خلل في دائرة الطاقة
   - فحص Power IC
   - قد يكون هناك تسريب تيار

3️⃣ مشكلة في البطارية
   - فحص تضخم البطارية
   - استبدال إذا لزم الأمر

4️⃣ مشكلة برمجية
   - تحديث iOS
   - استعادة إعدادات المصنع

⚠️ تحذير: إذا وصلت الحرارة لـ 50°C، أوقف استخدام الجهاز فوراً!""",

        "واي فاي" to """📶 مشكلة الواي فاي:

1️⃣ إعادة تعيين إعدادات الشبكة
   الإعدادات > عام > نقل أو إعادة تعيين > إعادة تعيين إعدادات الشبكة

2️⃣ فحص وحدة الواي فاي
   - رقم القطعة: 339S00761
   - الحل: إعادة اللحام
   - الصعوبة: صعبة

3️⃣ مشكلة في الأنتينا
   - فحص كابل الأنتينا
   - إعادة تركيبه

4️⃣ تحديث iOS
   - قد يحتوي على إصلاحات للشبكة""",

        "صوت" to """🔊 مشكلة الصوت:

1️⃣ فحص مكبر الصوت
   - تنظيف الشبكة من الغبار
   - فحص التوصيل

2️⃣ دائرة الصوت (Audio IC)
   - رقم القطعة: 338S00105
   - الحل: إعادة اللحام (Reballing)
   - الصعوبة: صعبة

3️⃣ مشكلة برمجية
   - إعادة تعيين إعدادات الصوت
   - تحديث iOS

4️⃣ تلف مكبر الصوت
   - استبدال المكبر
   - الصعوبة: سهلة""",

        "Face ID" to """👤 مشكلة Face ID:

⚠️ تحذير: Face ID مبرمج على كل جهاز ولا يمكن نقله!

1️⃣ رسالة "أبعد iPhone"
   - مشكلة في مستشعر العمق (Dot Projector)
   - الحل: استبدال مجموعة Face ID بالكامل
   - ⚠️ سيفقد Face ID نهائياً!

2️⃣ رسالة "Face ID غير متاح"
   - مشكلة في الكاميرا الأمامية
   - فحص كابل الكاميرا

3️⃣ بعد تغيير الشاشة
   - إذا لم يتم نقل مجموعة Face ID
   - الحل: نقل المجموعة من الشاشة القديمة

💡 نصيحة: احتفظ دائماً بمجموعة Face ID الأصلية!""",

        "بطارية" to """🔋 مشاكل البطارية:

1️⃣ استهلاك سريع
   - فحص التطبيقات في الخلفية
   - إيقاف خدمات الموقع
   - تقليل سطوع الشاشة

2️⃣ تضخم البطارية
   - ⚠️ خطر! قد تنفجر
   - استبدال فوري
   - لا تشحن الجهاز

3️⃣ إغلاق مفاجئ
   - فحص صحة البطارية
   - استبدال إذا كانت تحت 80%

4️⃣ لا تشحن
   - فحص كابل الشحن
   - فحص دائرة الشحن
   - فحص البطارية نفسها

📊 صحة البطارية المثالية: 85%+
📊 صحة البطارية القابلة للقبول: 80-85%
📊 صحة البطارية تحتاج استبدال: <80%"""
    )

    private val defaultResponses = listOf(
        "أنا مساعدك الذكي لصيانة iPhone! 🛠️\n\nيمكنني مساعدتك في:\n• تشخيص الأعطال\n• معرفة رقم القطعة\n• خطوات الإصلاح\n• نصائح الصيانة\n\nما المشكلة التي تواجهك؟",
        "يمكنك سؤالي عن:\n• مشاكل الشاشة\n• مشاكل الشحن\n• مشاكل الواي فاي\n• مشاكل الصوت\n• مشاكل Face ID\n• مشاكل البطارية\n• السخونة المفرطة",
        "للحصول على أفضل إجابة، صف المشكلة بالتفصيل.\n\nمثال: 'الجهاز يسخن كثيراً ويبطئ' أو 'الشاشة سوداء لكن الجهاز يعمل'"
    )

    fun getResponse(userMessage: String): String {
        val lowerMessage = userMessage.lowercase()

        // البحث في قاعدة المعرفة
        for ((keyword, response) in knowledgeBase) {
            if (lowerMessage.contains(keyword.lowercase())) {
                return response
            }
        }

        // كلمات مفتاحية عامة
        return when {
            lowerMessage.contains("مرحبا") || lowerMessage.contains("هلا") || lowerMessage.contains("سلام") ->
                defaultResponses[0]
            lowerMessage.contains("شكرا") || lowerMessage.contains("شكر") ->
                "عفواً! 😊 إذا احتجت أي مساعدة أخرى، أنا هنا!"
            lowerMessage.contains("سعر") || lowerMessage.contains("كم") || lowerMessage.contains("تكلفة") ->
                """💰 الأسعار التقريبية:

• استبدال شاشة: 50-150$
• استبدال بطارية: 30-60$
• دائرة شحن: 15-30$ (بدون يد العمل)
• دائرة طاقة: 25-50$ (بدون يد العمل)
• استبدال كاميرا: 40-80$
• Face ID: 80-150$ (مع فقدان Face ID)

⚠️ الأسعار تختلف حسب البلد وجودة القطع"""
            lowerMessage.contains("أدوات") || lowerMessage.contains("معدات") ->
                """🛠️ الأدوات الأساسية:

1. مفك Pentalobe (لبراغي iPhone)
2. مفك Tri-wing Y000
3. ملقط ESD
4. مسدس هواء ساخن (لإذابة اللاصق)
5. شفاط شاشة
6. سلك فصل البطارية
7. مجهر (للحام IC)
8. محطة لحام (لإعادة اللحام)
9. فلكس كابل اختبار
10. باور سبلاي (للفحص)

💡 للمبتدئين: ابدأ بأدوات 1-6 فقط"""
            else -> """🤔 لم أفهم سؤالك تماماً.

جرب أن تسأل عن:
• "الشاشة سوداء"
• "لا يشحن"
• "سخونة"
• "مشكلة واي فاي"
• "لا يوجد صوت"
• "Face ID لا يعمل"
• "مشكلة بطارية"

أو اكتب وصف تفصيلي للمشكلة!"""
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatbotScreen() {
    val chatbot = remember { AIChatbotEngine() }
    var messages by remember { mutableStateOf(listOf(
        ChatMessage(
            text = chatbot.getResponse("مرحبا"),
            isUser = false
        )
    )) }
    var userInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = SuccessGreen,
                            modifier = Modifier.size(10.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("🤖 مساعد AI")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = { messages = listOf(ChatMessage(text = chatbot.getResponse("مرحبا"), isUser = false)) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "مسح", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // رسائل المحادثة
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message = message)
                }
            }

            // اقتراحات سريعة
            if (messages.size <= 2) {
                QuickSuggestions { suggestion ->
                    userInput = suggestion
                }
            }

            // حقل الإدخال
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    placeholder = { Text("اكتب سؤالك هنا...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                FloatingActionButton(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            val userMsg = ChatMessage(text = userInput, isUser = true)
                            messages = messages + userMsg

                            scope.launch {
                                kotlinx.coroutines.delay(500)
                                val aiResponse = ChatMessage(
                                    text = chatbot.getResponse(userInput),
                                    isUser = false
                                )
                                messages = messages + aiResponse
                                listState.animateScrollToItem(messages.size - 1)
                            }

                            userInput = ""
                        }
                    },
                    containerColor = PrimaryBlue
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "إرسال", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val backgroundColor = if (message.isUser) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (message.isUser) Color.White else MaterialTheme.colorScheme.onSurface
    val alignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Surface(
            color = backgroundColor,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isUser) 16.dp else 4.dp,
                bottomEnd = if (message.isUser) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun QuickSuggestions(onSuggestionClick: (String) -> Unit) {
    val suggestions = listOf(
        "الشاشة سوداء",
        "لا يشحن",
        "سخونة",
        "مشكلة واي فاي",
        "لا يوجد صوت",
        "Face ID"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        suggestions.forEach { suggestion ->
            Surface(
                color = PrimaryBlue.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.clickable { onSuggestionClick(suggestion) }
            ) {
                Text(
                    text = suggestion,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    color = PrimaryBlue
                )
            }
        }
    }
}
