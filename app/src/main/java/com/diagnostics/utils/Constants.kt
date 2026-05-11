package com.diagnostics.utils

object Constants {
    const val BACKEND_URL_DEFAULT = "https://iphone-diagnostics-ai-production.up.railway.app"
    const val BACKEND_URL_DEVICE = "https://iphone-diagnostics-ai-production.up.railway.app"

    const val PREFS_NAME = "iphone_diagnostics_prefs"
    const val KEY_OCR_PROVIDER = "ocr_provider"
    const val KEY_OCR_SPACE_KEY = "ocr_space_key"
    const val KEY_GOOGLE_VISION_KEY = "google_vision_key"
    const val KEY_AZURE_KEY = "azure_key"
    const val KEY_AZURE_ENDPOINT = "azure_endpoint"
    const val KEY_OPENAI_KEY = "openai_key"
    const val KEY_GEMINI_KEY = "gemini_key"
    const val KEY_BACKEND_URL = "backend_url"

    const val ML_KIT_FREE = "Google ML Kit (مجاني - يعمل على الجهاز)"
    const val OCR_SPACE_FREE = "OCR.space (25,000 طلب/شهر مجاناً)"

    const val MAX_IMAGE_SIZE = 1024 * 1024 * 5 // 5MB
}
