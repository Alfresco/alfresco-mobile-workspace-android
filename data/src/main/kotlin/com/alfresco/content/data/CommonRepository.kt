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
import java.net.URL

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

    suspend fun getMobileConfigData() {
        val data = MobileConfigDataEntry.with(service.getMobileConfig("https://${URL(session.account.serverUrl).host}/app-config.json"))

        saveJsonToSharedPrefs(context, KEY_FEATURES_MOBILE, data)
    }

    companion object {
        const val KEY_FEATURES_MOBILE = "features_mobile"
    }
}
