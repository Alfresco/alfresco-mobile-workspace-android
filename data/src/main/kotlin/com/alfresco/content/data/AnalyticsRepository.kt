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
    fun logEvent(type: EventType, params: Bundle) {
        firebaseAnalytics.logEvent(type.value, params)
    }
}

/**
 * Marked as EventName enum
 */
enum class EventName(val value: String) {
    FilePreview("Event_filePreview"),
    Download("Event_download"),
    AddFavorite("Event_Add to Favorites"),
    RemoveFavorite("Event_Remove from Favorite"),
    RenameNode("Event_Rename"),
    MoveToFolder("Event_Move"),
    MarkOffline("Event_Make available offline"),
    RemoveOffline("Event_Remove offline"),
    MoveTrash("Event_Move to Trash"),
    ChangeTheme("Event_changeTheme"),
    CreateFolder("Event_New folder"),
    UploadMedia("Event_Upload photos or videos"),
    CreateMedia("Event_Take a photo or video"),
    UploadFiles("Event_Upload files"),
    AppLaunched("Event_appLaunched"),
    SearchFacets("Event_searchFacets"),
    PermanentlyDelete("Event_Permanently Delete"),
    Restore("Event_Restore"),
    OpenWith("Event_Open with")
}

/**
 * Marked as PageView enum
 */
enum class PageView(val value: String) {
    Recent("PageView_Recent"),
    Favorites("PageView_Favorites"),
    Offline("PageView_Offline"),
    Browse("PageView_Browse"),
    PersonalFiles("PageView_Personal files"),
    FolderName("PageView_FolderName"),
    MyLibraries("PageView_My libraries"),
    Shared("PageView_Shared"),
    Trash("PageView_Trash"),
    Search("PageView_Search"),
    ShareExtension("PageView_ShareExtension"),
    Transfers("PageView_Transfers"),
    None("none")
}

/**
 * Marked as APIEvent enum
 */
enum class APIEvent(val value: String) {
    NewFolder("Event_API_NewFolder"),
    UploadFiles("Event_API_UploadFiles"),
    Login("Event_API_Login")
}

/**
 * Marked as EventType enum
 */
enum class EventType(val value: String) {
    ScreenView("screen_views"),
    ActionEvent("action_event"),
    ApiTracker("api_tracker")
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
    FolderName("folder_name"),
    ThemeName("theme_name"),
    NumberOfFiles("number_of_files"),
    FacetName("facet_name"),
    NumberOfAssets("number_of_assets"),
    IsFile("is_file"),
    Success("success")
}