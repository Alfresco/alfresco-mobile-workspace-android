package com.alfresco.content.search

import android.os.Parcelable
import com.alfresco.content.data.Facets
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
    var facets: Facets? = null,
    var isSelected: Boolean = false,
    var selectedName: String = "",
    var selectedQuery: String = ""
) : Parcelable {

    companion object {

        /**
         * update and returns the searchChipCategory
         */
        fun with(searchChipCategory: SearchChipCategory?, name: String, query: String): SearchChipCategory {
            return SearchChipCategory(
                category = searchChipCategory?.category,
                isSelected = searchChipCategory?.isSelected == true,
                facets = searchChipCategory?.facets,
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
                facets = searchChipCategory.facets,
                isSelected = searchChipCategory.category?.component == null,
                selectedName = "",
                selectedQuery = ""
            )
        }

        /**
         * return the SearchChipCategory obj using FacetIntervals data obj
         */
        fun withDefaultFacet(data: Facets): SearchChipCategory {
            return SearchChipCategory(
                category = CategoriesItem(
                    null, Component(null, ChipComponentType.FACETS.component),
                    data.label, data.label, null
                ),
                facets = data,
                selectedName = "",
                selectedQuery = ""
            )
        }

        /**
         * return the update SearchChipCategory obj using FacetIntervals data obj
         */
        fun updateFacet(oldDataObj: SearchChipCategory, data: Facets): SearchChipCategory {
            return SearchChipCategory(
                category = oldDataObj.category,
                facets = data,
                selectedName = oldDataObj.selectedName,
                selectedQuery = oldDataObj.selectedQuery,
                isSelected = oldDataObj.isSelected
            )
        }
        /**
         * return the update SearchChipCategory obj using FacetIntervals data obj
         */
        fun withFilterCountZero(data: Facets): SearchChipCategory {
            return SearchChipCategory(
                category = CategoriesItem(
                    null, Component(null, ChipComponentType.FACETS.component),
                    data.label, data.label, null
                ),
                facets = Facets.filterZeroCount(data),
                selectedName = "",
                selectedQuery = ""
            )
        }
    }
}
