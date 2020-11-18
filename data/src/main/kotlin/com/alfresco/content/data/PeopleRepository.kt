package com.alfresco.content.data

import android.net.Uri
import com.alfresco.content.apis.AlfrescoApi
import com.alfresco.content.apis.PeopleApi
import com.alfresco.content.models.Person
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager

class PeopleRepository(session: Session = SessionManager.requireSession) {

    private val service: PeopleApi by lazy {
        session.createService(PeopleApi::class.java)
    }

    suspend fun me(): Person =
        service.getPerson(AlfrescoApi.CURRENT_USER).entry

    companion object {
        fun myPicture(): Uri {
            val baseUrl = SessionManager.currentSession?.baseUrl
            val userId = AlfrescoApi.CURRENT_USER
            return Uri.parse("${baseUrl}alfresco/versions/1/people/$userId/avatar")
        }
    }
}
