package com.alfresco.content.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ConnectivityTracker {
    private var isTracking = false
    private val _networkAvailable = MutableStateFlow(false)
    val networkAvailable: StateFlow<Boolean> = _networkAvailable

    @RequiresApi(24)
    fun startTracking(context: Context) {
        if (isTracking) return

        val cm = context.getSystemService<ConnectivityManager>()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _networkAvailable.value = true
            }

            override fun onLost(network: Network) {
                _networkAvailable.value = false
            }
        }
        cm?.registerDefaultNetworkCallback(networkCallback)
        isTracking = true
    }

    fun isActiveNetworkMetered(context: Context): Boolean =
        context.getSystemService<ConnectivityManager>()?.isActiveNetworkMetered == true
}
