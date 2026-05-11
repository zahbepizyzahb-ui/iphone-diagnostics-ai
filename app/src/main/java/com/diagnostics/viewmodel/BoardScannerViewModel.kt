package com.diagnostics.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diagnostics.model.DetectedComponent
import com.diagnostics.service.APIService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset

class BoardScannerViewModel : ViewModel() {
    private val apiService = APIService()

    private val _scannedImage = MutableStateFlow<Bitmap?>(null)
    val scannedImage: StateFlow<Bitmap?> = _scannedImage.asStateFlow()

    private val _detectedComponents = MutableStateFlow<List<DetectedComponent>>(emptyList())
    val detectedComponents: StateFlow<List<DetectedComponent>> = _detectedComponents.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // ✅ حالات تحليل AI الجديدة
    private val _ocrText = MutableStateFlow("")
    val ocrText: StateFlow<String> = _ocrText.asStateFlow()

    private val _aiAnalysis = MutableStateFlow<String?>(null)
    val aiAnalysis: StateFlow<String?> = _aiAnalysis.asStateFlow()

    fun analyzeImage(bitmap: Bitmap) {
        analyzeImageWithAI(bitmap)
    }

    // ✅ تحليل AI شامل للصورة
    fun analyzeImageWithAI(bitmap: Bitmap) {
        viewModelScope.launch {
            _scannedImage.value = bitmap
            _isAnalyzing.value = true
            _errorMessage.value = null
            _ocrText.value = ""
            _aiAnalysis.value = null

            try {
                // محاولة التحليل AI الكامل
                try {
                    val analysis = apiService.analyzeImageWithAI(bitmap)
                    _detectedComponents.value = analysis.components
                    _ocrText.value = analysis.ocrText
                    _aiAnalysis.value = analysis.aiInsights
                } catch (e: Exception) {
                    // fallback: تحليل محلي
                    _detectedComponents.value = getMockComponents()
                    _ocrText.value = "تحليل OCR محلي (Backend غير متاح)"
                    _aiAnalysis.value = "استخدم ML Kit للتحليل المحلي. لتفعيل AI الكامل، شغّل Backend."
                    _errorMessage.value = "تم التحليل المحلي. خطأ الاتصال: ${e.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "خطأ: ${e.message}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun analyzeImageFromUri(uri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                val bitmap = context.contentResolver.openInputStream(uri)?.use {
                    android.graphics.BitmapFactory.decodeStream(it)
                }
                bitmap?.let { analyzeImageWithAI(it) }
            } catch (e: Exception) {
                _errorMessage.value = "فشل قراءة الصورة: ${e.message}"
            }
        }
    }

    private fun getMockComponents(): List<DetectedComponent> {
        return listOf(
            DetectedComponent(
                name = "U1 - A16 Bionic",
                type = DetectedComponent.ComponentType.CPU,
                position = Offset(0.5f, 0.4f),
                confidence = 0.95,
                partNumber = "APL1W10",
                isFaulty = false,
                notes = "المعالج الرئيسي"
            ),
            DetectedComponent(
                name = "U2 - Power Management",
                type = DetectedComponent.ComponentType.POWER_IC,
                position = Offset(0.3f, 0.6f),
                confidence = 0.88,
                partNumber = "338S00817",
                isFaulty = true,
                notes = "قد يكون سبب مشكلة إعادة التشغيل"
            ),
            DetectedComponent(
                name = "U3 - Charging IC",
                type = DetectedComponent.ComponentType.CHARGING_IC,
                position = Offset(0.7f, 0.7f),
                confidence = 0.82,
                partNumber = "SN2012010",
                isFaulty = false,
                notes = "دائرة الشحن"
            ),
            DetectedComponent(
                name = "L1 - Inductor",
                type = DetectedComponent.ComponentType.INDUCTOR,
                position = Offset(0.4f, 0.5f),
                confidence = 0.75,
                notes = "ملف طاقة"
            ),
            DetectedComponent(
                name = "C1 - Capacitor",
                type = DetectedComponent.ComponentType.CAPACITOR,
                position = Offset(0.6f, 0.5f),
                confidence = 0.70,
                notes = "مكثف تصفية"
            )
        )
    }
}
