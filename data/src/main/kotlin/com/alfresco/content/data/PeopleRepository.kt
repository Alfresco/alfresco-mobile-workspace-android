package com.alfresco.content.data

import android.content.Context
import android.net.Uri
import com.alfresco.auth.AuthInterceptor
import com.alfresco.content.account.Account
import com.alfresco.content.apis.PeopleApi
import com.alfresco.content.models.Person
import com.alfresco.content.session.SessionManager
import com.alfresco.content.tools.GeneratedCodeConverters
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

class PeopleRepository(context: Context, account: Account) {
    private val service: PeopleApi by lazy {
        val authInterceptor = AuthInterceptor(context, account.id, account.authType, account.authState, account.authConfig)
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
            .baseUrl("${account.serverUrl}/api/-default-/public/")
            .build()
        retrofit.create(PeopleApi::class.java)
    }

    suspend fun me(): Person {
        return service.getPerson("-me-", null).entry
    }

    companion object {
        fun myPicture(context: Context): Uri {
            return Uri.parse(SessionManager.currentSession?.baseUrl + "alfresco/versions/1/people/-me-/avatar")
        }
    }
}
