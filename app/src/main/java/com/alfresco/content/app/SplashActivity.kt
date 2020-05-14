package com.alfresco.content.app

import android.content.Intent

class SplashActivity: com.alfresco.ui.SplashActivity() {
    override fun getMainIntent(): Intent {
        return Intent(this, MainActivity::class.java)
    }
}

