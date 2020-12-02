package com.alfresco.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.alfresco.android.aims.R

abstract class SplashActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        val i = getMainIntent()
        startActivity(i)
        overridePendingTransition(R.anim.anim_fade_in, R.anim.anim_fade_out)
        finish()
    }

    abstract fun getMainIntent(): Intent

    companion object {
        private const val DISPLAY_TIMEOUT = 100L
    }
}
