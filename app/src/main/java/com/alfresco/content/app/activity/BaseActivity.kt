package com.alfresco.content.app.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.alfresco.content.listview.ListViewModel

/**
 * Marked as BaseActivity class
 */
open class BaseActivity : AppCompatActivity() {

    var screenType: ScreenType = ScreenType.NONE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (screenType == ScreenType.MainActivity) saveEventRegisteredState()
    }

    override fun onStart() {
        super.onStart()
        if (screenType == ScreenType.MoveActivity || screenType == ScreenType.ExtensionActivity)
            saveEventRegisteredState()
    }

    private fun saveEventRegisteredState() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = sharedPrefs.edit()
        editor.putBoolean(ListViewModel.IS_EVENT_REGISTERED, false)
        editor.apply()
    }
}

/**
 * Marked as ScreenType
 */
enum class ScreenType {
    MainActivity,
    ExtensionActivity,
    MoveActivity,
    NONE
}
