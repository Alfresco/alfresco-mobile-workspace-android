package com.alfresco.content.search

import com.alfresco.content.models.CategoriesItem

data class SearchChipCategory(
    var category: CategoriesItem,
    var isSelected: Boolean,
    var selectedName: String = "",
    var selectedQuery: String = ""
)
