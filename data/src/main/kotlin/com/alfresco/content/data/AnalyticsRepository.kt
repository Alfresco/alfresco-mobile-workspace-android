package com.alfresco.content.data

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.content.getSystemService
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

class AnalyticsRepository(val session: Session = SessionManager.requireSession) {

    private val context get() = session.context
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    private fun serverURL() = session.account.serverUrl

    private fun deviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL

        if (model.lowercase().startsWith(manufacturer.lowercase()))
            return model.uppercase()

        return "${manufacturer.uppercase()} $model"
    }

    private fun deviceOS() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        "Android: ${Build.VERSION.RELEASE_OR_CODENAME}"
    } else {
        "Android: ${Build.VERSION.RELEASE}"
    }

    private fun deviceNetwork(): String {
        val cm = context.getSystemService<ConnectivityManager>() ?: return NetworkStatus.NOT_CONNECTED.name
        val nw = cm.activeNetwork ?: return NetworkStatus.NOT_CONNECTED.name
        val actNw = cm.getNetworkCapabilities(nw) ?: return NetworkStatus.NOT_CONNECTED.name

        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.WIFI.name
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.CELLULAR.name
            else -> NetworkStatus.NOT_CONNECTED.name
        }
    }

    private fun appVersion(): String {
        val packageManager = context.packageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageManager.getPackageInfo(context.packageName, 0).longVersionCode.toString()
        } else {
            packageManager.getPackageInfo(context.packageName, 0).versionCode.toString()
        }
    }

    @SuppressLint("HardwareIds")
    private fun deviceID() = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    fun defaultParams(): Bundle {
        val bundle = Bundle().apply {
            putString(DefaultParameters.ServerURL.name, serverURL())
            putString(DefaultParameters.DeviceName.name, deviceName())
            putString(DefaultParameters.DeviceOS.name, deviceOS())
            putString(DefaultParameters.DeviceNetwork.name, deviceNetwork())
            putString(DefaultParameters.AppVersion.name, appVersion())
            putString(DefaultParameters.DeviceName.name, deviceID())
        }
        return bundle
    }

    fun logEvent(type: EventType, params: Bundle) {
        firebaseAnalytics.logEvent(type.name, params)
    }
}

enum class EventName(val value: String) {
    FilePreview("Event_FilePreview"),
    OpenWith("Event_OpenWith")
}

enum class EventType(val value: String) {
    ScreenView("screen_view"),
    ActionEvent("action_event"),
    ApiTracker("api_tracker")
}

enum class DefaultParameters(val value: String) {
    ServerURL("server_url"),
    DeviceName("device_name"),
    DeviceOS("device_os"),
    DeviceNetwork("device_network"),
    AppVersion("app_version"),
    DeviceID("device_id")
}

enum class NetworkStatus {
    NOT_CONNECTED,
    WIFI,
    CELLULAR
}
