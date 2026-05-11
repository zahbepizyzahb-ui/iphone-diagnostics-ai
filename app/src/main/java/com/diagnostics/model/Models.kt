package com.diagnostics.model

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import java.util.Date
import java.util.UUID

// نتيجة تحليل السجل
data class LogAnalysisResult(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Date = Date(),
    val exceptionType: String,
    val exceptionSubtype: String = "",
    val faultingThread: Int = 0,
    val crashReason: String,
    val affectedComponent: String,
    val suggestedFix: String,
    val severity: SeverityLevel,
    val rawLog: String
) {
    enum class SeverityLevel(val label: String, val colorHex: String) {
        CRITICAL("حرج", "#FF3B30"),
        HIGH("عالي", "#FF9500"),
        MEDIUM("متوسط", "#FFCC00"),
        LOW("منخفض", "#34C759");

        companion object {
            fun fromString(value: String): SeverityLevel {
                return values().find { it.name == value } ?: MEDIUM
            }
        }
    }
}

// نتيجة التشخيص
data class DiagnosisResult(
    val id: String = UUID.randomUUID().toString(),
    val date: Date = Date(),
    val deviceModel: String,
    val iosVersion: String,
    val problem: String,
    val confidence: Double,
    val components: List<DetectedComponent>,
    val recommendations: List<String>
)

// مكون مكتشف على البورد
data class DetectedComponent(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: ComponentType,
    val position: Offset,
    val confidence: Double,
    val partNumber: String? = null,
    val isFaulty: Boolean = false,
    val notes: String = ""
) {
    enum class ComponentType(val label: String, val icon: String) {
        CPU("معالج", "cpu"),
        RAM("ذاكرة", "memory"),
        STORAGE("تخزين", "storage"),
        POWER_IC("دائرة طاقة", "power"),
        CHARGING_IC("دائرة شحن", "charging"),
        WIFI_MODULE("واي فاي", "wifi"),
        CELLULAR("شبكة خلوية", "cellular"),
        AUDIO_IC("دائرة صوت", "audio"),
        DISPLAY_IC("دائرة شاشة", "display"),
        TOUCH_IC("دائرة لمس", "touch"),
        BATTERY("بطارية", "battery"),
        CAMERA("كاميرا", "camera"),
        SENSOR("حساس", "sensor"),
        CONNECTOR("موصل", "connector"),
        CAPACITOR("مكثف", "capacitor"),
        RESISTOR("مقاومة", "resistor"),
        INDUCTOR("ملف", "inductor"),
        DIODE("دايود", "diode"),
        TRANSISTOR("ترانزستور", "transistor"),
        UNKNOWN("غير معروف", "unknown");

        companion object {
            fun fromString(value: String): ComponentType {
                return values().find { it.name == value } ?: UNKNOWN
            }
        }
    }
}

// بورد ممسوح
data class ScannedBoard(
    val id: String = UUID.randomUUID().toString(),
    val date: Date = Date(),
    val bitmap: Bitmap? = null,
    val imagePath: String? = null,
    val deviceModel: String,
    val components: List<DetectedComponent>,
    val analysisText: String
)

// إعدادات API
data class APIConfiguration(
    val ocrSpaceKey: String = "",
    val googleVisionKey: String = "",
    val azureVisionKey: String = "",
    val azureEndpoint: String = "",
    val openAIKey: String = "",
    val backendURL: String = "https://iphone-diagnostics-ai-production.up.railway.app", // Railway Production
    val selectedOCRProvider: OCRProvider = OCRProvider.ML_KIT_FREE
) {
    enum class OCRProvider(val label: String) {
        ML_KIT_FREE("Google ML Kit (مجاني)"),
        OCR_SPACE("OCR.space"),
        GOOGLE_VISION("Google Vision"),
        AZURE_VISION("Azure Vision");

        companion object {
            fun fromString(value: String): OCRProvider {
                return values().find { it.name == value } ?: ML_KIT_FREE
            }
        }
    }
}

// حالة التطبيق
data class AppState(
    val diagnoses: MutableList<DiagnosisResult> = mutableListOf(),
    val scannedBoards: MutableList<ScannedBoard> = mutableListOf(),
    val apiKeysConfigured: Boolean = false
)
