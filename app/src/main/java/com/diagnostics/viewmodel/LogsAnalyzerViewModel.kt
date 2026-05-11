package com.diagnostics.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diagnostics.model.LogAnalysisResult
import com.diagnostics.service.APIService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class LogsAnalyzerViewModel : ViewModel() {
    private val apiService = APIService()

    private val _results = MutableStateFlow<List<LogAnalysisResult>>(emptyList())
    val results: StateFlow<List<LogAnalysisResult>> = _results.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun analyzeText(text: String) {
        if (text.isBlank()) {
            _errorMessage.value = "النص فارغ"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                // تحليل محلي أولاً
                val localResult = parseLogLocally(text)

                // محاولة تحليل AI عبر Backend
                try {
                    val aiResult = apiService.analyzeCrashLog(text)
                    _results.value = listOf(aiResult) + _results.value
                } catch (e: Exception) {
                    _results.value = listOf(localResult) + _results.value
                    _errorMessage.value = "تم التحليل محلياً. خطأ الاتصال: ${e.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "خطأ: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun analyzeFile(uri: Uri, content: String) {
        analyzeText(content)
    }

    fun deleteResult(result: LogAnalysisResult) {
        _results.value = _results.value.filter { it.id != result.id }
    }

    fun clearAllResults() {
        _results.value = emptyList()
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun showError(message: String) {
        _errorMessage.value = message
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _errorMessage.value = null
        }
    }

    private fun parseLogLocally(text: String): LogAnalysisResult {
        val lines = text.lines()

        var exceptionType = "Unknown"
        var exceptionSubtype = ""
        var faultingThread = 0
        var crashReason = "غير محدد"
        var affectedComponent = "غير معروف"
        var suggestedFix = "يرجى مراجعة التحليل AI"
        var severity = LogAnalysisResult.SeverityLevel.MEDIUM

        lines.forEach { line ->
            when {
                line.contains("Exception Type:") -> {
                    exceptionType = line.substringAfter("Exception Type:").trim()
                    when {
                        line.contains("EXC_BAD_ACCESS") || line.contains("EXC_CRASH") -> {
                            severity = LogAnalysisResult.SeverityLevel.CRITICAL
                            crashReason = "وصول لذاكرة غير صالحة أو تعطل النظام"
                            affectedComponent = "ذاكرة النظام / المعالج"
                            suggestedFix = """1. فحص ذاكرة الجهاز
2. إعادة تعيين إعدادات الشبكة
3. تحديث iOS
4. إذا استمرت المشكلة، قد يكون هناك خلل في اللوحة الأم"""
                        }
                        line.contains("EXC_RESOURCE") -> {
                            severity = LogAnalysisResult.SeverityLevel.HIGH
                            crashReason = "استهلاك مفرط للموارد"
                            affectedComponent = "البطارية / المعالج"
                            suggestedFix = """1. إغلاق التطبيقات التي تعمل في الخلفية
2. تقليل سطوع الشاشة
3. تعطيل خدمات الموقع غير الضرورية
4. فحص البطارية"""
                        }
                        line.contains("EXC_BREAKPOINT") -> {
                            severity = LogAnalysisResult.SeverityLevel.MEDIUM
                            crashReason = "نقطة توقف في الكود"
                            affectedComponent = "البرمجيات / التطبيق"
                            suggestedFix = """1. تحديث التطبيق المتسبب
2. إعادة تثبيت التطبيق
3. مسح ذاكرة التخزين المؤقت"""
                        }
                        line.contains("EXC_GUARD") -> {
                            severity = LogAnalysisResult.SeverityLevel.HIGH
                            crashReason = "انتهاك حماية الملفات"
                            affectedComponent = "نظام الملفات"
                            suggestedFix = """1. فحص صلاحيات التطبيقات
2. إعادة تشغيل الجهاز
3. تحديث iOS"""
                        }
                    }
                }
                line.contains("Exception Subtype:") -> {
                    exceptionSubtype = line.substringAfter("Exception Subtype:").trim()
                }
                line.contains("Thread") && line.contains("Crashed") -> {
                    faultingThread = line.filter { it.isDigit() }.toIntOrNull() ?: 0
                }
                line.contains("PowerUI") || line.contains("battery") || line.contains("Battery") -> {
                    affectedComponent = "دائرة الطاقة / البطارية"
                }
                line.contains("WiFi") || line.contains("80211") || line.contains("wifi") -> {
                    affectedComponent = "وحدة الواي فاي"
                }
                line.contains("Audio") || line.contains("CoreAudio") || line.contains("audio") -> {
                    affectedComponent = "دائرة الصوت"
                }
                line.contains("Display") || line.contains("Backlight") || line.contains("display") -> {
                    affectedComponent = "دائرة الشاشة"
                }
                line.contains("Touch") || line.contains("Multitouch") || line.contains("touch") -> {
                    affectedComponent = "دائرة اللمس"
                }
                line.contains("Camera") || line.contains("ISP") || line.contains("camera") -> {
                    affectedComponent = "كاميرا / معالج الصور"
                }
                line.contains("NAND") || line.contains("storage") || line.contains("Storage") -> {
                    affectedComponent = "ذاكرة التخزين"
                }
                line.contains("DRAM") || line.contains("RAM") || line.contains("memory") -> {
                    affectedComponent = "ذاكرة الوصول العشوائي"
                }
            }
        }

        return LogAnalysisResult(
            timestamp = Date(),
            exceptionType = exceptionType,
            exceptionSubtype = exceptionSubtype,
            faultingThread = faultingThread,
            crashReason = crashReason,
            affectedComponent = affectedComponent,
            suggestedFix = suggestedFix,
            severity = severity,
            rawLog = text
        )
    }
}
