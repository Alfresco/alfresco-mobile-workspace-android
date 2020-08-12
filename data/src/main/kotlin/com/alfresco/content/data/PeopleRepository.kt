package com.alfresco.content.data

import android.net.Uri
import com.alfresco.content.apis.PeopleApi
import com.alfresco.content.models.Person
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager

class PeopleRepository(session: Session = SessionManager.requireSession) {

    private val service: PeopleApi by lazy {
        session.createService(PeopleApi::class.java)
    }

    suspend fun me(): Person {
        return service.getPerson("-me-", null).entry
    }

    companion object {
        fun myPicture(): Uri {
            return Uri.parse(SessionManager.currentSession?.baseUrl + "alfresco/versions/1/people/-me-/avatar")
        }
    }
}
