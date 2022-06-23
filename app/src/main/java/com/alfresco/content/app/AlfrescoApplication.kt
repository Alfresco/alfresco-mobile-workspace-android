package com.alfresco.content.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.airbnb.mvrx.Mavericks
import com.alfresco.Logger
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.Settings
import com.alfresco.content.data.SyncWorker
import com.alfresco.content.network.ConnectivityTracker
import com.alfresco.content.viewer.ViewerRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Suppress("unused")
class AlfrescoApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var settings: Settings

    override fun onCreate() {
        super.onCreate()
        AnalyticsManager().appLaunch()
        Logger.init(BuildConfig.DEBUG)

        ViewerRegistry.setup()
        SyncWorker.use(ViewerRegistry)

        settings = Settings(this)
        settings.observeChanges()

        applicationScope.launch {
            settings.observeTheme().collect {
                updateAppTheme(it)
            }
        }

        ConnectivityTracker.startTracking(this)

        Mavericks.initialize(this)
    }

    private fun updateAppTheme(theme: Settings.Theme) {
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                Settings.Theme.Light -> {
                    AnalyticsManager().theme(Settings.Theme.Light.name.lowercase())
                    AppCompatDelegate.MODE_NIGHT_NO
                }
                Settings.Theme.Dark -> {
                    AnalyticsManager().theme(Settings.Theme.Dark.name.lowercase())
                    AppCompatDelegate.MODE_NIGHT_YES
                }
                Settings.Theme.System -> {
                    AnalyticsManager().theme(Settings.Theme.System.name.lowercase())
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
            }
        )
    }
}
