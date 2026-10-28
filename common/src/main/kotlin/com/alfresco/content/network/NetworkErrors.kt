package com.alfresco.content.network

import java.io.InterruptedIOException
import java.net.SocketException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

/**
 * Returns true when this error (or any of its causes) means the server could not be reached.
 */
fun Throwable.isNetworkError(): Boolean = generateSequence(this) { it.cause }.any { it.isTransportFailure() }

private fun Throwable.isTransportFailure(): Boolean =
    this is UnknownHostException ||
        this is SocketException ||
        this is InterruptedIOException ||
        this is SSLException
