package com.example.thewatcher.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.thewatcher.monitor.MonitorStateHolder
import com.example.thewatcher.util.formatBytes
import com.example.thewatcher.util.formatMac
import com.example.thewatcher.util.formatDuration

@Composable
fun MainScreen(
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit,
    onOpenHistory: () -> Unit
) {
    val state by MonitorStateHolder.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("TheWatcher", style = MaterialTheme.typography.headlineMedium)
        Text("Monitor konsumsi data hotspot", style = MaterialTheme.typography.bodySmall)

        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Total Sesi Ini", style = MaterialTheme.typography.titleMedium)
                Text(
                    "↓ ${formatBytes(state.sessionRxBytes)}   ↑ ${formatBytes(state.sessionTxBytes)}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Button(
            onClick = { if (state.isMonitoring) onStopMonitoring() else onStartMonitoring() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (state.isMonitoring) "Stop Monitoring" else "Mulai Monitoring")
        }

        Button(
            onClick = onOpenHistory,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("History")
        }

        Text(
            "Device Terhubung",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        if (state.clients.isEmpty()) {
            Text("Belum ada device terhubung.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.clients) { c ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("MAC: ${formatMac(c.macAddress)}")
                            Text("IP: ${c.ipAddress.ifEmpty { "-" }}  •  ${formatDuration(c.lastSeenMs - c.firstSeenMs)}")
                            Text("↓ ${formatBytes(c.estRxBytes)}   ↑ ${formatBytes(c.estTxBytes)}  (estimasi)")
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                "Estimasi per-device BUKAN byte sesungguhnya (batasan Android non-root). Total hotspot mendekati pasti.",
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
