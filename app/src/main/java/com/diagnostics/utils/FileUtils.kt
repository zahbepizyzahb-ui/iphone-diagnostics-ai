package com.diagnostics.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

object FileUtils {

    suspend fun readTextFromUri(uri: Uri, contentResolver: ContentResolver): String = 
        withContext(Dispatchers.IO) {
            val stringBuilder = StringBuilder()
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line).append("\n")
                    }
                }
            }
            stringBuilder.toString()
        }

    fun getFileName(uri: Uri, contentResolver: ContentResolver): String {
        var name = "unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    name = cursor.getString(index) ?: name
                }
            }
        }
        return name
    }

    fun isValidLogFile(fileName: String): Boolean {
        return fileName.endsWith(".ips") || 
               fileName.endsWith(".ipsync") ||
               fileName.endsWith(".crash") ||
               fileName.endsWith(".txt")
    }
}
