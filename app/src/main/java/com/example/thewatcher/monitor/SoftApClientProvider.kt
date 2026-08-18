package com.example.thewatcher.monitor

import android.annotation.SuppressLint
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.example.thewatcher.data.model.ConnectedClient
import java.lang.reflect.Proxy
import java.util.concurrent.Executors

/**
 * API 30+ (Android 11+): uses the official SoftAp callback to enumerate
 * connected clients. Implemented via reflection so it compiles on SDK images
 * whose android.jar omits the public WifiClient / SoftApCallback classes.
 * At runtime on a real device (API 30+) these classes exist and are used.
 * Needs ACCESS_FINE_LOCATION or NEARBY_WIFI_DEVICES.
 */
@SuppressLint("MissingPermission", "PrivateApi")
class SoftApClientProvider(private val wifiManager: WifiManager) : ClientProvider {

    @Volatile
    private var latestClients: List<ConnectedClient> = emptyList()

    private var registered = false
    private val executor = Executors.newSingleThreadExecutor()

    private val callbackProxy: Any? by lazy {
        try {
            val cbClass = Class.forName("android.net.wifi.WifiManager\$SoftApCallback")
            Proxy.newProxyInstance(
                cbClass.classLoader,
                arrayOf(cbClass)
            ) { _, method, args ->
                if (method.name == "onConnectedClientsChanged" && args != null && args.isNotEmpty()) {
                    @Suppress("UNCHECKED_CAST")
                    val list = args[0] as? List<Any>
                    latestClients = list?.mapNotNull { toClient(it) } ?: emptyList()
                }
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun toClient(raw: Any): ConnectedClient? {
        return try {
            val mac = raw.javaClass.getMethod("getMacAddress").invoke(raw) as? String ?: return null
            val ip = try {
                val ia = raw.javaClass.getMethod("getIpAddress").invoke(raw)
                ia?.javaClass?.getMethod("getHostAddress")?.invoke(ia) as? String ?: ""
            } catch (_: Exception) { "" }
            val now = System.currentTimeMillis()
            ConnectedClient(
                macAddress = mac,
                ipAddress = ip,
                firstSeenMs = now,
                lastSeenMs = now
            )
        } catch (_: Exception) {
            null
        }
    }

    fun register() {
        if (registered) return
        val proxy = callbackProxy ?: return
        try {
            val method = wifiManager.javaClass.getMethod(
                "registerSoftApCallback",
                java.util.concurrent.Executor::class.java,
                Class.forName("android.net.wifi.WifiManager\$SoftApCallback")
            )
            method.invoke(wifiManager, executor, proxy)
            registered = true
        } catch (_: Exception) {
            registered = false
        }
    }

    override suspend fun getClients(): List<ConnectedClient> = latestClients

    override fun release() {
        if (!registered) return
        try {
            val proxy = callbackProxy ?: return
            val method = wifiManager.javaClass.getMethod(
                "unregisterSoftApCallback",
                Class.forName("android.net.wifi.WifiManager\$SoftApCallback")
            )
            method.invoke(wifiManager, proxy)
        } catch (_: Exception) {
            // ignore
        } finally {
            registered = false
        }
    }
}
