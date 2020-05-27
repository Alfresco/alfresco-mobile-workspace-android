package com.alfresco.content.account

import android.app.Service
import android.content.Intent
import android.os.IBinder

/**
 * A bound Service that instantiates the authenticator
 * when started.
 */
class AuthenticatorService : Service() {

    private lateinit var authenticator: com.alfresco.content.account.Authenticator

    override fun onCreate() {
        authenticator = com.alfresco.content.account.Authenticator(this)
    }

    override fun onBind(intent: Intent?): IBinder = authenticator.iBinder
}
