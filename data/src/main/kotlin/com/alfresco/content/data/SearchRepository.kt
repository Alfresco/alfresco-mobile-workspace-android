package com.alfresco.content.data

import com.alfresco.content.apis.SearchApi
import com.alfresco.content.models.RequestFilterQueriesInner
import com.alfresco.content.models.RequestIncludeEnum
import com.alfresco.content.models.RequestPagination
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
        val filter = listOf(
            RequestFilterQueriesInner("+TYPE:'cm:folder' OR +TYPE:'cm:content'"),
            RequestFilterQueriesInner("-TYPE:'cm:thumbnail' AND -TYPE:'cm:failedThumbnail' AND -TYPE:'cm:rating'"),
            RequestFilterQueriesInner("-cm:creator:System AND -QNAME:comment"),
            RequestFilterQueriesInner("-TYPE:'st:site' AND -ASPECT:'st:siteContainer' AND -ASPECT:'sys:hidden'"),
            RequestFilterQueriesInner("-TYPE:'dl:dataList' AND -TYPE:'dl:todoList' AND -TYPE:'dl:issue'"),
            RequestFilterQueriesInner("-TYPE:'fm:topic' AND -TYPE:'fm:post'"),
            RequestFilterQueriesInner("-TYPE:'lnk:link'"),
            RequestFilterQueriesInner("-PNAME:'0/wiki'")
        )
        val include = listOf(RequestIncludeEnum.PATH)
        val sort = listOf(RequestSortDefinitionInner(RequestSortDefinitionInner.TypeEnum.FIELD, "score", false))
        val req = SearchRequest(reqQuery, sort = sort, filterQueries = filter, include = include)

        return service.search(req).list?.entries?.map { it.entry } ?: emptyList()
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
        val include = listOf(RequestIncludeEnum.PATH)
        val sort = listOf(RequestSortDefinitionInner(RequestSortDefinitionInner.TypeEnum.FIELD, "cm:modified", false))
        val pagination = RequestPagination(maxItems, skipCount)
        val req = SearchRequest(reqQuery, sort = sort, filterQueries = filter, include = include, paging = pagination)

        return ResponsePaging.with(service.search(req))
    }

    suspend fun getRecents(skipCount: Int, maxItems: Int): Flow<ResponsePaging> {
        return flow {
            emit(recents(skipCount, maxItems))
        }
    }
}
