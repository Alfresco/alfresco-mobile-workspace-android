package com.alfresco.content.data

import com.alfresco.auth.AuthInterceptor
import com.alfresco.auth.AuthType
import com.alfresco.content.apis.AuthenticationApi
import com.alfresco.content.models.TicketBody
import com.alfresco.content.session.ActionSessionInvalid
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.content.session.SessionNotFoundException
import com.alfresco.events.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthenticationRepository {
    lateinit var session: Session
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        try {
            session = SessionManager.requireSession
        } catch (e: SessionNotFoundException) {
            e.printStackTrace()
            coroutineScope.launch {
                EventBus.default.send(ActionSessionInvalid(true))
            }
        }
    }

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
