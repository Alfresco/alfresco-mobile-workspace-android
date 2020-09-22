package com.alfresco.content.data

import com.alfresco.auth.AuthInterceptor
import com.alfresco.auth.AuthType
import com.alfresco.content.apis.AuthenticationApi
import com.alfresco.content.models.TicketBody
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager

class AuthenticationRepository(val session: Session = SessionManager.requireSession) {
    private val service: AuthenticationApi by lazy {
        session.createService(AuthenticationApi::class.java)
    }

    suspend fun fetchTicket(): String? {
        return if (session.account.authType == AuthType.BASIC.value) {
            service.createTicket(createTicketRequest()).entry.id
        } else {
            service.validateTicket().entry.id
        }
    }

    private fun createTicketRequest(): TicketBody {
        val creds = AuthInterceptor.decodeBasicState(session.account.authState)
        return TicketBody(creds?.first, creds?.second)
    }
}
