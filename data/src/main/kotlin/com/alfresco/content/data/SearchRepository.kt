package com.alfresco.content.data

import com.alfresco.content.apis.SearchApi
import com.alfresco.content.models.RequestFilterQueriesInner
import com.alfresco.content.models.RequestIncludeEnum
import com.alfresco.content.models.RequestQuery
import com.alfresco.content.models.RequestSortDefinitionInner
import com.alfresco.content.models.ResultNode
import com.alfresco.content.models.SearchRequest
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchRepository() {

    private val service: SearchApi by lazy {
        SessionManager.requireSession.createService(SearchApi::class.java)
    }

    suspend fun search(query: String): List<ResultNode> {
        val queryString = "((cm:name:\"$query*\" OR cm:title:\"$query*\" OR cm:description:\"$query*\" OR TEXT:\"$query*\" OR TAG:\"$query*\"))"
        val reqQuery = RequestQuery(queryString, RequestQuery.LanguageEnum.AFTS)
        val filter = listOf(RequestFilterQueriesInner("+TYPE:'cm:folder' OR +TYPE:'cm:content'"))
        val include = listOf(RequestIncludeEnum.PATH)
        val req = SearchRequest(reqQuery, filterQueries = filter, include = include)

        return service.search(req).list?.entries?.map { it.entry } ?: emptyList()
    }

    private suspend fun recents(): List<Entry> {
        val queryString = "*"
        val currentId = SessionManager.requireSession.account.id

        val reqQuery = RequestQuery(queryString, RequestQuery.LanguageEnum.AFTS)
        val filter = listOf(
            RequestFilterQueriesInner("cm:modified:[NOW/DAY-30DAYS TO NOW/DAY+1DAY]"),
            RequestFilterQueriesInner("cm:modifier:$currentId OR cm:creator:$currentId"),
            RequestFilterQueriesInner("TYPE:'cm:content' OR TYPE:'cm:folder'")
        )
        val include = listOf(RequestIncludeEnum.PATH)
        val sort = listOf(RequestSortDefinitionInner(RequestSortDefinitionInner.TypeEnum.FIELD, "cm:modified", false))
        val req = SearchRequest(reqQuery, sort = sort, filterQueries = filter, include = include)

        return service.search(req).list?.entries?.map { Entry.with(it.entry) } ?: emptyList()
    }

    suspend fun getRecents(): Flow<List<Entry>> {
        return flow {
            emit(recents())
        }
    }
}
