package com.alfresco.auth

import android.content.Context
import android.util.Base64
import com.alfresco.auth.pkce.PkceAuthService
import java.lang.Exception
import java.lang.ref.WeakReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AuthState
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException

/**
 * OkHttp [Interceptor] which deals with managing session information and attaching authentication headers.
 */
class AuthInterceptor(
    private val context: Context,
    private val accountId: String,
    typeString: String,
    stateString: String,
    config: String
) : Interceptor {

    private val localScope = CoroutineScope(Dispatchers.IO)
    private var listener: Listener? = null
    private val authType = AuthType.fromValue(typeString)
    private val provider: Provider

    init {
        requireNotNull(authType)

        provider = when (authType) {
            AuthType.BASIC -> BasicProvider(stateString)
            AuthType.PKCE -> PkceProvider(stateString, config)
            AuthType.UNKNOWN -> PlainProvider()
        }
    }

    /**
     * Associates event [listener] with current object.
     */
    fun setListener(listener: Listener) {
        this.listener = listener
    }

    /**
     * Call to cleanup any running tasks before the object is GCed to avoid any leaks.
     */
    fun finish() {
        this.provider.finish()
    }

    override fun intercept(chain: Interceptor.Chain): Response = provider.intercept(chain)

    private interface Provider {

        fun intercept(chain: Interceptor.Chain): Response

        fun finish()
    }

    private inner class PlainProvider : Provider {

        override fun intercept(chain: Interceptor.Chain): Response {
            return chain.proceed(chain.request())
        }

        override fun finish() {
            // no-op
        }
    }

    private inner class BasicProvider(private var credentials: String) : Provider {

        override fun intercept(chain: Interceptor.Chain): Response {
            val response = chain.proceed(AuthType.BASIC, credentials)

            // When unauthorized notify of failure
            if (response.code == HTTP_RESPONSE_401_UNAUTHORIZED) {
                listener?.onAuthFailure(accountId)
            }

            return response
        }

        override fun finish() {
            // no-op
        }
    }

    private inner class PkceProvider(stateString: String, configString: String) : Provider {

        private val pkceAuthService: PkceAuthService
        private var lastRefresh = 0L
        private var scheduledRefreshJob: Job? = null
        private var expirationTime: Long = 0L

        init {
            val state = try { AuthState.jsonDeserialize(stateString) } catch (ex: JSONException) { null }
            val config = AuthConfig.jsonDeserialize(configString)

            requireNotNull(state)
            requireNotNull(config)

            pkceAuthService = PkceAuthService(context, state, config)
        }

        @Synchronized override fun finish() {
            cancelScheduledTokenRefresh()
            localScope.coroutineContext.cancelChildren()
        }

        override fun intercept(chain: Interceptor.Chain): Response {
            var state = pkceAuthService.getAuthState() ?: return chain.proceed(chain.request())

            // Preemptive token refresh when close to expiration
            refreshTokenIfNeeded(state)?.let { state = it }

            var response = chain.proceed(AuthType.PKCE, state.accessToken)

            // When unauthorized try to refresh
            if (response.code == HTTP_RESPONSE_401_UNAUTHORIZED) {
                val newState = refreshTokenNow()

                if (newState != null) {
                    response.close()
                    response = chain.proceed(AuthType.PKCE, newState.accessToken)
                }
            }

            // If still error notify listener of failure
            if (response.code == HTTP_RESPONSE_401_UNAUTHORIZED) {
                listener?.onAuthFailure(accountId)
            }

            return response
        }

        @Synchronized private fun refreshTokenNow(): AuthState? {
            val state = pkceAuthService.getAuthState() ?: return null

            // Another thread might've refreshed the token already
            val current = System.currentTimeMillis()
            if (current - lastRefresh < REFRESH_THROTTLE_DELAY) {
                return state
            }

            cancelScheduledTokenRefresh()
            lastRefresh = current

            val result = runBlocking { runTokenRefresh() }

            if (result != null) {
                scheduleTokenRefresh(result)
            }

            return result
        }

        @Synchronized private fun refreshTokenIfNeeded(state: AuthState): AuthState? {
            val expiration = state.accessTokenExpirationTime ?: return null
            val delta = expiration - System.currentTimeMillis()

            if (delta < REFRESH_DELTA_BEFORE_EXPIRY) {
                return refreshTokenNow()
            } else {
                scheduleTokenRefresh(state)
            }

            return null
        }

        @Synchronized fun scheduleTokenRefresh(state: AuthState) {
            val expiration = state.accessTokenExpirationTime ?: return

            val delta = expiration - System.currentTimeMillis() - REFRESH_DELTA_BEFORE_EXPIRY
            if (delta < 0) return
            if (expirationTime != expiration) {
                expirationTime = expiration

                cancelScheduledTokenRefresh()
                val weakThis = WeakReference(this)
                scheduledRefreshJob = localScope.launch {
                    delay(delta)
                    if (isActive) {
                        weakThis.get()?.refreshTokenNow()
                    }
                }
            }
        }

        private fun cancelScheduledTokenRefresh() {
            scheduledRefreshJob?.cancel()
            scheduledRefreshJob = null
        }

        private suspend fun runTokenRefresh(): AuthState? {
            return try {
                pkceAuthService.refreshToken()
                val state = pkceAuthService.getAuthState()
                state?.jsonSerializeString()?.let {
                    listener?.onAuthStateChange(accountId, it)
                }
                return state
            } catch (ex: Exception) {
                null
            }
        }
    }

    private fun Interceptor.Chain.proceed(type: AuthType, token: String?): Response {
        val headerValue = when (type) {
            AuthType.BASIC -> "Basic $token"
//            AuthType.PKCE -> "Bearer "
            AuthType.PKCE -> "Bearer $token"
            AuthType.UNKNOWN -> null
        }
        return proceedWithAuthorization(headerValue)
    }

    private fun Interceptor.Chain.proceedWithAuthorization(value: String?): Response {
        val request = if (value != null) request().newBuilder().addAuthorization(value).build() else request()
        return proceed(request)
    }

    private fun Request.Builder.addAuthorization(credentials: String) =
        this.apply { removeHeader("Authorization") }
            .apply { header("Authorization", credentials) }

    companion object {
        private const val HTTP_RESPONSE_401_UNAUTHORIZED = 401
        private const val REFRESH_THROTTLE_DELAY = 15000L
        private const val REFRESH_DELTA_BEFORE_EXPIRY = 20000L

        /**
         * Returns compatible state representation for basic authorization
         */
        @JvmStatic fun basicState(username: String, password: String): String {
            val credentials = "$username:$password"
            return Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        }

        /**
         * Returns a <username, password> [Pair] from provided basic [state]
         * Please try to avoid using this function if possible.
         */
        @JvmStatic fun decodeBasicState(state: String): Pair<String, String>? {
            return try {
                val decoded = String(Base64.decode(state, Base64.NO_WRAP))
                val split = decoded.split(":")
                Pair(split[0], split[1])
            } catch (_: Exception) {
                null
            }
        }
    }

    /**
     * Interface definition for a callback to be invoked on authentication events.
     */
    interface Listener {
        /**
         * Called when [authState] changes during a refresh.
         */
        fun onAuthStateChange(accountId: String, authState: String)

        /**
         * Called when a non-recoverable authentication failure occurs.
         */
        fun onAuthFailure(accountId: String)
    }
}
