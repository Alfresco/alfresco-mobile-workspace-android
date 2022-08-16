package com.alfresco.content.data

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.core.content.getSystemService
import com.alfresco.content.session.Session
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

/**
 * Marked as AnalyticsRepository class
 */
class AnalyticsRepository(val session: Session) {

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
        "android: ${Build.VERSION.RELEASE_OR_CODENAME}"
    } else {
        "android: ${Build.VERSION.RELEASE}"
    }

    private fun deviceNetwork(): String {
        val cm = context.getSystemService<ConnectivityManager>() ?: return NetworkStatus.NOT_CONNECTED.name
        val nw = cm.activeNetwork ?: return NetworkStatus.NOT_CONNECTED.name
        val actNw = cm.getNetworkCapabilities(nw) ?: return NetworkStatus.NOT_CONNECTED.name

        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkStatus.WIFI.name.lowercase()
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkStatus.CELLULAR.name.lowercase()
            else -> NetworkStatus.NOT_CONNECTED.name.lowercase()
        }
    }

    private fun appVersion() = context.packageManager.getPackageInfo(context.packageName, 0).versionName

    @SuppressLint("HardwareIds")
    private fun deviceID() = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)

    /**
     * default parameters added in bundle for analytics
     */
    fun defaultParams(): Bundle {
        val bundle = Bundle().apply {
            putString(DefaultParameters.ServerURL.value, serverURL())
            putString(DefaultParameters.DeviceName.value, deviceName())
            putString(DefaultParameters.DeviceOS.value, deviceOS())
            putString(DefaultParameters.DeviceNetwork.value, deviceNetwork())
            putString(DefaultParameters.AppVersion.value, appVersion())
            putString(DefaultParameters.DeviceID.value, deviceID())
        }
        return bundle
    }

    /**
     * It will get triggered to log analytics on firebase console
     */
    fun logEvent(type: String, params: Bundle) {
        firebaseAnalytics.logEvent(type, params)
    }
}

/**
 * Marked as EventName enum
 */
enum class EventName(val value: String) {
    FilePreview("event_file_preview"),
    Download("event_download"),
    AddFavorite("event_add_to_favorites"),
    RemoveFavorite("event_remove_from_favorite"),
    RenameNode("event_rename"),
    MoveToFolder("event_move"),
    MarkOffline("event_Make_available_offline"),
    RemoveOffline("event_remove_offline"),
    MoveTrash("event_move_to_trash"),
    ChangeTheme("event_change_theme"),
    CreateFolder("event_new_folder"),
    UploadMedia("event_upload_photos_or_videos"),
    CreateMedia("event_take_a_photo_or_video"),
    UploadFiles("event_upload_files"),
    AppLaunched("event_app_launched"),
    SearchFacets("event_search_facets"),
    PermanentlyDelete("event_permanently_delete"),
    Restore("event_restore"),
    OpenWith("event_open_with"),
    TaskFilterReset("event_reset"),
    None("event_none")
}

/**
 * Marked as PageView enum
 */
enum class PageView(val value: String) {
    Recent("page_view_recent"),
    Favorites("page_view_favorites"),
    Tasks("page_view_tasks"),
    Offline("page_view_offline"),
    Browse("page_view_browse"),
    PersonalFiles("page_view_personal_files"),
    MyLibraries("page_view_my_libraries"),
    Shared("page_view_shared"),
    Trash("page_view_trash"),
    Search("page_view_search"),
    ShareExtension("page_view_share_extension"),
    Transfers("page_view_transfers"),
    TaskView("page_view_task_view"),
    Comments("page_view_comments"),
    None("none")
}

/**
 * Marked as APIEvent enum
 */
enum class APIEvent(val value: String) {
    NewFolder("event_api_new_folder"),
    UploadFiles("event_api_upload_files"),
    Login("event_api_login")
}

/**
 * Marked as DefaultParameters enum
 */
enum class DefaultParameters(val value: String) {
    ServerURL("server_url"),
    DeviceName("device_name"),
    DeviceOS("device_os"),
    DeviceNetwork("device_network"),
    AppVersion("app_version"),
    DeviceID("device_id")
}

/**
 * Marked as NetworkStatus enum
 */
enum class NetworkStatus {
    NOT_CONNECTED,
    WIFI,
    CELLULAR
}

/**
 * Marked as Parameters enum
 */
enum class Parameters(val value: String) {
    EventName("event_name"),
    FileMimeType("file_mimetype"),
    FileExtension("file_extension"),
    FileSize("file_size"),
    ThemeName("theme_name"),
    NumberOfFiles("number_of_files"),
    FacetName("facet_name"),
    Success("success")
}
