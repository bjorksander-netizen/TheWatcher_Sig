package com.example.thewatcher.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.thewatcher.R
import com.example.thewatcher.data.db.DatabaseProvider
import com.example.thewatcher.monitor.ClientDetector
import com.example.thewatcher.monitor.MonitorStateHolder
import com.example.thewatcher.monitor.PerDeviceSplitter
import com.example.thewatcher.monitor.TrafficSampler
import com.example.thewatcher.data.model.ConnectedClient
import com.example.thewatcher.data.model.DailyTotal
import com.example.thewatcher.data.model.HotspotSession
import com.example.thewatcher.data.model.SessionDeviceUsage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

class MonitorService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var pollJob: Job? = null

    private lateinit var detector: ClientDetector
    private lateinit var trafficSampler: TrafficSampler
    private var currentSessionId: Long = -1L

    // Baseline + running accumulators for the active session.
    private var startMobileRx = 0L
    private var startMobileTx = 0L
    private var sessionRx = 0L
    private var sessionTx = 0L
    private val clientDurations = ConcurrentHashMap<String, Long>() // mac -> accumulated ms
    private var lastTickMs = 0L
    private val sessionDeviceRx = ConcurrentHashMap<String, Long>()
    private val sessionDeviceTx = ConcurrentHashMap<String, Long>()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        trafficSampler = TrafficSampler(applicationContext)
        detector = ClientDetector(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        startMonitoring()
        return START_STICKY
    }

    private fun startForeground() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIF_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(NOTIF_ID, notification)
        }
        MonitorStateHolder.update { it.copy(isMonitoring = true) }
    }

    private fun startMonitoring() {
        serviceScope.launch {
            // Open a new session.
            val db = DatabaseProvider.get(applicationContext)
            val session = HotspotSession(startedMs = System.currentTimeMillis())
            currentSessionId = db.sessionDao().insertSession(session)

            startMobileRx = trafficSampler.getMobileRxBytes()
            startMobileTx = trafficSampler.getMobileTxBytes()
            sessionRx = 0L
            sessionTx = 0L
            lastTickMs = System.currentTimeMillis()

            pollJob = launch {
                while (isActive) {
                    delay(POLL_INTERVAL_MS)
                    tick()
                }
            }
        }
    }

    private suspend fun tick() {
        val now = System.currentTimeMillis()

        // Bug fix #1: try multiple strategies; capture diagnostic for UI.
        val clients = detector.detect()
        val diag = detector.getDiagnostic()

        val mobileRxNow = trafficSampler.getMobileRxBytes()
        val mobileTxNow = trafficSampler.getMobileTxBytes()

        // Bug fix #2: use delta from baseline, not raw cumulative total.
        val deltaRx = (mobileRxNow - startMobileRx).coerceAtLeast(0L)
        val deltaTx = (mobileTxNow - startMobileTx).coerceAtLeast(0L)

        sessionRx = deltaRx
        sessionTx = deltaTx

        // Update per-client durations for this interval.
        val interval = (now - lastTickMs).coerceAtLeast(0L)
        val weightClients = clients.map { c ->
            val acc = clientDurations.getOrDefault(c.macAddress, 0L) + interval
            clientDurations[c.macAddress] = acc
            PerDeviceSplitter.ClientWeight(c.macAddress, acc)
        }
        lastTickMs = now

        val split = PerDeviceSplitter.split(sessionRx, sessionTx, weightClients)
        val db = DatabaseProvider.get(applicationContext)
        val clientNow = System.currentTimeMillis()

        // Persist clients + per-device session usage.
        val connectedClients = clients.map { c ->
            val (erx, etx) = split[c.macAddress] ?: (0L to 0L)
            sessionDeviceRx[c.macAddress] = (sessionDeviceRx[c.macAddress] ?: 0L) + erx
            sessionDeviceTx[c.macAddress] = (sessionDeviceTx[c.macAddress] ?: 0L) + etx
            val prevRx = sessionDeviceRx[c.macAddress] ?: 0L
            val prevTx = sessionDeviceTx[c.macAddress] ?: 0L
            c.copy(
                firstSeenMs = c.firstSeenMs,
                lastSeenMs = clientNow,
                estRxBytes = prevRx,
                estTxBytes = prevTx
            )
        }

        db.clientDao().upsertAll(connectedClients)
        for ((mac, erx) in sessionDeviceRx) {
            db.sessionDao().upsertDeviceUsage(
                SessionDeviceUsage(
                    sessionId = currentSessionId,
                    macAddress = mac,
                    estRxBytes = erx,
                    estTxBytes = sessionDeviceTx[mac] ?: 0L
                )
            )
        }
        db.sessionDao().updateTotals(currentSessionId, sessionRx, sessionTx, clients.size)

        // Daily total.
        val dateText = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val day = db.dailyTotalDao().getByDate(dateText)
            ?: DailyTotal(dateText = dateText)
        db.dailyTotalDao().upsert(
            day.copy(rxBytes = day.rxBytes + 0L, txBytes = day.txBytes + 0L)
        )

        MonitorStateHolder.update { st ->
            st.copy(
                isMonitoring = true,
                sessionRxBytes = sessionRx,
                sessionTxBytes = sessionTx,
                clients = connectedClients,
                diagnostic = diag
            )
        }
    }

    override fun onDestroy() {
        runCatching {
            serviceScope.launch {
                val db = DatabaseProvider.get(applicationContext)
                db.sessionDao().closeSession(
                    currentSessionId,
                    endedMs = System.currentTimeMillis(),
                    rx = sessionRx,
                    tx = sessionTx,
                    count = clientDurations.size
                )
            }
            pollJob?.cancel()
            detector.release()
        }
        MonitorStateHolder.reset()
        stopForeground(STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.notif_title))
            .setContentText(getString(R.string.notif_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notif_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = getString(R.string.notif_channel_desc) }
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr.createNotificationChannel(channel)
        }
    }

    companion object {
        const val NOTIF_ID = 1001
        const val CHANNEL_ID = "thewatcher_channel"
        const val POLL_INTERVAL_MS = 5_000L
    }
}
