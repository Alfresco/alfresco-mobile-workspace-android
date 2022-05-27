package com.alfresco.content.apis

class AlfrescoApi {

    companion object {
        /**
         * Alias for the current logged in user.
         */
        const val CURRENT_USER = "-me-"

        /**
         * Returns correct comma-separated query parameter list to pass to requests.
         * Due to a retrofit issue, lists in query parameters will be passed as individual values.
         * This method facilitates the conversion until the issue is fixed.
         */
        fun csvQueryParam(vararg fields: String) =
            listOf(fields.joinToString(","))
    }
}
