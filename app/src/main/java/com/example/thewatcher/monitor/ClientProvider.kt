package com.example.thewatcher.monitor

import com.example.thewatcher.data.model.ConnectedClient

/**
 * Abstraction for obtaining the list of clients currently connected to the
 * device's hotspot. Two implementations exist: SoftApClientProvider (API 31+)
 * and ArpClientProvider (older devices, via /proc/net/arp).
 */
interface ClientProvider {
    /** Returns the currently connected clients. */
    suspend fun getClients(): List<ConnectedClient>
    /** Release any resources / listeners. */
    fun release()
}
