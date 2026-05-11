package com.diagnostics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diagnostics.model.DetectedComponent
import com.diagnostics.model.DiagnosisResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset

class DiagnosisViewModel : ViewModel() {
    private val _diagnoses = MutableStateFlow<List<DiagnosisResult>>(emptyList())
    val diagnoses: StateFlow<List<DiagnosisResult>> = _diagnoses.asStateFlow()

    private val _isDiagnosing = MutableStateFlow(false)
    val isDiagnosing: StateFlow<Boolean> = _isDiagnosing.asStateFlow()

    private val _overallHealth = MutableStateFlow(0.85)
    val overallHealth: StateFlow<Double> = _overallHealth.asStateFlow()

    private val _batteryHealth = MutableStateFlow(0.90)
    val batteryHealth: StateFlow<Double> = _batteryHealth.asStateFlow()

    private val _performanceHealth = MutableStateFlow(0.80)
    val performanceHealth: StateFlow<Double> = _performanceHealth.asStateFlow()

    private val _storageHealth = MutableStateFlow(0.75)
    val storageHealth: StateFlow<Double> = _storageHealth.asStateFlow()

    fun startFullDiagnosis() {
        viewModelScope.launch {
            _isDiagnosing.value = true

            kotlinx.coroutines.delay(2000)

            val mockDiagnosis = DiagnosisResult(
                deviceModel = "iPhone 14 Pro",
                iosVersion = "17.4",
                problem = "استهلاك مفرط للبطارية",
                confidence = 0.87,
                components = listOf(
                    DetectedComponent(
                        name = "U2 - Power Management IC",
                        type = DetectedComponent.ComponentType.POWER_IC,
                        position = Offset(0.5f, 0.5f),
                        confidence = 0.92,
                        partNumber = "338S00817",
                        isFaulty = true,
                        notes = "استهلاك غير طبيعي للطاقة"
                    ),
                    DetectedComponent(
                        name = "بطارية Li-ion",
                        type = DetectedComponent.ComponentType.BATTERY,
                        position = Offset(0.3f, 0.7f),
                        confidence = 0.85,
                        partNumber = "616-00675",
                        isFaulty = false,
                        notes = "الصحة 89% - ضمن المعدل الطبيعي"
                    )
                ),
                recommendations = listOf(
                    "استبدال دائرة إدارة الطاقة (Power IC)",
                    "فحص البطارية بجهاز Battery Tester",
                    "تجنب الشحن السريع المستمر",
                    "مراجعة مركز صيانة معتمد"
                )
            )

            _diagnoses.value = listOf(mockDiagnosis) + _diagnoses.value
            _isDiagnosing.value = false
            _overallHealth.value = 0.72
            _batteryHealth.value = 0.65
        }
    }
}
