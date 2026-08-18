package com.example.thewatcher.monitor

import com.example.thewatcher.data.model.ConnectedClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the live monitoring state so both MonitorService (writer) and the
 * Compose UI (reader) can share it without a ServiceConnection.
 */
data class MonitorUiState(
    val isMonitoring: Boolean = false,
    val sessionRxBytes: Long = 0L,
    val sessionTxBytes: Long = 0L,
    val todayRxBytes: Long = 0L,
    val todayTxBytes: Long = 0L,
    val clients: List<ConnectedClient> = emptyList(),
    val diagnostic: String = "",
    val error: String? = null
)

object MonitorStateHolder {
    private val _state = MutableStateFlow(MonitorUiState())
    val state = _state.asStateFlow()

    fun update(block: (MonitorUiState) -> MonitorUiState) {
        _state.value = block(_state.value)
    }

    fun reset() {
        _state.value = MonitorUiState()
    }
}
