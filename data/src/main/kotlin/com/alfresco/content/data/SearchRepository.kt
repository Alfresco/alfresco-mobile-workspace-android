package com.alfresco.content.data

import android.content.Context
import com.alfresco.auth.AuthInterceptor
import com.alfresco.content.account.Account
import com.alfresco.content.apis.SearchApi
import com.alfresco.content.models.RequestFilterQueries
import com.alfresco.content.models.RequestFilterQueriesInner
import com.alfresco.content.models.RequestQuery
import com.alfresco.content.models.ResultSetRowEntry
import com.alfresco.content.models.SearchRequest
import com.alfresco.content.tools.GeneratedCodeConverters
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class SearchRepository(context: Context) {

    private val service: SearchApi by lazy {
        val acc = Account.getAccount(context)!!
        val authInterceptor = AuthInterceptor(context, acc.username, acc.authType, acc.authState, acc.authConfig)
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient: OkHttpClient = OkHttpClient()
            .newBuilder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .addConverterFactory(GeneratedCodeConverters.converterFactory())
            .baseUrl("${acc.serverUrl}/api/-default-/public/")
            .build()
        retrofit.create(SearchApi::class.java)
    }

    suspend fun search(query: String): List<ResultSetRowEntry> {
        val queryString = "((cm:name:\"$query*\" OR cm:title:\"$query*\" OR cm:description:\"$query*\" OR TEXT:\"$query*\" OR TAG:\"$query*\"))"
        val reqQuery = RequestQuery(queryString, RequestQuery.LanguageEnum.AFTS)
        val filter = listOf(RequestFilterQueriesInner("+TYPE:'cm:folder' OR +TYPE:'cm:content'"))
        val req = SearchRequest(reqQuery, filterQueries = filter)

        return service.search(req).list?.entries!!
    }
}
