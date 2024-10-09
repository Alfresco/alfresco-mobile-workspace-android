package com.alfresco.content.session

import android.content.Context
import com.alfresco.content.account.Account

object SessionManager {
    var currentSession: Session? = null
    val requireSession
        get() = currentSession ?: throw SessionNotFoundException("Session is not valid")

    fun newSession(context: Context): Session? {
        // Cleanup current session
        currentSession?.finish()

        // Create a new session
        val account = Account.getAccount(context)
        currentSession =
            if (account != null) {
                Session(
                    context.applicationContext,
                    account,
                )
            } else {
                null
            }
        return currentSession
    }
}
