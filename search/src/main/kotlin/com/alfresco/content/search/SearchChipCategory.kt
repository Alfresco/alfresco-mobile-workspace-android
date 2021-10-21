package com.alfresco.content.search

import android.os.Parcelable
import com.alfresco.content.models.CategoriesItem
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * SearchChipCategory type is  used for listSearchChips
 */
@Parcelize
data class SearchChipCategory(
    var category: @RawValue CategoriesItem,
    var isSelected: Boolean,
    var selectedName: String = "",
    var selectedQuery: String = ""
) : Parcelable
