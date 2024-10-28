package com.alfresco.auth.activity

import android.content.Context
import android.os.Bundle
import com.alfresco.auth.AuthConfig
import com.alfresco.auth.AuthType
import com.alfresco.auth.ui.EndSessionActivity
import com.alfresco.auth.ui.EndSessionViewModel
import com.alfresco.common.getViewModel
import org.json.JSONException

class LogoutViewModel(context: Context, authType: AuthType?, authState: String, authConfig: AuthConfig, serverURL: String, clientId: String) :
    EndSessionViewModel(context, authType, authState, authConfig, serverURL, clientId) {
    companion object {
        const val EXTRA_HOST_NAME = "host_name"
        const val EXTRA_CLIENT_ID = "client_id"
        const val EXTRA_AUTH_TYPE = "authType"
        const val EXTRA_AUTH_STATE = "authState"
        const val EXTRA_AUTH_CONFIG = "authConfig"

        fun with(
            context: Context,
            bundle: Bundle?,
        ): LogoutViewModel {
            requireNotNull(bundle)

            val stateString = bundle.getString(EXTRA_AUTH_STATE)
            val configString = bundle.getString(EXTRA_AUTH_CONFIG)
            val hostName = bundle.getString(EXTRA_HOST_NAME)
            val clientId = bundle.getString(EXTRA_CLIENT_ID)
            val authType = bundle.getString(EXTRA_AUTH_TYPE)?.let { AuthType.fromValue(it) }

            val config =
                try {
                    if (configString != null) {
                        AuthConfig.jsonDeserialize(configString)
                    } else {
                        null
                    }
                } catch (ex: JSONException) {
                    ex.printStackTrace()
                null
            }

            requireNotNull(stateString)
            requireNotNull(hostName)
            requireNotNull(clientId)
            requireNotNull(config)

            return LogoutViewModel(context, authType, stateString, config, hostName, clientId)
        }
    }
}

class LogoutActivity : EndSessionActivity<LogoutViewModel>() {
    override val viewModel: LogoutViewModel by lazy {
        getViewModel {
            LogoutViewModel.with(applicationContext, intent.extras)
        }
    }
}
