package com.alfresco.dbp.sample.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

/**
 * Created by Bogdan Roatis on 8/26/2019.
 */
object ConnectivityReceiver : BroadcastReceiver() {

    private val listeners: MutableList<ConnectivityReceiverListener> = mutableListOf()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            listeners.forEach { it.onNetworkConnectionChanged(isConnectedOrConnecting(context)) }
        }
    }

    private fun isConnectedOrConnecting(context: Context?): Boolean {
        val connMgr = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkInfo = connMgr?.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }

    fun addConnectivityReceiverListener(connectivityReceiverListener: ConnectivityReceiverListener) {
        if (!listeners.contains(connectivityReceiverListener)) {
            listeners.add(connectivityReceiverListener)
        }
    }

    fun removeConnectivityReceiverListener(connectivityReceiverListener: ConnectivityReceiverListener) {
        listeners.remove(connectivityReceiverListener)
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }
}
