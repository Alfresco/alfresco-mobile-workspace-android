package com.alfresco.content.data.payloads

import com.alfresco.process.models.AuthConfiguration
import com.alfresco.process.models.SystemProperties

data class SystemPropertiesEntry(
    val allowInvolveByEmail: Boolean? = false,
    val disableJavaScriptEventsInFormEditor: Boolean? = false,
    val logoutDisabled: Boolean? = false,
    val alfrescoContentSsoEnabled: Boolean? = false,
    val authConfiguration: AuthConfigurationData? = null
) {
    companion object {
        fun with(data: SystemProperties?): SystemPropertiesEntry {
            return SystemPropertiesEntry(
                allowInvolveByEmail = data?.allowInvolveByEmail,
                disableJavaScriptEventsInFormEditor = data?.disableJavaScriptEventsInFormEditor,
                logoutDisabled = data?.logoutDisabled,
                alfrescoContentSsoEnabled = data?.alfrescoContentSsoEnabled,
                authConfiguration = AuthConfigurationData.with(data?.authConfiguration)
            )
        }
    }
}

data class AuthConfigurationData(
    val authUrl: String? = null,
    val realm: String? = null,
    val clientId: String? = null,
    val useBrowserLogout: Boolean? = false
) {
    companion object {
        fun with(data: AuthConfiguration?): AuthConfigurationData {
            return AuthConfigurationData(
                authUrl = data?.authUrl,
                realm = data?.realm,
                clientId = data?.clientId,
                useBrowserLogout = data?.useBrowserLogout
            )
        }
    }
}
