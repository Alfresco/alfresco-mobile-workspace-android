package com.alfresco.content.app.activity

import android.content.Intent
import com.alfresco.auth.AuthConfig
import com.alfresco.auth.Credentials

class LoginActivity: com.alfresco.auth.activity.LoginActivity() {
    override fun onCredentials(credentials: Credentials, endpoint: String, authConfig: AuthConfig) {
        startActivity( Intent(this, MainActivity::class.java))
    }
}
