package com.alfresco.content.common

import java.net.URLDecoder

class SharedURLParser {

    /**
     * isPreview (Boolean)
     * String
     * isRemoteFolder (Boolean)
     */
    fun getEntryIdFromShareURL(url: String, isHyperLink: Boolean = false): Triple<Boolean, String, Boolean> {
        val extData = URLDecoder.decode(url, "UTF-8")

        if (!isHyperLink && !extData.contains(SCHEME)) return Triple(false, "", false)

        if (extData.contains(IDENTIFIER_PREVIEW)) {
            return Triple(
                true,
                extData.substringAfter(SCHEME),
                false,
            )
        }

        if (!extData.contains(IDENTIFIER_PERSONAL_FILES)) return Triple(false, "", false)

        return if (extData.contains(IDENTIFIER_VIEWER)) {
            Triple(
                false,
                extData.substringAfter(IDENTIFIER_VIEWER).substringBefore(DELIMITER_BRACKET),
                false,
            )
        } else {
            Triple(
                false,
                extData.substringAfter(IDENTIFIER_PERSONAL_FILES)
                    .substringBefore(DELIMITER_FORWARD_SLASH),
                true,
            )
        }
    }

    companion object {
        const val ID_KEY = "id"
        const val MODE_KEY = "mode"
        const val VALUE_REMOTE = "remote"
        const val VALUE_SHARE = "share"
        const val KEY_FOLDER = "folder"
        const val SCHEME = "androidamw:///"
        const val IDENTIFIER_PREVIEW = "/preview"
        const val IDENTIFIER_VIEWER = "viewer:view/"
        const val IDENTIFIER_PERSONAL_FILES = "/personal-files/"
        const val DELIMITER_BRACKET = ")"
        const val DELIMITER_FORWARD_SLASH = "/"
    }
}
