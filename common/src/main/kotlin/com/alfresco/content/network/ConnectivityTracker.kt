package com.alfresco.content.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
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

    /**
     * returns true if network is metered otherwise false
     */
    fun isActiveNetworkMetered(context: Context): Boolean =
        context.getSystemService<ConnectivityManager>()?.isActiveNetworkMetered == true

    /**
     * returns true if network is active otherwise false
     */
    fun isActiveNetwork(context: Context): Boolean {
        val connectivityManager = context.getSystemService<ConnectivityManager>() ?: return false

        val network = context.getSystemService<ConnectivityManager>()?.activeNetwork ?: return false

        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            // Indicates this network uses a Wi-Fi transport,
            // or WiFi has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

            // Indicates this network uses a Cellular transport. or
            // Cellular has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            // else return false
            else -> false
        }
    }
}
