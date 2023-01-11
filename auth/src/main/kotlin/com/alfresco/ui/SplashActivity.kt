package com.alfresco.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.alfresco.android.aims.R

abstract class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())
    private var entryId = ""
    private var isPreview = false
    private var isRemoteFolder = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.data != null && intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == 0) {
            // Handle the url passed through the intent
            entryId = getEntryIdFromShareURL()
        }
        setContentView(R.layout.activity_alfresco_splash)
    }

    override fun onPause() {
        super.onPause()

        // On configuration change and on background cancel previous handler
        handler.removeCallbacksAndMessages(null)
    }

    private fun getEntryIdFromShareURL(): String {
        isPreview = false
        isRemoteFolder = false
        val extData = intent.data.toString()

        if (!extData.contains(SCHEME)) return ""

        if (extData.contains(IDENTIFIER_PREVIEW)) {
            isPreview = true
            return extData.substringAfter(SCHEME)
        }

        if (!extData.contains(IDENTIFIER_PERSONAL_FILES)) return ""

        return if (extData.contains(IDENTIFIER_VIEWER)) {
            extData.substringAfter(IDENTIFIER_VIEWER).substringBefore(DELIMITER_BRACKET)
        } else {
            isRemoteFolder = true
            extData.substringAfter(IDENTIFIER_PERSONAL_FILES).substringBefore(DELIMITER_FORWARD_SLASH)
        }
    }

    override fun onResume() {
        super.onResume()

        // Transition after delay
        handler.postDelayed({
            goToMain()
        }, DISPLAY_TIMEOUT)
    }

    private fun goToMain() {
        val i = getMainIntent()
        if (entryId.isNotEmpty()) {
            i.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
//            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            i.putExtra(MODE_KEY, if (isPreview) VALUE_SHARE else VALUE_REMOTE)
            i.putExtra(KEY_FOLDER, isRemoteFolder)
            i.putExtra(ID_KEY, entryId)
        }
        startActivity(i)
        overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out)
        finish()
    }

    abstract fun getMainIntent(): Intent

    companion object {
        private const val DISPLAY_TIMEOUT = 100L
        private const val ID_KEY = "id"
        private const val MODE_KEY = "mode"
        private const val VALUE_REMOTE = "remote"
        private const val VALUE_SHARE = "share"
        const val KEY_FOLDER = "folder"
        const val SCHEME = "androidamw:///"
        const val IDENTIFIER_PREVIEW = "#/preview"
        const val IDENTIFIER_VIEWER = "viewer:view/"
        const val IDENTIFIER_PERSONAL_FILES = "#/personal-files/"
        const val DELIMITER_BRACKET = ")"
        const val DELIMITER_FORWARD_SLASH = "/"
    }
}
