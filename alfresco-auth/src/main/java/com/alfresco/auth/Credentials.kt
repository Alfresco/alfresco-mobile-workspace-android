package com.alfresco.auth

/**
 * Data class holding authentication credentials.
 */
data class Credentials(
    /**
     * Username associated with credentials.
     */
    val username: String,

    /**
     * JSON representation of authentication state.
     */
    val authState: String,

    /**
     * String representation of authentication type.
     */
    val authType: String
)
