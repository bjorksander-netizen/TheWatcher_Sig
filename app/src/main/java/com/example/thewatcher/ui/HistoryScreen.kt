package com.example.thewatcher.ui

import android.content.Context
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.thewatcher.data.db.DatabaseProvider
import com.example.thewatcher.data.model.HotspotSession
import com.example.thewatcher.data.model.SessionDeviceUsage
import com.example.thewatcher.monitor.SessionSummaryCalculator
import com.example.thewatcher.util.formatBytes
import com.example.thewatcher.util.formatDateTime
import com.example.thewatcher.util.formatDuration
import com.example.thewatcher.util.formatMac
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var sessions by remember { mutableStateOf<List<HotspotSession>>(emptyList()) }
    var selected by remember { mutableStateOf<HotspotSession?>(null) }
    var devices by remember { mutableStateOf<List<SessionDeviceUsage>>(emptyList()) }

    LaunchedEffect(Unit) {
        sessions = withContext(Dispatchers.IO) {
            DatabaseProvider.get(context).sessionDao().getRecentSessions(50).first()
        }
    }

    LaunchedEffect(selected) {
        selected?.let { s ->
            devices = withContext(Dispatchers.IO) {
                DatabaseProvider.get(context).sessionDao().getDevicesForSession(s.id)
            }
        } ?: run { devices = emptyList() }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = onBack) { Text("Kembali") }

        if (selected == null) {
            Text("History Sesi Hotspot", style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 8.dp))
            if (sessions.isEmpty()) {
                Text("Belum ada sesi tersimpan.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sessions) { s ->
                        val summary = SessionSummaryCalculator.summarize(s, emptyList())
                        Card(modifier = Modifier.fillMaxWidth().clickable { selected = s }) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(formatDateTime(s.startedMs))
                                Text("Durasi: ${formatDuration((s.endedMs ?: s.startedMs) - s.startedMs)}")
                                Text("Total: ${formatBytes(s.totalRxBytes + s.totalTxBytes)}")
                                Text("Device: ${s.deviceCount}")
                            }
                        }
                    }
                }
            }
        } else {
            val summary = SessionSummaryCalculator.summarize(selected!!, devices)
            Text("Sesi ${formatDateTime(selected!!.startedMs)}",
                style = MaterialTheme.typography.titleMedium)
            Text("Durasi: ${formatDuration(summary.durationMin * 60_000)}")
            Text("Total: ${formatBytes(summary.totalRxBytes + summary.totalTxBytes)}  (${summary.totalMB} MB)")
            Text("Device: ${summary.deviceCount}", modifier = Modifier.padding(bottom = 8.dp))

            if (summary.perDevice.isEmpty()) {
                Text("Tidak ada data per-device untuk sesi ini.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(summary.perDevice) { d ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("MAC: ${formatMac(d.macAddress)}  •  ${d.sharePct}%")
                                Text("↓ ${formatBytes(d.estRxBytes)}   ↑ ${formatBytes(d.estTxBytes)}  (estimasi)")
                            }
                        }
                    }
                }
            }
            Text(
                "Angka per-device adalah estimasi.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 12.dp)
            )
            Button(onClick = { selected = null }, modifier = Modifier.padding(top = 8.dp)) {
                Text("Tutup detail")
            }
        }
    }
}
