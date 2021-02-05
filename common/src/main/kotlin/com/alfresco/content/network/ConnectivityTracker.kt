package com.alfresco.content.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.asFlow

object ConnectivityTracker {
    private var isTracking = false
    private val availableChannel = ConflatedBroadcastChannel<Boolean>()

    @RequiresApi(24)
    fun startTracking(context: Context) {
        if (isTracking) return

        val cm = context.getSystemService<ConnectivityManager>()
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                availableChannel.offer(true)
            }

            override fun onLost(network: Network) {
                availableChannel.offer(false)
            }
        }
        cm?.registerDefaultNetworkCallback(networkCallback)
        isTracking = true
    }

    fun isActiveNetworkMetered(context: Context): Boolean =
        context.getSystemService<ConnectivityManager>()?.isActiveNetworkMetered == true

    val networkAvailable = availableChannel.asFlow()
}
