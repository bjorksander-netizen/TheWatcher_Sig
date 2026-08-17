package com.example.thewatcher.monitor

import android.annotation.SuppressLint
import android.net.wifi.WifiClient
import android.net.wifi.WifiManager
import android.os.Build
import com.example.thewatcher.data.model.ConnectedClient
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/**
 * API 31+ (Android 12+): uses the official SoftAp callback to enumerate
 * connected clients. Needs ACCESS_FINE_LOCATION or NEARBY_WIFI_DEVICES.
 */
@SuppressLint("MissingPermission")
class SoftApClientProvider(private val wifiManager: WifiManager) : ClientProvider {

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.S)
    private val callback = object : WifiManager.SoftApCallback {
        override fun onConnectedClientsChanged(clients: MutableList<WifiClient>) {
            latestClients = clients
        }
    }

    @Volatile
    private var latestClients: List<WifiClient> = emptyList()

    private var registered = false

    @SuppressLint("MissingPermission")
    fun register() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !registered) {
            wifiManager.registerSoftApCallback({ it.run() }, callback)
            registered = true
        }
    }

    override suspend fun getClients(): List<ConnectedClient> {
        return latestClients.map { client ->
            val mac = client.macAddress?.toString() ?: "unknown"
            val ip = client.ipAddress?.hostAddress ?: ""
            ConnectedClient(
                macAddress = mac,
                ipAddress = ip,
                firstSeenMs = System.currentTimeMillis(),
                lastSeenMs = System.currentTimeMillis()
            )
        }
    }

    override fun release() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && registered) {
            wifiManager.unregisterSoftApCallback(callback)
            registered = false
        }
    }
}
