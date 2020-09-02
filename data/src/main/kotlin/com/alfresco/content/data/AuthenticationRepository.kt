package com.alfresco.content.data

import com.alfresco.content.apis.AuthenticationApi
import com.alfresco.content.session.SessionManager

class AuthenticationRepository() {
    private val service: AuthenticationApi by lazy {
        SessionManager.requireSession.createService(AuthenticationApi::class.java)
    }

    suspend fun fetchTicket(): String? {
        return service.validateTicket().entry.id
    }
}
