package com.alfresco.content.network

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

class NetworkErrorsTest {
    @Test
    fun `transport failures are network errors`() {
        assertTrue(UnknownHostException("acs.internal").isNetworkError())
        assertTrue(ConnectException("refused").isNetworkError())
        assertTrue(SocketTimeoutException("timeout").isNetworkError())
        assertTrue(SSLHandshakeException("handshake").isNetworkError())
    }

    @Test
    fun `wrapped transport failure is a network error`() {
        assertTrue(RuntimeException(UnknownHostException("acs.internal")).isNetworkError())
    }

    @Test
    fun `non transport failures are not network errors`() {
        assertFalse(IllegalStateException("bad state").isNetworkError())
        assertFalse(IOException("unexpected end of stream").isNetworkError())
    }
}
