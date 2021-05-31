package com.alfresco.download

import com.alfresco.Logger
import java.io.File
import java.io.IOException
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.buffer
import okio.sink

object ContentDownloader {

    const val FILE_PROVIDER_AUTHORITY = "com.alfresco.content.fileprovider"

    suspend fun downloadFileTo(uri: String, outputPath: String) {
        Logger.d("Downloading: $uri to: $outputPath")
        val req = Request.Builder().get().url(uri).build()
        val client = OkHttpClient()
        client.newCall(req).downloadAndSaveTo(File(outputPath))
    }

    suspend fun downloadFile(uri: String, outputPath: String): Flow<String> {
        return flow {
            downloadFileTo(uri, outputPath)
            emit(outputPath)
        }
    }
}

/**
 * Custom coroutine dispatcher for blocking calls
 */
val OK_IO = newFixedThreadPoolContext(5, "OK_IO")

/**
 * Invokes OkHttp Call and saves successful result to [output]
 *
 * Warning: Dispatcher in [blockingDispatcher] executes blocking calls
 * [progress] callback returns downloaded bytes and total bytes, but total not always available
 */
suspend fun Call.downloadAndSaveTo(
    output: File,
    bufferSize: Long = DEFAULT_BUFFER_SIZE.toLong(),
    blockingDispatcher: CoroutineDispatcher = OK_IO,
    progress: ((downloaded: Long, total: Long) -> Unit)? = null
): File = withContext(blockingDispatcher) {
    suspendCancellableCoroutine { cont ->
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    cont.resumeWithException(IOException("Unexpected HTTP code: ${response.code}"))
                    return
                }
                try {
                    val body = response.body
                    if (body == null) {
                        cont.resumeWithException(IllegalStateException("Body is null"))
                        return
                    }
                    val contentLength = body.contentLength()
                    var finished = false
                    output.sink().buffer().use { out ->
                        body.source().use { source ->
                            val buffer = out.buffer
                            var totalLength = 0L
                            while (cont.isActive) {
                                val read = source.read(buffer, bufferSize)
                                if (read == -1L) {
                                    finished = true
                                    break
                                }
                                out.emit()
                                totalLength += read
                                progress?.invoke(totalLength, contentLength)
                            }
                            out.flush()
                        }
                    }
                    if (finished) {
                        cont.resume(output) {
                            cancel()
                        }
                    } else {
                        cont.resumeWithException(IOException("Download cancelled"))
                    }
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            }
        })
    }
}
