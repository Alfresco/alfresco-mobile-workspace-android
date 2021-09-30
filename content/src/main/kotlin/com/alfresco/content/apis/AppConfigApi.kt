package com.alfresco.content.apis

import com.alfresco.content.models.AppConfigModel
import retrofit2.http.GET
import retrofit2.http.Headers

/**
 * AppConfigApi contains method to fetch config json
 */
@JvmSuppressWildcards
interface AppConfigApi {
    /**
     * get App config api to fetch the config json for advance search
     */
    @Headers(
        "Content-Type: application/json"
    )
    @GET("app.config.json")
    suspend fun getAppConfig(): AppConfigModel
}
