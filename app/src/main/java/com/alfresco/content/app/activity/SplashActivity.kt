package com.alfresco.content.app.activity

import android.content.Intent
import android.os.Bundle

class SplashActivity : com.alfresco.ui.SplashActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun getMainIntent(): Intent {
        return Intent(this, MainActivity::class.java)
    }
}
