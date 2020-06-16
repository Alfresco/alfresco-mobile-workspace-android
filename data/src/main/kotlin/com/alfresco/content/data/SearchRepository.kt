package com.alfresco.content.data

import android.content.Context
import com.alfresco.content.apis.SearchApi
import com.alfresco.content.models.Node
import com.alfresco.content.models.RequestFilterQueriesInner
import com.alfresco.content.models.RequestIncludeEnum
import com.alfresco.content.models.RequestQuery
import com.alfresco.content.models.ResultNode
import com.alfresco.content.models.SearchRequest
import com.alfresco.content.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchRepository(context: Context) {

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

    private suspend fun recents(): List<Node> {
        val queryString = "cm:modified:[NOW/DAY-30DAYS TO NOW/DAY+1DAY]"

        val reqQuery = RequestQuery(queryString, RequestQuery.LanguageEnum.AFTS)
        val filter = listOf(RequestFilterQueriesInner("+TYPE:'cm:folder' OR +TYPE:'cm:content'"))
        val include = listOf(RequestIncludeEnum.PATH)
        val req = SearchRequest(reqQuery, filterQueries = filter, include = include)

        return service.search(req).list?.entries?.map { with(it.entry) } ?: emptyList()
    }

    suspend fun getRecents(): Flow<List<Node>> {
        return flow {
            emit(recents())
        }
    }
}
