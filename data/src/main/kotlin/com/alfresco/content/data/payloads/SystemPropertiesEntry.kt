package com.alfresco.content.data.payloads

import com.alfresco.process.models.AuthConfiguration
import com.alfresco.process.models.SystemProperties

/**
 * Marked as SystemPropertiesEntry
 */
data class SystemPropertiesEntry(
    val allowInvolveByEmail: Boolean? = false,
    val disableJavaScriptEventsInFormEditor: Boolean? = false,
    val logoutDisabled: Boolean? = false,
    val alfrescoContentSsoEnabled: Boolean? = false,
    val authConfiguration: AuthConfigurationData? = null
) {
    companion object {
        /**
         * returns the SystemPropertiesEntry obj by using SystemProperties
         */
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

/**
 * Marked as AuthConfigurationData
 */
data class AuthConfigurationData(
    val authUrl: String? = null,
    val realm: String? = null,
    val clientId: String? = null,
    val useBrowserLogout: Boolean? = false
) {
    companion object {
        /**
         * returns theAuthConfigurationData obj by using AuthConfiguration
         */
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
