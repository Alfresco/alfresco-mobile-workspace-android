package com.alfresco.content.search

import android.os.Parcelable
import com.alfresco.content.models.CategoriesItem
import kotlinx.parcelize.Parcelize

/**
 * SearchChipCategory type is  used for listSearchChips
 */
@Parcelize
data class SearchChipCategory(
    var category: CategoriesItem,
    var isSelected: Boolean,
    var selectedName: String = "",
    var selectedQuery: String = ""
) : Parcelable {

    companion object {
        fun with(searchChipCategory: SearchChipCategory, name: String, query: String): SearchChipCategory {
            return SearchChipCategory(
                category = searchChipCategory.category,
                isSelected = searchChipCategory.isSelected,
                selectedName = name,
                selectedQuery = query
            )
        }
    }
}
