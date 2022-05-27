package com.alfresco.content.apis

import com.alfresco.content.models.RequestDefaults
import com.alfresco.content.models.RequestFacetField
import com.alfresco.content.models.RequestFacetFields
import com.alfresco.content.models.RequestFacetIntervals
import com.alfresco.content.models.RequestFacetIntervalsInIntervals
import com.alfresco.content.models.RequestFacetQueriesInner
import com.alfresco.content.models.RequestFilterQueries
import com.alfresco.content.models.RequestFilterQueriesInner
import com.alfresco.content.models.RequestIncludeEnum
import com.alfresco.content.models.RequestPagination
import com.alfresco.content.models.RequestQuery
import com.alfresco.content.models.RequestSortDefinitionInner
import com.alfresco.content.models.RequestTemplatesInner
import com.alfresco.content.models.ResultSetPaging
import com.alfresco.content.models.SearchRequest

enum class SearchInclude(val value: String) {
    Files("cm:content"),
    Folders("cm:folder")
}

/**
 * Mark as AdvanceSearchInclude class
 */
data class AdvanceSearchInclude(val query: String, val name: String)

/**
 * Mark as FacetSearchInclude for facets data
 */
data class FacetSearchInclude(
    val fields: List<RequestFacetField>?,
    val queries: List<RequestFacetQueriesInner>?,
    val intervals: List<RequestFacetIntervalsInIntervals>?
)

/**
 * Searches for files and folders using the current recommended way.
 */
suspend fun SearchApi.simpleSearch(
    query: String,
    parentId: String?,
    skipCount: Int,
    maxItems: Int,
    include: Set<SearchInclude>
): ResultSetPaging {
    val reqQuery = RequestQuery(
        "$query*",
        RequestQuery.LanguageEnum.AFTS
    )

    val nameKeywords = "keywords"

    val templates =
        listOf(
            RequestTemplatesInner(
                nameKeywords,
                "%(cm:name cm:title cm:description TEXT TAG)"
            )
        )

    val defaults = RequestDefaults(
        defaultFieldName = nameKeywords,
        defaultFTSOperator = RequestDefaults.DefaultFTSOperatorEnum.AND
    )

    val typeFilter = (if (include.isEmpty()) {
        setOf(SearchInclude.Files, SearchInclude.Folders)
    } else {
        include
    }).joinToString(separator = " OR ") { "+TYPE:'${it.value}'" }

    val filter =
        (makeFilterQueries(typeFilter) + excludeUnsupportedTypes()).toMutableList()

    if (parentId != null) {
        filter.add(RequestFilterQueriesInner("ANCESTOR:'workspace://SpacesStore/$parentId'"))
    }

    val reqInclude = listOf(RequestIncludeEnum.PATH)
    val sort = listOf(
        RequestSortDefinitionInner(
            RequestSortDefinitionInner.TypeEnum.FIELD,
            "score",
            false
        )
    )
    val paging = RequestPagination(maxItems, skipCount)

    return search(
        SearchRequest(
            reqQuery,
            paging,
            reqInclude,
            sort = sort,
            templates = templates,
            defaults = defaults,
            filterQueries = filter
        )
    )
}

/**
 * Advance Search using the filters .
 */
suspend fun SearchApi.advanceSearch(
    query: String,
    parentId: String?,
    skipCount: Int,
    maxItems: Int,
    include: Set<AdvanceSearchInclude>,
    faceData: FacetSearchInclude,
    facetFormat: String
): ResultSetPaging {
    val reqQuery = RequestQuery(
        "$query*",
        RequestQuery.LanguageEnum.AFTS
    )

    val templates =
        listOf(
            RequestTemplatesInner(
                "keywords",
                "%(cm:name cm:title cm:description TEXT TAG)"
            )
        )

    val defaults = RequestDefaults(
        defaultFieldName = "keywords",
        defaultFTSOperator = RequestDefaults.DefaultFTSOperatorEnum.AND
    )

    val typeFilter: String = if (include.isEmpty()) {
        setOf(SearchInclude.Files, SearchInclude.Folders)
            .joinToString(separator = " OR ") { "+TYPE:'$it'" }
    } else {
        include.joinToString(separator = " AND ") { "(${it.query})" }
    }

    val filter =
        (makeFilterQueries(typeFilter) + excludeUnsupportedTypes()).toMutableList()

    if (parentId != null) {
        filter.add(RequestFilterQueriesInner("ANCESTOR:'workspace://SpacesStore/$parentId'"))
    }

    val reqInclude = listOf(RequestIncludeEnum.PATH)
    val sort = listOf(
        RequestSortDefinitionInner(
            RequestSortDefinitionInner.TypeEnum.FIELD,
            "score",
            false
        )
    )
    val paging = RequestPagination(maxItems, skipCount)

    return search(
        SearchRequest(
            reqQuery,
            paging,
            reqInclude,
            sort = sort,
            templates = templates,
            defaults = defaults,
            filterQueries = filter,
            facetFields = RequestFacetFields(faceData.fields),
            facetQueries = faceData.queries,
            facetIntervals = RequestFacetIntervals(intervals = faceData.intervals),
            facetFormat = facetFormat
        )
    )
}

/**
 * Returns recently modified files of [userId].
 */
suspend fun SearchApi.recentFiles(
    userId: String,
    days: Int = 30,
    skipCount: Int,
    maxItems: Int
): ResultSetPaging {
    val query = RequestQuery("*", RequestQuery.LanguageEnum.AFTS)
    val filter = makeFilterQueries(
        "cm:modified:[NOW/DAY-${days}DAYS TO NOW/DAY+1DAY]",
        "cm:modifier:$userId OR cm:creator:$userId",
        "TYPE:'content'"
    ) + excludeUnsupportedTypes()
    val include = listOf(RequestIncludeEnum.PATH)
    val sort = listOf(
        RequestSortDefinitionInner(
            RequestSortDefinitionInner.TypeEnum.FIELD,
            "cm:modified",
            false
        )
    )
    val paging = RequestPagination(maxItems, skipCount)

    return search(
        SearchRequest(
            query,
            paging,
            include,
            sort = sort,
            filterQueries = filter
        )
    )
}

internal fun SearchApi.excludeUnsupportedTypes() =
    makeFilterQueries(
        "-TYPE:'st:site'",
        "-TYPE:'cm:thumbnail' AND -TYPE:'cm:failedThumbnail' AND -TYPE:'cm:rating'",
        "-ASPECT:'st:siteContainer' AND -ASPECT:'sys:hidden'",
        "-TYPE:'dl:dataList' AND -TYPE:'dl:todoList'",
        "-TYPE:'dl:issue' AND -TYPE:'dl:task' AND -TYPE:'dl:simpletask'",
        "-TYPE:'dl:event' AND -TYPE:'dl:eventAgenda' AND -TYPE:'dl:meetingAgenda'",
        "-TYPE:'dl:contact' AND -TYPE:'dl:location'",
        "-TYPE:'fm:forum' AND -TYPE:'fm:topic' AND -TYPE:'fm:post'",
        "-TYPE:'app:filelink' AND -TYPE:'lnk:link' AND -TYPE:'ia:calendarEvent'",
        "-QNAME:comment AND -PNAME:'0/wiki'"
    )

fun SearchApi.makeFilterQueries(vararg filters: String): RequestFilterQueries {
    return filters.map { RequestFilterQueriesInner(it) }
}
