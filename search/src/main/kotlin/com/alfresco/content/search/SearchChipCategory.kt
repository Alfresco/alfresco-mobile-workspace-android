package com.alfresco.content.search

import android.os.Parcelable
import com.alfresco.content.data.ChipModel
import kotlinx.parcelize.Parcelize

/**
 * SearchChipCategory type is  used for listSearchChips
 */
@Parcelize
data class SearchChipCategory(
    var category: ChipModel,
    var isSelected: Boolean,
    var selectedName: String = "",
    var selectedQuery: String = ""
) : Parcelable
