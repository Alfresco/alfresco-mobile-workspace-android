package com.alfresco.auth

/**
 * Enum representing currently supported authentication types.
 */
enum class AuthType(val value: String) {

    /**
     * Used to specify the need of basic auth with username and password
     */
    BASIC("basic"),

    /**
     * Used to specify the need of SSO auth
     */
    PKCE("pkce"),

    /**
     * Used to specify that the auth type is unknown
     */
    UNKNOWN("");

    companion object {
        private val map = values().associateBy(AuthType::value)

        /**
         * Convert string representation to enum.
         */
        @JvmStatic fun fromValue(value: String) = map[value]
    }
}
