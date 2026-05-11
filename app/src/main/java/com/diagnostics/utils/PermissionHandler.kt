package com.diagnostics.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat

object PermissionHandler {

    val CAMERA_PERMISSION = Manifest.permission.CAMERA
    val READ_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE
    val READ_MEDIA_IMAGES = Manifest.permission.READ_MEDIA_IMAGES

    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, CAMERA_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun hasStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context, READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context, READ_STORAGE_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun RequestCameraPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    val permissionState = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onGranted()
        } else {
            onDenied()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }
}

@Composable
fun RequestStoragePermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onGranted()
        } else {
            onDenied()
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.READ_MEDIA_IMAGES)
    }
}
