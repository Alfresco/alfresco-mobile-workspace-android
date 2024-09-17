package com.alfresco.content.data

import com.alfresco.content.apis.MobileConfigApi
import com.alfresco.content.session.Session
import com.alfresco.content.session.SessionManager
import java.net.URL

class CommonRepository(val session: Session = SessionManager.requireSession) {

    private val service: MobileConfigApi by lazy {
        session.createService(MobileConfigApi::class.java)
    }

    suspend fun getMobileConfigData() {
        val data = MobileConfigDataEntry.with(service.getMobileConfig("https://${URL(session.account.serverUrl).host}/app-config.json"))

        saveJsonToSharedPrefs(session.context, KEY_FEATURES_MOBILE, data)
    }

    companion object {
        const val KEY_FEATURES_MOBILE = "features_mobile"
    }
}
