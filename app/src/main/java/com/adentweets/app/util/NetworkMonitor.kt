package com.adentweets.app.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class NetworkMonitor {
    private val _isConnected = MutableStateFlow(true)
    val isConnected: Flow<Boolean> = _isConnected

    fun updateConnectionStatus(connected: Boolean) {
        _isConnected.value = connected
    }
}