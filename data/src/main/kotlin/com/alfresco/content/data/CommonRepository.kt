package com.alfresco.content.data

import com.alfresco.content.apis.MobileConfigApi
import com.alfresco.content.session.ActionSessionInvalid
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import com.alfresco.content.session.SessionNotFoundException
import com.alfresco.events.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CommonRepository(otherSession: Session? = null) {

    lateinit var session: Session
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        try {
            session = otherSession ?: SessionManager.requireSession
        } catch (e: SessionNotFoundException) {
            e.printStackTrace()
            coroutineScope.launch {
                EventBus.default.send(ActionSessionInvalid(true))
            }
        }
    }

    private val context get() = session.context

    private val service: MobileConfigApi by lazy {
        session.createService(MobileConfigApi::class.java)
    }

    suspend fun getMobileConfigData() = MobileConfigDataEntry.with(service.getMobileConfig("https://run.mocky.io/v3/7b75235d-381a-4cdb-9eed-2eec246f38f4"))
}
