package com.alfresco.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.alfresco.android.aims.R
import com.alfresco.content.common.SharedURLParser
import com.alfresco.content.common.SharedURLParser.Companion.ID_KEY
import com.alfresco.content.common.SharedURLParser.Companion.KEY_FOLDER
import com.alfresco.content.common.SharedURLParser.Companion.MODE_KEY
import com.alfresco.content.common.SharedURLParser.Companion.VALUE_REMOTE
import com.alfresco.content.common.SharedURLParser.Companion.VALUE_SHARE
import com.alfresco.content.data.rooted.CheckForRootWorker
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class SplashActivity : AppCompatActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private var entryId = ""
    private var isPreview = false
    private var isRemoteFolder = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.data != null && intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY == 0) {
            // Handle the url passed through the intent
            val urlData = SharedURLParser().getEntryIdFromShareURL(intent.data.toString())
            isRemoteFolder = urlData.third
            entryId = urlData.second
            isPreview = urlData.first
        }
        setContentView(R.layout.activity_alfresco_splash)
    }

    override fun onPause() {
        super.onPause()

        // On configuration change and on background cancel previous handler
        handler.removeCallbacksAndMessages(null)
    }

    override fun onResume() {
        super.onResume()

        // Transition after delay
        handler.postDelayed({
            goToMain()
        }, DISPLAY_TIMEOUT)
    }

    private fun goToMain() {
        GlobalScope.launch {
            val checkForRoot = CheckForRootWorker(this@SplashActivity)
            val results = checkForRoot.invoke()

            if (!results.any { it.result }) {
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
            }
            finish()
        }
    }

    abstract fun getMainIntent(): Intent

    companion object {
        private const val DISPLAY_TIMEOUT = 100L
    }
}
