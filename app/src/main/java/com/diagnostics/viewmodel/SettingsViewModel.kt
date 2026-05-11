package com.diagnostics.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diagnostics.model.APIConfiguration
import com.diagnostics.service.APIService
import com.diagnostics.service.APITestResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {
    private val apiService = APIService()

    private val _config = MutableStateFlow(APIConfiguration())
    val config: StateFlow<APIConfiguration> = _config.asStateFlow()

    private val _showSaved = MutableStateFlow(false)
    val showSaved: StateFlow<Boolean> = _showSaved.asStateFlow()

    private val _showAPIHelp = MutableStateFlow(false)
    val showAPIHelp: StateFlow<Boolean> = _showAPIHelp.asStateFlow()

    // ✅ حالة اختبار API
    private val _apiTestResults = MutableStateFlow<List<APITestResult>>(emptyList())
    val apiTestResults: StateFlow<List<APITestResult>> = _apiTestResults.asStateFlow()

    private val _isTestingAPI = MutableStateFlow(false)
    val isTestingAPI: StateFlow<Boolean> = _isTestingAPI.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        // TODO: Load from DataStore/SharedPreferences
        _config.value = APIConfiguration()
    }

    // ✅ تشغيل اختبار API
    fun runAPITests() {
        viewModelScope.launch {
            _isTestingAPI.value = true
            _apiTestResults.value = emptyList()

            try {
                val results = apiService.runFullDiagnostics()
                _apiTestResults.value = results
            } catch (e: Exception) {
                _apiTestResults.value = listOf(
                    APITestResult(
                        name = "General Error",
                        status = com.diagnostics.service.TestStatus.FAILED,
                        message = "فشل الاختبار: ${e.message}"
                    )
                )
            } finally {
                _isTestingAPI.value = false
            }
        }
    }

    fun updateProvider(provider: APIConfiguration.OCRProvider) {
        _config.value = _config.value.copy(selectedOCRProvider = provider)
    }

    fun updateOCRSpaceKey(key: String) {
        _config.value = _config.value.copy(ocrSpaceKey = key)
    }

    fun updateGoogleVisionKey(key: String) {
        _config.value = _config.value.copy(googleVisionKey = key)
    }

    fun updateAzureVisionKey(key: String) {
        _config.value = _config.value.copy(azureVisionKey = key)
    }

    fun updateAzureEndpoint(endpoint: String) {
        _config.value = _config.value.copy(azureEndpoint = endpoint)
    }

    fun updateOpenAIKey(key: String) {
        _config.value = _config.value.copy(openAIKey = key)
    }

    fun updateGeminiKey(key: String) {
        _config.value = _config.value.copy(geminiKey = key)
    }

    fun updateBackendURL(url: String) {
        _config.value = _config.value.copy(backendURL = url)
    }

    fun saveSettings() {
        viewModelScope.launch {
            APIService.updateConfig(_config.value)
            // TODO: Save to DataStore
            _showSaved.value = true
        }
    }

    fun dismissSaved() {
        _showSaved.value = false
    }

    fun showAPIHelpDialog() {
        _showAPIHelp.value = true
    }

    fun dismissAPIHelp() {
        _showAPIHelp.value = false
    }

    fun resetSettings() {
        _config.value = APIConfiguration()
        _apiTestResults.value = emptyList()
        saveSettings()
    }
}
