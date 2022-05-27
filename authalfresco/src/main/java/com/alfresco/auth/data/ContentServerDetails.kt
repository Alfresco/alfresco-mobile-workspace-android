package com.alfresco.auth.data

import java.lang.NumberFormatException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
data class ContentServerDetailsData(
    val edition: String,
    val version: String,
    val schema: String
)

@Serializable
internal data class ContentServerDetails(
    val data: ContentServerDetailsData
) {
    /**
     * Verify current [data] version, is at lest [minVersion].
     * Extra build information is ignored, e.g. 6.2.0 (b120)
     */
    fun isAtLeast(minVersion: String): Boolean {
        val minParts = minVersion.split(" ")[0].split(".")
        val verParts = data.version.split(" ")[0].split(".")
        try {
            for ((index, value) in minParts.withIndex()) {
                val part = if (index > verParts.size - 1) 0 else verParts[index].toInt()
                val min = value.toInt()
                if (part > min) {
                    return true
                } else if (part < min) {
                    return false
                }
            }
        } catch (_: NumberFormatException) {
            return false
        }
        return true
    }

    companion object {
        fun jsonDeserialize(str: String): ContentServerDetails? {
            return try {
                Json.decodeFromString(serializer(), str)
            } catch (ex: SerializationException) {
                null
            }
        }
    }
}
