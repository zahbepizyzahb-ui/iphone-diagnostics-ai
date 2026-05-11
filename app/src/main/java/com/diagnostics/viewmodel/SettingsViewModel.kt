package com.diagnostics.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.diagnostics.model.APIConfiguration
import com.diagnostics.service.APIService
import com.diagnostics.service.APITestResult
import com.diagnostics.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val apiService = APIService()
    private val prefs = application.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

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
        val savedConfig = APIConfiguration(
            ocrSpaceKey = prefs.getString(Constants.KEY_OCR_SPACE_KEY, "") ?: "",
            googleVisionKey = prefs.getString(Constants.KEY_GOOGLE_VISION_KEY, "") ?: "",
            azureVisionKey = prefs.getString(Constants.KEY_AZURE_KEY, "") ?: "",
            azureEndpoint = prefs.getString(Constants.KEY_AZURE_ENDPOINT, "") ?: "",
            openAIKey = prefs.getString(Constants.KEY_OPENAI_KEY, "") ?: "",
            geminiKey = prefs.getString(Constants.KEY_GEMINI_KEY, "") ?: "",
            backendURL = prefs.getString(Constants.KEY_BACKEND_URL, "https://iphone-diagnostics-ai-production.up.railway.app") ?: "https://iphone-diagnostics-ai-production.up.railway.app",
            selectedOCRProvider = APIConfiguration.OCRProvider.fromString(
                prefs.getString(Constants.KEY_OCR_PROVIDER, APIConfiguration.OCRProvider.ML_KIT_FREE.name) ?: APIConfiguration.OCRProvider.ML_KIT_FREE.name
            )
        )
        _config.value = savedConfig
        APIService.updateConfig(savedConfig)
    }

    // ✅ تشغيل اختبار API
    fun runAPITests() {
        viewModelScope.launch {
            _isTestingAPI.value = true
            _apiTestResults.value = emptyList()

            try {
                // نحدث الإعدادات في الخدمة قبل الاختبار
                APIService.updateConfig(_config.value)
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
            val c = _config.value
            prefs.edit().apply {
                putString(Constants.KEY_OCR_SPACE_KEY, c.ocrSpaceKey)
                putString(Constants.KEY_GOOGLE_VISION_KEY, c.googleVisionKey)
                putString(Constants.KEY_AZURE_KEY, c.azureVisionKey)
                putString(Constants.KEY_AZURE_ENDPOINT, c.azureEndpoint)
                putString(Constants.KEY_OPENAI_KEY, c.openAIKey)
                putString(Constants.KEY_GEMINI_KEY, c.geminiKey)
                putString(Constants.KEY_BACKEND_URL, c.backendURL)
                putString(Constants.KEY_OCR_PROVIDER, c.selectedOCRProvider.name)
                apply()
            }
            APIService.updateConfig(c)
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
