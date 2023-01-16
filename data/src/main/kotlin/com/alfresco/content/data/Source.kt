package com.alfresco.content.data

import com.alfresco.content.models.Node

/**
 * Mark as Source class
 */
data class Source(val name: String) {

    /**
     * returns obj as Source type.
     */
    companion object {
        fun with(node: Node?): Source {
            return Source(name = node?.name ?: "")
        }
    }
}
