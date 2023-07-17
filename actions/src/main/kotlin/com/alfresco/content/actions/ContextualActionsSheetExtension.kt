package com.alfresco.content.actions

import androidx.core.content.res.ResourcesCompat
import com.alfresco.content.data.Entry
import com.alfresco.content.mimetype.MimeType

fun ContextualActionsSheet.setHeader(state: ContextualActionsState) {
    if (state.isMultiSelection) {
    } else {
        val entryObj = state.entries.first()
        entryObj.let { entry ->
            val type = when (entry.type) {
                Entry.Type.SITE -> MimeType.LIBRARY
                Entry.Type.FOLDER -> MimeType.FOLDER
                else -> MimeType.with(entry.mimeType)
            }

            binding.header.apply {
                parentTitle.contentDescription = getString(R.string.accessibility_text_title_type, entry.name, type.name)
                icon.setImageDrawable(ResourcesCompat.getDrawable(resources, type.icon, context?.theme))
                title.text = entry.name
            }
        }
    }
}
