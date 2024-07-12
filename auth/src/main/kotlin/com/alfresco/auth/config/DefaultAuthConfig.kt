package com.alfresco.auth.config

import com.alfresco.auth.AuthConfig
import com.alfresco.auth.AuthType

val AuthConfig.Companion.defaultKeycloakConfig: AuthConfig
    get() = AuthConfig(
        authType = AuthType.PKCE.value,
        https = true,
        port = "443",
        clientId = "alfresco-android-acs-app",
        realm = "alfresco",
        redirectUrl = "androidacsapp://aims/auth",
        contentServicePath = "alfresco",
        scheme = "",
    )
val AuthConfig.Companion.defaultAuth0Config: AuthConfig
    get() = AuthConfig(
        authType = AuthType.OIDC.value,
        https = true,
        port = "",
        clientId = "",
        realm = "",
        redirectUrl = "",
        contentServicePath = "",
        scheme = "demo",
    )
