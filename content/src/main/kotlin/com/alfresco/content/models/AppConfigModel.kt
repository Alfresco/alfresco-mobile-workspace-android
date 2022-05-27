package com.alfresco.content.models

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

/**
 * Advance search model
 * @property search
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class AppConfigModel(
    @Json(name = "search") @field:Json(name = "search") val search: List<SearchItem>?
) : Parcelable

/**
 * Categories model
 * @property expanded
 * @property component
 * @property name
 * @property id
 * @property enabled
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class CategoriesItem(
    @Json(name = "expanded") @field:Json(name = "expanded") val expanded: Boolean?,
    @Json(name = "component") @field:Json(name = "component") val component: Component?,
    @Json(name = "name") @field:Json(name = "name") val name: String?,
    @Json(name = "id") @field:Json(name = "id") val id: String?,
    @Json(name = "enabled") @field:Json(name = "enabled") val enabled: Boolean?
) : Parcelable

/**
 * Component model
 * @property settings
 * @property selector
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class Component(
    @Json(name = "settings") @field:Json(name = "settings") val settings: Settings?,
    @Json(name = "selector") @field:Json(name = "selector") val selector: String?
) : Parcelable

/**
 * SearchItem model
 * @property default
 * @property name
 * @property filterWithContains
 * @property categories
 * @property resetButton
 * @property facetFields
 * @property facetQueries
 * @property facetIntervals
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class SearchItem(
    @Json(name = "default") @field:Json(name = "default") val default: Boolean?,
    @Json(name = "name") @field:Json(name = "name") val name: String?,
    @Json(name = "filterWithContains") @field:Json(name = "filterWithContains") val filterWithContains: Boolean?,
    @Json(name = "categories") @field:Json(name = "categories") val categories: List<CategoriesItem>?,
    @Json(name = "resetButton") @field:Json(name = "resetButton") val resetButton: Boolean?,
    @Json(name = "filterQueries") @field:Json(name = "filterQueries") val filterQueries: List<FilterQueriesItem>?,
    @Json(name = "facetFields") @field:Json(name = "facetFields") val facetFields: FacetFieldsItem?,
    @Json(name = "facetQueries") @field:Json(name = "facetQueries") val facetQueries: FacetQueriesItem?,
    @Json(name = "facetIntervals") @field:Json(name = "facetIntervals") val facetIntervals: FacetIntervalsItem?
) : Parcelable

/**
 * Filter Queries Model
 * @property query
 * @property fields
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class FilterQueriesItem(
    @Json(name = "query") @field:Json(name = "query") val query: String?
) : Parcelable

/**
 * Facet Fields Model
 * @property expanded
 * @property fields
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class FacetFieldsItem(
    @Json(name = "expanded") @field:Json(name = "expanded") val expanded: Boolean?,
    @Json(name = "fields") @field:Json(name = "fields") val fields: List<FieldsItem>?
) : Parcelable

/**
 * Facet Queries Model
 * @property label
 * @property pageSize
 * @property expanded
 * @property mincount
 * @property queries
 * @property settings
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class FacetQueriesItem(
    @Json(name = "label") @field:Json(name = "label") val label: String?,
    @Json(name = "pageSize") @field:Json(name = "pageSize") val pageSize: Int?,
    @Json(name = "expanded") @field:Json(name = "expanded") val expanded: Boolean?,
    @Json(name = "mincount") @field:Json(name = "mincount") val mincount: Int?,
    @Json(name = "queries") @field:Json(name = "queries") val queries: List<QueriesItem>?,
    @Json(name = "settings") @field:Json(name = "settings") val settings: Settings?
) : Parcelable

/**
 * Facet Fields Model
 * @property expanded
 * @property intervals
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class FacetIntervalsItem(
    @Json(name = "expanded") @field:Json(name = "expanded") val expanded: Boolean?,
    @Json(name = "intervals") @field:Json(name = "intervals") val intervals: List<IntervalsItem>?
) : Parcelable

/**
 * Fields Model
 * @property field
 * @property mincount
 * @property label
 * @property settings
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class FieldsItem(
    @Json(name = "field") @field:Json(name = "field") val field: String?,
    @Json(name = "mincount") @field:Json(name = "mincount") val mincount: Int?,
    @Json(name = "label") @field:Json(name = "label") val label: String?,
    @Json(name = "settings") @field:Json(name = "settings") val settings: Settings?
) : Parcelable

/**
 * Queries Model
 * @property query
 * @property label
 * @property group
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class QueriesItem(
    @Json(name = "query") @field:Json(name = "query") val query: String?,
    @Json(name = "label") @field:Json(name = "label") val label: String?,
    @Json(name = "group") @field:Json(name = "group") val group: String?
) : Parcelable

/**
 * Queries Model
 * @property label
 * @property field
 * @property sets
 * @property settings
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class IntervalsItem(
    @Json(name = "label") @field:Json(name = "label") val label: String?,
    @Json(name = "field") @field:Json(name = "field") val field: String?,
    @Json(name = "sets") @field:Json(name = "sets") val sets: List<SetsItem>?,
    @Json(name = "settings") @field:Json(name = "settings") val settings: Settings?
) : Parcelable

/**
 * Queries Model
 * @property label
 * @property start
 * @property end
 * @property endInclusive
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class SetsItem(
    @Json(name = "label") @field:Json(name = "label") val label: String?,
    @Json(name = "start") @field:Json(name = "start") val start: String?,
    @Json(name = "end") @field:Json(name = "end") val end: String?,
    @Json(name = "startInclusive") @field:Json(name = "startInclusive") val startInclusive: Boolean?,
    @Json(name = "endInclusive") @field:Json(name = "endInclusive") val endInclusive: Boolean?
) : Parcelable

/**
 * Settings model
 * @property field
 * @property pattern
 * @property placeholder
 * @property pageSize
 * @property operator
 * @property options
 * @property min
 * @property max
 * @property step
 * @property thumbLabel
 * @property format
 * @property dateFormat
 * @property maxDate
 * @property allowUpdateOnChange
 * @property hideDefaultAction
 * @property unit
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class Settings(
    @Json(name = "field") @field:Json(name = "field") val field: String?,
    @Json(name = "pattern") @field:Json(name = "pattern") val pattern: String?,
    @Json(name = "placeholder") @field:Json(name = "placeholder") val placeholder: String?,
    @Json(name = "pageSize") @field:Json(name = "pageSize") val pageSize: Int?,
    @Json(name = "operator") @field:Json(name = "operator") val operator: String?,
    @Json(name = "options") @field:Json(name = "options") val options: List<Options>?,
    @Json(name = "min") @field:Json(name = "min") val min: Int?,
    @Json(name = "max") @field:Json(name = "max") val max: Int?,
    @Json(name = "step") @field:Json(name = "step") val step: Int?,
    @Json(name = "thumbLabel") @field:Json(name = "thumbLabel") val thumbLabel: Boolean?,
    @Json(name = "format") @field:Json(name = "format") val format: String?,
    @Json(name = "dateFormat") @field:Json(name = "dateFormat") val dateFormat: String?,
    @Json(name = "maxDate") @field:Json(name = "maxDate") val maxDate: String?,
    @Json(name = "allowUpdateOnChange") @field:Json(name = "allowUpdateOnChange") val allowUpdateOnChange: Boolean?,
    @Json(name = "hideDefaultAction") @field:Json(name = "hideDefaultAction") val hideDefaultAction: Boolean?,
    @Json(name = "unit") @field:Json(name = "unit") val unit: String?
) : Parcelable

/**
 * Options model
 * @property name
 * @property value
 * @property default
 */
@Parcelize
@JsonClass(generateAdapter = true)
data class Options(
    @Json(name = "name") @field:Json(name = "name") val name: String?,
    @Json(name = "value") @field:Json(name = "value") val value: String?,
    @Json(name = "default") @field:Json(name = "default") val default: Boolean?
) : Parcelable
