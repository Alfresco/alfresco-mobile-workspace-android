package com.alfresco.auth.config

import com.alfresco.auth.AuthConfig

val AuthConfig.Companion.defaultConfig: AuthConfig
    get() =
        AuthConfig(
            https = true,
            port = "443",
            clientId = "alfresco-android-acs-app",
            realm = "alfresco",
            scope = "openid profile email",
            redirectUrl = "androidacsapp://aims/auth",
            contentServicePath = "alfresco",
        )
