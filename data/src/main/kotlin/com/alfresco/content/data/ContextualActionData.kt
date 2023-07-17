package com.alfresco.content.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContextualActionData(
    val entries: List<Entry> = emptyList(),
    val isMultiSelection: Boolean = false,
) : Parcelable {
    companion object {

        fun withEntries(entries: List<Entry>, isMultiSelection: Boolean = false): ContextualActionData {
            return ContextualActionData(
                entries = entries,
                isMultiSelection = isMultiSelection,
            )
        }
    }
}
