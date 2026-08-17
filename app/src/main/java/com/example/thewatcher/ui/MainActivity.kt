package com.example.thewatcher.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.thewatcher.service.MonitorService
import com.example.thewatcher.ui.theme.TheWatcherTheme
import com.example.thewatcher.util.RequestPermissions

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheWatcherTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var hasPermissions by remember { mutableStateOf(false) }
                    RequestPermissions(context = this) { granted -> hasPermissions = granted }

                    if (hasPermissions) {
                        AppNavigation(
                            onStartMonitoring = { startService(Intent(this, MonitorService::class.java)) },
                            onStopMonitoring = { stopService(Intent(this, MonitorService::class.java)) }
                        )
                    } else {
                        PermissionGate()
                    }
                }
            }
        }
    }
}
