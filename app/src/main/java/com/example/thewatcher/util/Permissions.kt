package com.example.thewatcher.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat

/**
 * Requests the needed runtime permissions on first composition. If permanently
 * denied, opens the app settings so the user can grant them.
 */
@Composable
fun RequestPermissions(context: Context, onResult: (allGranted: Boolean) -> Unit) {
    val needed = requiredPermissions()
    var launchedSettings by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it }
        if (allGranted) {
            onResult(true)
        } else {
            // Open settings so the user can grant the still-missing ones.
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                }
            )
            launchedSettings = true
        }
    }

    LaunchedEffect(Unit) {
        val missing = needed.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isEmpty()) {
            onResult(true)
        } else {
            launcher.launch(missing.toTypedArray())
        }
    }

    // When returning from settings, re-check.
    LaunchedEffect(launchedSettings) {
        if (launchedSettings) {
            val allGranted = needed.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
            onResult(allGranted)
        }
    }
}
