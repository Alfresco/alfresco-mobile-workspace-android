package com.alfresco.content

import android.content.Context
import android.net.ConnectivityManager
import android.net.IpPrefix
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.alfresco.content.common.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.net.InetAddress
import kotlin.coroutines.cancellation.CancellationException

/**
 * Android 17 (API 37) blocks connections to hosts on the device's local subnet unless
 * ACCESS_LOCAL_NETWORK is granted at runtime, so on-prem servers on the same LAN need it.
 */
object LocalNetworkPermission {
    const val PERMISSION: String = "android.permission.ACCESS_LOCAL_NETWORK"
    private const val ANDROID_17: Int = 37

    /**
     * Requests the permission only when [server] resolves to a local-network address and it isn't granted yet.
     * Returns false only when the user declines.
     */
    suspend fun requestIfNeeded(
        context: Context,
        server: String,
    ): Boolean {
        val needed: Boolean = withContext(Dispatchers.IO) { isNeeded(context, server) }
        if (!needed) return true
        return try {
            PermissionFragment.requestPermission(context, PERMISSION, context.getString(R.string.local_network_permission_rationale))
        } catch (ex: CancellationException) {
            // PermissionFragment cancels the request when the rationale is dismissed; real cancellation still propagates.
            currentCoroutineContext().ensureActive()
            false
        }
    }

    private fun isNeeded(
        context: Context,
        server: String,
    ): Boolean {
        if (Build.VERSION.SDK_INT < ANDROID_17) return false
        if (PermissionFragment.deniedPermissions(context, listOf(PERMISSION)).isEmpty()) return false
        val host: String = hostOf(server) ?: return false
        val addresses: List<InetAddress> = runCatching { InetAddress.getAllByName(host).toList() }.getOrDefault(emptyList())
        val subnets: List<IpPrefix> = localSubnets(context)
        return addresses.any { address -> isLocal(address, subnets) }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun isLocal(
        address: InetAddress,
        subnets: List<IpPrefix>,
    ): Boolean = address.isLinkLocalAddress || address.isMulticastAddress || subnets.any { it.contains(address) }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun localSubnets(context: Context): List<IpPrefix> {
        val connectivity: ConnectivityManager = context.getSystemService(ConnectivityManager::class.java) ?: return emptyList()
        val linkAddresses = connectivity.getLinkProperties(connectivity.activeNetwork)?.linkAddresses ?: return emptyList()
        return linkAddresses.map { IpPrefix(it.address, it.prefixLength) }
    }

    private fun hostOf(server: String): String? {
        val trimmed: String = server.trim()
        if (trimmed.isEmpty()) return null
        val withScheme: String = if (trimmed.contains("://")) trimmed else "https://$trimmed"
        return Uri.parse(withScheme).host?.takeIf { it.isNotEmpty() }
    }
}
