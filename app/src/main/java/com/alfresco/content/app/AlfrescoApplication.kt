package com.alfresco.content.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.alfresco.content.data.Settings
import com.alfresco.content.network.ConnectivityTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AlfrescoApplication : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var settings: Settings

    override fun onCreate() {
        super.onCreate()

        settings = Settings(this)
        settings.observeChanges()

        applicationScope.launch {
            settings.observeTheme().collect {
                updateAppTheme(it)
            }
        }

        ConnectivityTracker.startTracking(this)
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
