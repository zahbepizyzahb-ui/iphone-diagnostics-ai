package com.diagnostics.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.diagnostics.model.DiagnosisResult
import com.diagnostics.model.LogAnalysisResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PDFReportGenerator(private val context: Context) {

    private val paint = Paint().apply {
        isAntiAlias = true
    }

    fun generateDiagnosisReport(
        diagnosis: DiagnosisResult,
        logs: List<LogAnalysisResult>,
        boardImage: Bitmap? = null
    ): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var yPosition = 50f

        // عنوان التقرير
        paint.textSize = 24f
        paint.color = android.graphics.Color.parseColor("#2196F3")
        canvas.drawText("📋 تقرير تشخيص iPhone", 50f, yPosition, paint)
        yPosition += 40f

        // خط فاصل
        paint.color = android.graphics.Color.LTGRAY
        canvas.drawLine(50f, yPosition, 545f, yPosition, paint)
        yPosition += 30f

        // معلومات الجهاز
        paint.textSize = 16f
        paint.color = android.graphics.Color.BLACK
        canvas.drawText("معلومات الجهاز:", 50f, yPosition, paint)
        yPosition += 25f

        paint.textSize = 12f
        canvas.drawText("• الموديل: ${diagnosis.deviceModel}", 70f, yPosition, paint)
        yPosition += 20f
        canvas.drawText("• iOS: ${diagnosis.iosVersion}", 70f, yPosition, paint)
        yPosition += 20f
        canvas.drawText("• التاريخ: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(diagnosis.date)}", 70f, yPosition, paint)
        yPosition += 20f
        canvas.drawText("• الثقة: ${(diagnosis.confidence * 100).toInt()}%", 70f, yPosition, paint)
        yPosition += 40f

        // المشكلة
        paint.textSize = 16f
        paint.color = android.graphics.Color.parseColor("#F44336")
        canvas.drawText("المشكلة المكتشفة:", 50f, yPosition, paint)
        yPosition += 25f

        paint.textSize = 14f
        paint.color = android.graphics.Color.BLACK
        canvas.drawText(diagnosis.problem, 70f, yPosition, paint)
        yPosition += 40f

        // المكونات
        if (diagnosis.components.isNotEmpty()) {
            paint.textSize = 16f
            paint.color = android.graphics.Color.parseColor("#FF9800")
            canvas.drawText("المكونات المكتشفة:", 50f, yPosition, paint)
            yPosition += 25f

            diagnosis.components.forEach { component ->
                paint.textSize = 12f
                paint.color = if (component.isFaulty) android.graphics.Color.RED else android.graphics.Color.BLACK
                canvas.drawText("• ${component.name} (${component.type.label}) ${if (component.isFaulty) "⚠️ تالف" else "✓ سليم"}", 70f, yPosition, paint)
                yPosition += 18f

                component.partNumber?.let {
                    paint.color = android.graphics.Color.GRAY
                    canvas.drawText("  رقم القطعة: $it", 90f, yPosition, paint)
                    yPosition += 16f
                }

                if (component.notes.isNotBlank()) {
                    paint.color = android.graphics.Color.DKGRAY
                    canvas.drawText("  ملاحظة: ${component.notes}", 90f, yPosition, paint)
                    yPosition += 16f
                }
            }
            yPosition += 20f
        }

        // التوصيات
        if (diagnosis.recommendations.isNotEmpty()) {
            paint.textSize = 16f
            paint.color = android.graphics.Color.parseColor("#4CAF50")
            canvas.drawText("التوصيات:", 50f, yPosition, paint)
            yPosition += 25f

            diagnosis.recommendations.forEach { rec ->
                paint.textSize = 12f
                paint.color = android.graphics.Color.BLACK
                canvas.drawText("→ $rec", 70f, yPosition, paint)
                yPosition += 20f
            }
        }

        pdfDocument.finishPage(page)

        // صفحة ثانية للسجلات
        if (logs.isNotEmpty()) {
            val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
            val page2 = pdfDocument.startPage(pageInfo2)
            val canvas2 = page2.canvas

            var y2 = 50f

            paint.textSize = 20f
            paint.color = android.graphics.Color.parseColor("#2196F3")
            canvas2.drawText("📊 سجلات الأعطال المحللة", 50f, y2, paint)
            y2 += 40f

            logs.forEach { log ->
                if (y2 > 750f) {
                    pdfDocument.finishPage(page2)
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                    val newPage = pdfDocument.startPage(newPageInfo)
                    val newCanvas = newPage.canvas
                    y2 = 50f

                    paint.textSize = 12f
                    paint.color = android.graphics.Color.BLACK
                    newCanvas.drawText("• ${log.exceptionType} (${log.severity.label})", 50f, y2, paint)
                    y2 += 20f
                    newCanvas.drawText("  السبب: ${log.crashReason}", 70f, y2, paint)
                    y2 += 20f
                    newCanvas.drawText("  المكون: ${log.affectedComponent}", 70f, y2, paint)
                    y2 += 30f

                    pdfDocument.finishPage(newPage)
                } else {
                    paint.textSize = 12f
                    paint.color = android.graphics.Color.BLACK
                    canvas2.drawText("• ${log.exceptionType} (${log.severity.label})", 50f, y2, paint)
                    y2 += 20f
                    canvas2.drawText("  السبب: ${log.crashReason}", 70f, y2, paint)
                    y2 += 20f
                    canvas2.drawText("  المكون: ${log.affectedComponent}", 70f, y2, paint)
                    y2 += 30f
                }
            }

            if (y2 <= 750f) {
                pdfDocument.finishPage(page2)
            }
        }

        // حفظ الملف
        val fileName = "iPhone_Diagnosis_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        FileOutputStream(file).use { output ->
            pdfDocument.writeTo(output)
        }
        pdfDocument.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun generateComponentReport(componentName: String, partNumber: String): Uri? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        var y = 50f

        paint.textSize = 24f
        paint.color = android.graphics.Color.parseColor("#2196F3")
        canvas.drawText("📋 تقرير القطعة", 50f, y, paint)
        y += 40f

        paint.textSize = 18f
        paint.color = android.graphics.Color.BLACK
        canvas.drawText(componentName, 50f, y, paint)
        y += 30f

        paint.textSize = 14f
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("رقم القطعة: $partNumber", 50f, y, paint)
        y += 40f

        paint.textSize = 12f
        paint.color = android.graphics.Color.BLACK
        canvas.drawText("تم إنشاء هذا التقرير بواسطة iPhone Diagnostics AI", 50f, y, paint)

        pdfDocument.finishPage(page)

        val fileName = "Component_${partNumber}_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.pdf"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

        FileOutputStream(file).use { output ->
            pdfDocument.writeTo(output)
        }
        pdfDocument.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }
}
