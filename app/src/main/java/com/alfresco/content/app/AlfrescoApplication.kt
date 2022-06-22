package com.alfresco.content.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.airbnb.mvrx.Mavericks
import com.alfresco.Logger
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

//        throw NullPointerException("test crash")
    }

    private fun updateAppTheme(theme: Settings.Theme) {
        AppCompatDelegate.setDefaultNightMode(
            when (theme) {
                Settings.Theme.Light -> AppCompatDelegate.MODE_NIGHT_NO
                Settings.Theme.Dark -> AppCompatDelegate.MODE_NIGHT_YES
                Settings.Theme.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }
}
