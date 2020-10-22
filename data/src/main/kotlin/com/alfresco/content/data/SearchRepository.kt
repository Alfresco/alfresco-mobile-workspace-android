package com.alfresco.content.data

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.alfresco.content.apis.QueriesApi
import com.alfresco.content.apis.SearchApi
import com.alfresco.content.models.RequestDefaults
import com.alfresco.content.models.RequestFilterQueriesInner
import com.alfresco.content.models.RequestIncludeEnum
import com.alfresco.content.models.RequestPagination
import com.alfresco.content.models.RequestQuery
import com.alfresco.content.models.RequestSortDefinitionInner
import com.alfresco.content.models.RequestTemplatesInner
import com.alfresco.content.models.SearchRequest
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchRepository() {

    private val context get() = SessionManager.requireSession.context

    private val searchService: SearchApi by lazy {
        SessionManager.requireSession.createService(SearchApi::class.java)
    }

    private val queryService: QueriesApi by lazy {
        SessionManager.requireSession.createService(QueriesApi::class.java)
    }

    suspend fun search(
        terms: String,
        nodeId: String?,
        filters: SearchFilters,
        skipCount: Int,
        maxItems: Int
    ): ResponsePaging {
        return if (filters.contains(SearchFilter.Libraries)) {
            siteSearch(terms, skipCount, maxItems)
        } else {
            fileSearch(
                terms,
                if (filters.contains(SearchFilter.Contextual)) nodeId else null,
                skipCount,
                maxItems,
                filters.contains(SearchFilter.Files),
                filters.contains(SearchFilter.Folders)
            )
        }
    }

    private suspend fun fileSearch(
        query: String,
        nodeId: String?,
        skipCount: Int,
        maxItems: Int,
        includeFiles: Boolean,
        includeFolders: Boolean
    ): ResponsePaging {
        val reqQuery = RequestQuery("$query*", RequestQuery.LanguageEnum.AFTS)
        val templates = listOf(RequestTemplatesInner("keywords", "%(cm:name cm:title cm:description TEXT TAG)"))
        val defaults = RequestDefaults(defaultFieldName = "keywords", defaultFTSOperator = RequestDefaults.DefaultFTSOperatorEnum.AND)
        val typeFilter = if (includeFiles) {
            if (includeFolders) "+TYPE:'cm:folder' OR +TYPE:'cm:content'" else "+TYPE:'cm:content'"
        } else {
            if (includeFolders) "+TYPE:'cm:folder'" else "+TYPE:'cm:folder' OR +TYPE:'cm:content'"
        }
        val filter = mutableListOf(
            RequestFilterQueriesInner(typeFilter),
            RequestFilterQueriesInner("-TYPE:'cm:thumbnail' AND -TYPE:'cm:failedThumbnail' AND -TYPE:'cm:rating'"),
            RequestFilterQueriesInner("-cm:creator:System AND -QNAME:comment"),
            RequestFilterQueriesInner("-TYPE:'st:site' AND -ASPECT:'st:siteContainer' AND -ASPECT:'sys:hidden'"),
            RequestFilterQueriesInner("-TYPE:'dl:dataList' AND -TYPE:'dl:todoList' AND -TYPE:'dl:issue'"),
            RequestFilterQueriesInner("-TYPE:'fm:forum' AND -TYPE:'fm:topic' AND -TYPE:'fm:post'"),
            RequestFilterQueriesInner("-TYPE:'lnk:link'"),
            RequestFilterQueriesInner("-PNAME:'0/wiki'")
        )

        if (nodeId != null) {
            filter.add(RequestFilterQueriesInner("ANCESTOR:'workspace://SpacesStore/$nodeId'"))
        }

        val include = listOf(RequestIncludeEnum.PATH, RequestIncludeEnum.ALLOWABLEOPERATIONS)
        val sort = listOf(RequestSortDefinitionInner(RequestSortDefinitionInner.TypeEnum.FIELD, "score", false))
        val pagination = RequestPagination(maxItems, skipCount)
        val req = SearchRequest(reqQuery, sort = sort, templates = templates, defaults = defaults, filterQueries = filter, include = include, paging = pagination)

        return ResponsePaging.with(searchService.search(req))
    }

    private suspend fun siteSearch(terms: String, skipCount: Int, maxItems: Int): ResponsePaging {
        return ResponsePaging.with(queryService.findSites(terms, skipCount, maxItems, null, null))
    }

    private suspend fun recents(skipCount: Int, maxItems: Int): ResponsePaging {
        val queryString = "*"
        val currentId = SessionManager.requireSession.account.id

        val reqQuery = RequestQuery(queryString, RequestQuery.LanguageEnum.AFTS)
        val filter = listOf(
            RequestFilterQueriesInner("cm:modified:[NOW/DAY-30DAYS TO NOW/DAY+1DAY]"),
            RequestFilterQueriesInner("cm:modifier:$currentId OR cm:creator:$currentId"),
            RequestFilterQueriesInner("TYPE:\"content\" AND -PNAME:\"0/wiki\" AND -TYPE:\"app:filelink\" AND -TYPE:\"cm:thumbnail\" AND -TYPE:\"cm:failedThumbnail\" AND -TYPE:\"cm:rating\" AND -TYPE:\"dl:dataList\" AND -TYPE:\"dl:todoList\" AND -TYPE:\"dl:issue\" AND -TYPE:\"dl:contact\" AND -TYPE:\"dl:eventAgenda\" AND -TYPE:\"dl:event\" AND -TYPE:\"dl:task\" AND -TYPE:\"dl:simpletask\" AND -TYPE:\"dl:meetingAgenda\" AND -TYPE:\"dl:location\" AND -TYPE:\"fm:topic\" AND -TYPE:\"fm:post\" AND -TYPE:\"ia:calendarEvent\" AND -TYPE:\"lnk:link\"")
        )
        val include = listOf(RequestIncludeEnum.PATH, RequestIncludeEnum.ALLOWABLEOPERATIONS)
        val sort = listOf(RequestSortDefinitionInner(RequestSortDefinitionInner.TypeEnum.FIELD, "cm:modified", false))
        val pagination = RequestPagination(maxItems, skipCount)
        val req = SearchRequest(reqQuery, sort = sort, filterQueries = filter, include = include, paging = pagination)

        return ResponsePaging.with(searchService.search(req))
    }

    suspend fun getRecents(skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return flow {
            emit(recents(skipCount, maxItems))
        }
    }

    fun getRecentSearches(): List<String> {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPrefs.getStringList(RECENT_SEARCH_KEY).toMutableList()
    }

    fun saveSearch(query: String) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        var list = sharedPrefs.getStringList(RECENT_SEARCH_KEY).toMutableList()

        // At most 15 distinct values, with the latest added to top
        list.remove(query)
        list.add(0, query)
        list = list.subList(0, minOf(list.count(), 15))

        val editor = sharedPrefs.edit()
        editor.putStringList(RECENT_SEARCH_KEY, list)
        editor.apply()
    }

    fun clearRecentSearch() {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPrefs.edit()
        editor.remove(RECENT_SEARCH_KEY)
        editor.apply()
    }

    class RecentSearchesChangeListener(
        val context: Context,
        val onChange: () -> Unit
    ) : SharedPreferences.OnSharedPreferenceChangeListener {

        init {
            PreferenceManager.getDefaultSharedPreferences(context)
                .registerOnSharedPreferenceChangeListener(this)
        }

        override fun onSharedPreferenceChanged(
            sharedPreferences: SharedPreferences?,
            key: String?
        ) {
            onChange()
        }
    }

    companion object {
        const val RECENT_SEARCH_KEY = "recent_searches"
    }
}
