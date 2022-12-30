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
        entryId = getEntryIdFromShareURL()
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
        // ///https://mobileapps.envalfresco.com/#/preview/s/8ljHcqjSQ1ObHJFPMhXeJA
        // ///https://mobileapps.envalfresco.com/#/personal-files/a7422279-ba45-4be1-8e3a-6afc1c462481
        // ///https://mobileapps.envalfresco.com/#/personal-files/a7422279-ba45-4be1-8e3a-6afc1c462481/(viewer:view/2f86c9ed-5e9c-4de8-b7b1-e12921322d1c)?location=%2Fpersonal-files%2Fa7422279-ba45-4be1-8e3a-6afc1c462481
        val extData = intent.data.toString()

        if (!extData.contains("androidamw:///")) return ""

        if (extData.contains("#/preview")) {
            isPreview = true
            return extData.substringAfter("androidamw:///").substringBeforeLast("#Intent")
        }

        if (!extData.contains("#/personal-files")) return ""

        return if (extData.contains("viewer:view")) {
            extData.substringAfter("viewer:view/").substringBefore(")")
        } else {
            isRemoteFolder = true
            extData.substringAfter("#/personal-files/").substringBefore("/")
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
        private const val KEY_FOLDER = "folder"
    }
}
