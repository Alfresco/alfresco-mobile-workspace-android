package com.alfresco.content.app.activity

import android.content.Intent
import com.alfresco.auth.AuthConfig
import com.alfresco.auth.Credentials
import com.alfresco.content.app.platform.Account

class LoginActivity: com.alfresco.auth.activity.LoginActivity() {
    override fun onCredentials(credentials: Credentials, endpoint: String, authConfig: AuthConfig) {
        Account.createAccount(this, credentials.username, credentials.authState, credentials.authType, endpoint)

        startActivity( Intent(this, MainActivity::class.java))
    }
}
