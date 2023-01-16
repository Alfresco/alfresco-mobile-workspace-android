package com.alfresco.content.data

import com.alfresco.content.models.Node

data class Source(val name: String) {
    companion object {
        fun with(node: Node?): Source {
            return Source(name = node?.name ?: "")
        }
    }
}
