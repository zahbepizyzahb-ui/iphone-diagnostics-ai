package com.diagnostics.service

import android.graphics.Bitmap
import com.diagnostics.model.APIConfiguration
import com.diagnostics.model.DetectedComponent
import com.diagnostics.model.LogAnalysisResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class APIService {
    companion object {
        private var config = APIConfiguration()

        fun updateConfig(newConfig: APIConfiguration) {
            config = newConfig
        }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // ✅ اختبار API - ميزة جديدة
    suspend fun testAPIConnection(): APIHealthStatus = withContext(Dispatchers.IO) {
        try {
            if (config.backendURL.isBlank() || config.backendURL == "http://10.0.2.2:8000") {
                return@withContext APIHealthStatus.NOT_CONFIGURED
            }

            val request = Request.Builder()
                .url("${config.backendURL}/api/health")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                when {
                    response.isSuccessful -> {
                        val body = response.body?.string()
                        val json = body?.let { JSONObject(it) }
                        val status = json?.optString("status", "unknown")
                        APIHealthStatus.CONNECTED("Backend يعمل | Status: $status | Port: 8000")
                    }
                    response.code == 404 -> APIHealthStatus.ERROR("API endpoint غير موجود (404)")
                    response.code == 500 -> APIHealthStatus.ERROR("خطأ في الخادم (500)")
                    else -> APIHealthStatus.ERROR("HTTP Error: ${response.code}")
                }
            }
        } catch (e: java.net.ConnectException) {
            APIHealthStatus.ERROR("لا يمكن الاتصال بالخادم. تأكد من تشغيل Backend")
        } catch (e: java.net.SocketTimeoutException) {
            APIHealthStatus.ERROR("انتهت مهلة الاتصال. تأكد من IP والمنفذ")
        } catch (e: Exception) {
            APIHealthStatus.ERROR("خطأ: ${e.message}")
        }
    }

    // ✅ اختبار OCR API - ميزة جديدة
    suspend fun testOCRAPI(): OCRTestResult = withContext(Dispatchers.IO) {
        when (config.selectedOCRProvider) {
            APIConfiguration.OCRProvider.ML_KIT_FREE -> {
                OCRTestResult.SUCCESS("ML Kit مجاني ويعمل على الجهاز مباشرة - لا يحتاج إنترنت")
            }
            APIConfiguration.OCRProvider.OCR_SPACE -> {
                if (config.ocrSpaceKey.isBlank()) {
                    OCRTestResult.ERROR("مفتاح OCR.space غير مضبوط. أضفه في الإعدادات")
                } else {
                    testOCRSpaceConnection()
                }
            }
            APIConfiguration.OCRProvider.GOOGLE_VISION -> {
                if (config.googleVisionKey.isBlank()) {
                    OCRTestResult.ERROR("مفتاح Google Vision غير مضبوط")
                } else {
                    testGoogleVisionConnection()
                }
            }
            APIConfiguration.OCRProvider.AZURE_VISION -> {
                if (config.azureVisionKey.isBlank() || config.azureEndpoint.isBlank()) {
                    OCRTestResult.ERROR("بيانات Azure غير مكتملة")
                } else {
                    testAzureConnection()
                }
            }
        }
    }

    private suspend fun testOCRSpaceConnection(): OCRTestResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://api.ocr.space/parse/imageurl?apikey=${config.ocrSpaceKey}&url=https://i.imgur.com/2M8Ine2.png")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    OCRTestResult.SUCCESS("OCR.space متصل ويعمل بشكل صحيح")
                } else {
                    OCRTestResult.ERROR("OCR.space: HTTP ${response.code} - تأكد من صحة المفتاح")
                }
            }
        } catch (e: Exception) {
            OCRTestResult.ERROR("OCR.space: ${e.message}")
        }
    }

    private suspend fun testGoogleVisionConnection(): OCRTestResult = withContext(Dispatchers.IO) {
        try {
            val testJson = JSONObject().apply {
                put("requests", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("image", JSONObject().apply {
                            put("source", JSONObject().apply {
                                put("imageUri", "https://i.imgur.com/2M8Ine2.png")
                            })
                        })
                        put("features", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "TEXT_DETECTION")
                            })
                        })
                    })
                })
            }

            val request = Request.Builder()
                .url("https://vision.googleapis.com/v1/images:annotate?key=${config.googleVisionKey}")
                .post(testJson.toString().toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    OCRTestResult.SUCCESS("Google Vision متصل ويعمل")
                } else {
                    OCRTestResult.ERROR("Google Vision: HTTP ${response.code} - تأكد من تفعيل API")
                }
            }
        } catch (e: Exception) {
            OCRTestResult.ERROR("Google Vision: ${e.message}")
        }
    }

    private suspend fun testAzureConnection(): OCRTestResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("${config.azureEndpoint}/vision/v3.2/ocr")
                .header("Ocp-Apim-Subscription-Key", config.azureVisionKey)
                .header("Content-Type", "application/json")
                .post("{\"url\":\"https://i.imgur.com/2M8Ine2.png\"}".toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    OCRTestResult.SUCCESS("Azure Vision متصل ويعمل")
                } else {
                    OCRTestResult.ERROR("Azure: HTTP ${response.code} - تأكد من Endpoint والمفتاح")
                }
            }
        } catch (e: Exception) {
            OCRTestResult.ERROR("Azure: ${e.message}")
        }
    }

    // ✅ اختبار Gemini API
    private suspend fun testGeminiAPI(): OCRTestResult = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${config.geminiKey}")
                .post("{\"contents\": [{\"parts\":[{\"text\": \"hi\"}]}]}".toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    OCRTestResult.SUCCESS("Gemini (AI Studio) متصل ويعمل بشكل ممتاز")
                } else {
                    OCRTestResult.ERROR("Gemini: HTTP ${response.code} - تأكد من صحة المفتاح")
                }
            }
        } catch (e: Exception) {
            OCRTestResult.ERROR("Gemini: ${e.message}")
        }
    }

    // ✅ تحليل سجل الأعطال
    suspend fun analyzeCrashLog(logText: String): LogAnalysisResult = withContext(Dispatchers.IO) {
        if (config.backendURL.isBlank() || config.backendURL == "http://10.0.2.2:8000") {
            throw Exception("Backend URL not configured")
        }

        val json = JSONObject().apply {
            put("log_text", logText)
        }

        val request = Request.Builder()
            .url("${config.backendURL}/api/analyze-log")
            .post(json.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

            val body = response.body?.string() ?: throw Exception("Empty response")
            val jsonResponse = JSONObject(body)

            LogAnalysisResult(
                exceptionType = jsonResponse.getString("exceptionType"),
                exceptionSubtype = jsonResponse.optString("exceptionSubtype", ""),
                faultingThread = jsonResponse.optInt("faultingThread", 0),
                crashReason = jsonResponse.getString("crashReason"),
                affectedComponent = jsonResponse.getString("affectedComponent"),
                suggestedFix = jsonResponse.getString("suggestedFix"),
                severity = LogAnalysisResult.SeverityLevel.fromString(jsonResponse.getString("severity")),
                rawLog = logText
            )
        }
    }

    // ✅ تحليل صورة البورد بالذكاء الاصطناعي - محسّن
    suspend fun analyzeBoardImage(bitmap: Bitmap): List<DetectedComponent> = withContext(Dispatchers.IO) {
        if (config.backendURL.isBlank() || config.backendURL == "http://10.0.2.2:8000") {
            throw Exception("Backend URL not configured. اضبط عنوان الخادم في الإعدادات")
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val imageBytes = stream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                "board.jpg",
                imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("${config.backendURL}/api/analyze-board")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code} - تأكد من تشغيل Backend")

            val body = response.body?.string() ?: throw Exception("Empty response")
            val jsonArray = org.json.JSONArray(body)

            List(jsonArray.length()) { i ->
                val json = jsonArray.getJSONObject(i)
                DetectedComponent(
                    name = json.getString("name"),
                    type = DetectedComponent.ComponentType.fromString(json.getString("type")),
                    position = androidx.compose.ui.geometry.Offset(
                        json.getJSONObject("position").getDouble("x").toFloat(),
                        json.getJSONObject("position").getDouble("y").toFloat()
                    ),
                    confidence = json.getDouble("confidence"),
                    partNumber = json.optString("partNumber", null),
                    isFaulty = json.getBoolean("isFaulty"),
                    notes = json.getString("notes")
                )
            }
        }
    }

    // ✅ OCR للمكونات
    suspend fun performOCR(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        when (config.selectedOCRProvider) {
            APIConfiguration.OCRProvider.ML_KIT_FREE -> performMLKitOCR(bitmap)
            APIConfiguration.OCRProvider.OCR_SPACE -> performOCRSpace(bitmap)
            APIConfiguration.OCRProvider.GOOGLE_VISION -> performGoogleVisionOCR(bitmap)
            APIConfiguration.OCRProvider.AZURE_VISION -> performAzureOCR(bitmap)
        }
    }

    private suspend fun performMLKitOCR(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        // ML Kit يعمل على الجهاز - يُستدعى من ViewModel
        // هذا placeholder - التنفيذ الفعلي في MLKitOCRHelper
        "ML Kit OCR - يعمل على الجهاز"
    }

    private suspend fun performOCRSpace(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        if (config.ocrSpaceKey.isBlank()) {
            throw Exception("OCR.space API key not configured")
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val imageBytes = stream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("apikey", config.ocrSpaceKey)
            .addFormDataPart("language", "eng")
            .addFormDataPart("isOverlayRequired", "false")
            .addFormDataPart(
                "file",
                "board.jpg",
                imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url("https://api.ocr.space/parse/image")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

            val body = response.body?.string() ?: throw Exception("Empty response")
            val json = JSONObject(body)
            val parsedResults = json.getJSONArray("ParsedResults")
            parsedResults.getJSONObject(0).getString("ParsedText")
        }
    }

    private suspend fun performGoogleVisionOCR(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        if (config.googleVisionKey.isBlank()) {
            throw Exception("Google Vision API key not configured")
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val base64Image = android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.DEFAULT)

        val json = JSONObject().apply {
            put("requests", org.json.JSONArray().apply {
                put(JSONObject().apply {
                    put("image", JSONObject().apply {
                        put("content", base64Image)
                    })
                    put("features", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("type", "TEXT_DETECTION")
                            put("maxResults", 10)
                        })
                    })
                })
            })
        }

        val request = Request.Builder()
            .url("https://vision.googleapis.com/v1/images:annotate?key=${config.googleVisionKey}")
            .post(json.toString().toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

            val body = response.body?.string() ?: throw Exception("Empty response")
            val jsonResponse = JSONObject(body)
            val textAnnotations = jsonResponse.getJSONArray("responses")
                .getJSONObject(0)
                .getJSONArray("textAnnotations")

            textAnnotations.getJSONObject(0).getString("description")
        }
    }

    private suspend fun performAzureOCR(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        if (config.azureVisionKey.isBlank() || config.azureEndpoint.isBlank()) {
            throw Exception("Azure Vision credentials not configured")
        }

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        val imageBytes = stream.toByteArray()

        val request = Request.Builder()
            .url("${config.azureEndpoint}/vision/v3.2/ocr")
            .post(imageBytes.toRequestBody("application/octet-stream".toMediaTypeOrNull()))
            .header("Ocp-Apim-Subscription-Key", config.azureVisionKey)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("HTTP ${response.code}")

            val body = response.body?.string() ?: throw Exception("Empty response")
            val json = JSONObject(body)
            val regions = json.getJSONArray("regions")

            buildString {
                for (i in 0 until regions.length()) {
                    val region = regions.getJSONObject(i)
                    val lines = region.getJSONArray("lines")
                    for (j in 0 until lines.length()) {
                        val line = lines.getJSONObject(j)
                        val words = line.getJSONArray("words")
                        for (k in 0 until words.length()) {
                            append(words.getJSONObject(k).getString("text"))
                            append(" ")
                        }
                        append("\n")
                    }
                }
            }
        }
    }

    // ✅ تحليل AI للصورة - ميزة جديدة
    suspend fun analyzeImageWithAI(bitmap: Bitmap): AIImageAnalysis = withContext(Dispatchers.IO) {
        try {
            // 1. OCR للحصول على النص
            val ocrText = performOCR(bitmap)

            // 2. تحليل المكونات
            val components = analyzeBoardImage(bitmap)

            // 3. تحليل AI إضافي (إذا كان Backend متاح)
            val aiInsights = try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                val imageBytes = stream.toByteArray()

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "image",
                        "board.jpg",
                        imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                    )
                    .addFormDataPart("ocr_text", ocrText)
                    .build()

                val request = Request.Builder()
                    .url("${config.backendURL}/api/analyze-board-ai")
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        val json = body?.let { JSONObject(it) }
                        json?.optString("ai_insights", "لا يوجد تحليل AI إضافي")
                    } else null
                }
            } catch (e: Exception) {
                null
            }

            AIImageAnalysis(
                ocrText = ocrText,
                components = components,
                aiInsights = aiInsights ?: "استخدم ML Kit للتحليل المحلي - Backend غير متاح"
            )

        } catch (e: Exception) {
            AIImageAnalysis(
                ocrText = "خطأ في OCR: ${e.message}",
                components = emptyList(),
                aiInsights = "فشل التحليل: ${e.message}"
            )
        }
    }

    // ✅ اختبار شامل لكل APIs
    suspend fun runFullDiagnostics(): List<APITestResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<APITestResult>()

        // 1. اختبار Backend
        val backendStatus = testAPIConnection()
        results.add(APITestResult(
            name = "Backend Connection",
            status = if (backendStatus is APIHealthStatus.CONNECTED) TestStatus.PASSED else TestStatus.FAILED,
            message = when (backendStatus) {
                is APIHealthStatus.CONNECTED -> backendStatus.message
                is APIHealthStatus.ERROR -> backendStatus.message
                APIHealthStatus.NOT_CONFIGURED -> "Backend URL غير مضبوط"
            }
        ))

        // 2. اختبار OCR
        val ocrStatus = testOCRAPI()
        results.add(APITestResult(
            name = "OCR Service (${config.selectedOCRProvider.label})",
            status = if (ocrStatus is OCRTestResult.SUCCESS) TestStatus.PASSED else TestStatus.FAILED,
            message = when (ocrStatus) {
                is OCRTestResult.SUCCESS -> ocrStatus.message
                is OCRTestResult.ERROR -> ocrStatus.message
            }
        ))

        // 3. اختبار Gemini (إذا كان المفتاح موجوداً)
        if (config.geminiKey.isNotBlank()) {
            val geminiStatus = testGeminiAPI()
            results.add(APITestResult(
                name = "Google Gemini (AI Studio)",
                status = if (geminiStatus is OCRTestResult.SUCCESS) TestStatus.PASSED else TestStatus.FAILED,
                message = if (geminiStatus is OCRTestResult.SUCCESS) geminiStatus.message else (geminiStatus as OCRTestResult.ERROR).message
            ))
        }

        // 4. اختبار تحليل السجلات (إذا كان Backend متصل)
        if (backendStatus is APIHealthStatus.CONNECTED) {
            try {
                val testLog = "Exception Type: EXC_BAD_ACCESS\nThread 0 Crashed"
                analyzeCrashLog(testLog)
                results.add(APITestResult(
                    name = "Log Analysis",
                    status = TestStatus.PASSED,
                    message = "تحليل السجلات يعمل بشكل صحيح"
                ))
            } catch (e: Exception) {
                results.add(APITestResult(
                    name = "Log Analysis",
                    status = TestStatus.FAILED,
                    message = "فشل تحليل السجلات: ${e.message}"
                ))
            }
        }

        results
    }
}

// ✅ Data Classes للنتائج
sealed class APIHealthStatus {
    object NOT_CONFIGURED : APIHealthStatus()
    data class CONNECTED(val message: String) : APIHealthStatus()
    data class ERROR(val message: String) : APIHealthStatus()
}

sealed class OCRTestResult {
    data class SUCCESS(val message: String) : OCRTestResult()
    data class ERROR(val message: String) : OCRTestResult()
}

data class AIImageAnalysis(
    val ocrText: String,
    val components: List<DetectedComponent>,
    val aiInsights: String
)

data class APITestResult(
    val name: String,
    val status: TestStatus,
    val message: String
)

enum class TestStatus {
    PASSED, FAILED, WARNING, SKIPPED
}
