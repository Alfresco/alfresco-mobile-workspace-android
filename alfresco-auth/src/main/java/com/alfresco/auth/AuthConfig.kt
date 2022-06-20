package com.alfresco.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Data class holding authentication configuration.
 */
@Serializable
data class AuthConfig(
    /**
     * Defines if the connection should be https or not
     */
    var https: Boolean,

    /**
     * The id of the client
     */
    var clientId: String,

    /**
     * The realm. Used to generate the final url.
     * The place of realm should be here https://identity-service/auth/realms/REALM/
     */
    var realm: String,

    /**
     * The redirect url
     */
    var redirectUrl: String,

    /**
     * Port for the network connection
     */
    var port: String,

    /**
     * Path to content service
     */
    var contentServicePath: String
) {
    /**
     * Convenience method for JSON serialization.
     */
    fun jsonSerialize(): String {
        return Json.encodeToString(serializer(), this)
    }

    companion object {
        /**
         * Convenience method for deserializing a JSON string representation.
         */
        @JvmStatic fun jsonDeserialize(str: String): AuthConfig? {
            return try {
                Json.decodeFromString(serializer(), str)
            } catch (ex: SerializationException) {
                null
            }
        }
    }
}
