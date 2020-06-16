package com.alfresco.content.app

import androidx.preference.Preference
import coil.Coil
import coil.ImageLoader
import coil.request.LoadRequest
import coil.request.LoadRequestBuilder
import coil.request.RequestDisposable

@JvmSynthetic
inline fun Preference.loadAny(
    data: Any?,
    imageLoader: ImageLoader = Coil.imageLoader(context),
    builder: LoadRequestBuilder.() -> Unit = {}
): RequestDisposable {
    val request = LoadRequest.Builder(context)
        .data(data)
        .target(
            onStart = { placeholder ->
                icon = placeholder
            },
            onSuccess = { result ->
                icon = result
            },
            onError = { error ->
                icon = error
            }
        )
        .apply(builder)
        .build()
    return imageLoader.execute(request)
}
