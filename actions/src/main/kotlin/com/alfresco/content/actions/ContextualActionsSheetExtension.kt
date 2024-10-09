package com.alfresco.content.actions

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updateLayoutParams
import com.alfresco.content.data.Entry
import com.alfresco.content.mimetype.MimeType

fun ContextualActionsSheet.setHeader(state: ContextualActionsState) {
    if (state.isMultiSelection) {
        val titleHeader = SpannableString(getString(R.string.title_action_mode, state.entries.size))
        titleHeader.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.colorActionMode)),
            0,
            titleHeader.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
        )
        binding.header.apply {
            parentTitle.contentDescription = titleHeader
            icon.visibility = View.GONE
            title.text = titleHeader
            title.updateLayoutParams<LinearLayout.LayoutParams> {
                marginStart = 0
            }
        }
    } else {
        val entryObj = state.entries.first()
        entryObj.let { entry ->
            val type =
                when (entry.type) {
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
