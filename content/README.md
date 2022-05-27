# Content Services API

The **content** module provides API bindings to be used with [Alfresco Content Services](https://api-explorer.alfresco.com/)

The module includes bindings for both **search** and **core** to make it easier to integrate in most applications.

Our bindings provide an API layer based on [Retrofit](https://square.github.io/retrofit/) and [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html)

Since we [previously](../auth#authentication-interceptor) mentioned the `AuthInterceptor` being an **OkHttp** interceptor and **Retrofit** is based on OkHttp, we'll show you how to easily integrate the two to get you started with authenticated requests to Alfresco.

3 steps are required to get Retrofit configured:
* create a AuthInterceptor
* create a OkHttp client and attach the AuthInterceptor to it
* create a Retrofit instance with the OkHttp client

``` kotlin
val authInterceptor = AuthInterceptor(
    context,
    username,
    authType,
    authState,
    authConfig
)

val okHttpClient: OkHttpClient = OkHttpClient()
    .newBuilder()
    .addInterceptor(authInterceptor)
    .build()

val retrofit = Retrofit.Builder()
    .client(okHttpClient)
    .addConverterFactory(GeneratedCodeConverters.converterFactory())
    .baseUrl(baseUrl)
    .build()
```

Now just get a **service** and a **coroutine context** and make your request:

```kotlin
val service = retrofit.create(SearchApi::class.java)
val search = SearchRequest("my query", ...)

lifecycleScope.launch {
    try {
        val results = service.search(search)
    } catch (ex: Exception) {
       // some error
    }
}
```

For more information on the APIs just read our [documentation](https://api-explorer.alfresco.com/)
