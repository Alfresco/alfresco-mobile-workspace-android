package com.alfresco.content.data

import android.content.Context
import com.alfresco.auth.AuthInterceptor
import com.alfresco.content.account.Account
import com.alfresco.content.apis.NodesApi
import com.alfresco.content.models.Node
import com.alfresco.content.tools.GeneratedCodeConverters
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class BrowseRepository(context: Context) {

    private val service: NodesApi by lazy {
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
        retrofit.create(NodesApi::class.java)
    }

    private suspend fun myFiles(): List<Node> {
        return service.listNodeChildren(
            "-my-",
            null,
            25,
            null,
            null,
            null,
            null,
            null,
            null).list?.entries?.map { with(it.entry) } ?: emptyList()
    }

    suspend fun getNodes(): Flow<List<Node>> {
        return flow {
            emit(myFiles())
        }
    }
}
