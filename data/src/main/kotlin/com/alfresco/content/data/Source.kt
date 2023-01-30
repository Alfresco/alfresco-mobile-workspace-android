package com.alfresco.content.data

import com.alfresco.content.models.Node

/**
 * Mark as Source class
 */
data class Source(val name: String? = null) {
    companion object {
        /**
         * returns obj as Source type.
         */
        fun with(node: Node?, email: String?): Source {
            return if (!email.equals(node?.name))
                Source(name = node?.name)
            else Source()
        }
    }
}
