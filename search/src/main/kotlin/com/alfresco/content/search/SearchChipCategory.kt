package com.alfresco.content.search

import android.os.Parcelable
import com.alfresco.content.data.FacetFields
import com.alfresco.content.data.FacetIntervals
import com.alfresco.content.data.FacetQueries
import com.alfresco.content.data.SearchFilter
import com.alfresco.content.models.CategoriesItem
import com.alfresco.content.models.Component
import kotlinx.parcelize.Parcelize

/**
 * SearchChipCategory type is  used for listSearchChips
 */
@Parcelize
data class SearchChipCategory(
    var category: CategoriesItem? = null,
    var queriesItem: FacetQueries? = null,
    var fieldsItem: FacetFields? = null,
    var intervalsItem: FacetIntervals? = null,
    var isSelected: Boolean = false,
    var selectedName: String = "",
    var selectedQuery: String = ""
) : Parcelable {

    companion object {

        /**
         * update and returns the searchChipCategory
         */
        fun with(searchChipCategory: SearchChipCategory, name: String, query: String): SearchChipCategory {
            return SearchChipCategory(
                category = searchChipCategory.category,
                isSelected = searchChipCategory.isSelected,
                selectedName = name,
                selectedQuery = query
            )
        }

        /**
         * returns the contextual searchChipCategory object
         */
        fun withContextual(name: String, contextual: SearchFilter): SearchChipCategory {
            return SearchChipCategory(
                category = CategoriesItem(
                    name = name, expanded = null,
                    component = null, enabled = null,
                    id = contextual.toString()
                ),
                isSelected = true,
                selectedName = name,
                selectedQuery = contextual.name
            )
        }

        /**
         * returns the default searchChipCategory object
         */
        fun resetData(searchChipCategory: SearchChipCategory): SearchChipCategory {
            return SearchChipCategory(
                category = searchChipCategory.category,
                isSelected = searchChipCategory.category?.component == null,
                selectedName = "",
                selectedQuery = ""
            )
        }

        /**
         * return the SearchChipCategory obj using FacetQueries data obj
         */
        fun withDefaultFacet(data: FacetQueries): SearchChipCategory {
            return SearchChipCategory(
                category = CategoriesItem(
                    null, Component(null, ChipComponentType.FACET_QUERIES.component),
                    null, null, null
                ),
                queriesItem = data,
                selectedName = "",
                selectedQuery = ""
            )
        }

        /**
         * return the SearchChipCategory obj using FacetFields data obj
         */
        fun withDefaultFacet(data: FacetFields): SearchChipCategory {
            return SearchChipCategory(
                category = CategoriesItem(
                    null, Component(null, ChipComponentType.FACET_FIELDS.component),
                    null, null, null
                ),
                fieldsItem = data,
                selectedName = "",
                selectedQuery = ""
            )
        }

        /**
         * return the SearchChipCategory obj using FacetIntervals data obj
         */
        fun withDefaultFacet(data: FacetIntervals): SearchChipCategory {
            return SearchChipCategory(
                category = CategoriesItem(
                    null, Component(null, ChipComponentType.FACET_INTERVALS.component),
                    null, null, null
                ),
                intervalsItem = data,
                selectedName = "",
                selectedQuery = ""
            )
        }
    }
}
