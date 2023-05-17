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
        fun with(node: Node?): Source {
            return if (node?.createdByUser?.id != "System")
                Source(name = node?.name)
            else Source()
        }
    }
}
