package com.alfresco.content.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ContextualActionData(
    val entries: List<Entry> = emptyList(),
    val isMultiSelection: Boolean = false,
    val mobileConfigData: MobileConfigDataEntry? = null,
) : Parcelable {
    companion object {

        fun withEntries(entries: List<Entry>, isMultiSelection: Boolean = false, mobileConfigData: MobileConfigDataEntry? = null): ContextualActionData {
            return ContextualActionData(
                entries = entries,
                isMultiSelection = isMultiSelection,
                mobileConfigData = mobileConfigData
            )
        }
    }
}
