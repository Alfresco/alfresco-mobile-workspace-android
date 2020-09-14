package com.alfresco.content.app

import androidx.preference.Preference
import coil.Coil
import coil.ImageLoader
import coil.request.Disposable
import coil.request.ImageRequest

@JvmSynthetic
inline fun Preference.loadAny(
    data: Any?,
    imageLoader: ImageLoader = Coil.imageLoader(context),
    builder: ImageRequest.Builder.() -> Unit = {}
): Disposable {
    val request = ImageRequest.Builder(context)
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
    return imageLoader.enqueue(request)
}
