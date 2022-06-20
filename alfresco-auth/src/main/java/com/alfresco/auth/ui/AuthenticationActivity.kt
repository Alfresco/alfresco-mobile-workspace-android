package com.alfresco.auth.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alfresco.auth.AuthConfig
import com.alfresco.auth.AuthInterceptor
import com.alfresco.auth.AuthType
import com.alfresco.auth.Credentials
import com.alfresco.auth.DiscoveryService
import com.alfresco.auth.data.LiveEvent
import com.alfresco.auth.data.MutableLiveEvent
import com.alfresco.auth.pkce.PkceAuthService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthState

/**
 * Companion [ViewModel] to [AuthenticationActivity] which facilitates authentication.
 */
abstract class AuthenticationViewModel : ViewModel() {

    protected abstract var context: Context
    protected lateinit var discoveryService: DiscoveryService

    protected val _onPkceLogin = MutableLiveEvent<String>()
    protected val _onCredentials = MutableLiveEvent<Credentials>()
    protected val _onError = MutableLiveEvent<String>()

    val onPkceLogin: LiveEvent<String> get() = _onPkceLogin
    val onCredentials: LiveEvent<Credentials> get() = _onCredentials
    val onError: LiveEvent<String> get() = _onError

    /**
     * true if the session expired or was invalidated and user has to re-login.
     */
    var isReLogin = false

    /**
     * Check which [AuthType] is supported by the [endpoint] based on the provided [authConfig].
     */
    fun checkAuthType(
        endpoint: String,
        authConfig: AuthConfig,
        onResult: (authType: AuthType) -> Unit
    ) = viewModelScope.launch {
        discoveryService = DiscoveryService(context, authConfig)
        val authType = withContext(Dispatchers.IO) { discoveryService.getAuthType(endpoint) }
        onResult(authType)
    }

    /**
     * Function takes [username] and [password] and returns result via [onCredentials].
     * Note: This function does not provide any credential validation.
     */
    fun basicLogin(username: String, password: String) {
        val state = AuthInterceptor.basicState(username, password)
        _onCredentials.value = Credentials(username, state, AuthType.BASIC.value)
    }

    /**
     * Initiate PKCE login, against [endpoint] with provided [authConfig].
     * If an [authState] is provided the invocation is considered a re-login.
     * On success credentials are provided via [onCredentials].
     * On error [onError] is invoked instead.
     * If the flow is cancelled by the user [onPkceAuthCancelled] is called.
     */
    fun pkceLogin(endpoint: String, authConfig: AuthConfig, authState: String? = null) {
        if (authState != null) {
            isReLogin = true
        }

        pkceAuth.initServiceWith(authConfig, authState)

        _onPkceLogin.value = endpoint
    }

    /**
     * Called if the user cancels the login by dismissing the webView.
     */
    open fun onPkceAuthCancelled() {}

    internal val pkceAuth = PkceAuth()
    internal inner class PkceAuth {
        private lateinit var authService: PkceAuthService

        /**
         * Recreates [authService] with provided [AuthConfig]
         * @throws [IllegalArgumentException]
         */
        fun initServiceWith(authConfig: AuthConfig, authState: String? = null) {
            var state: AuthState? = null
            if (authState != null) {
                state = AuthState.jsonDeserialize(authState)
            }

            authService = PkceAuthService(context, state, authConfig)
        }

        fun login(endpoint: String, activity: Activity, requestCode: Int) {
            viewModelScope.launch {
                try {
                    authService.initiateLogin(endpoint, activity, requestCode)
                } catch (ex: Exception) {
                    _onError.value = ex.message
                }
            }
        }

        fun reLogin(activity: Activity, requestCode: Int) {
            authService.initiateReLogin(activity, requestCode)
        }

        fun handleActivityResult(intent: Intent) {
            viewModelScope.launch {
                try {
                    val result = authService.getAuthResponse(intent)
                    val userEmail = authService.getUserEmail() ?: ""
                    _onCredentials.value = Credentials(userEmail, result, AuthType.PKCE.value)
                } catch (ex: Exception) {
                    _onError.value = ex.message ?: ""
                }
            }
        }
    }
}

/**
 * Abstract activity used to build the authentication flow.
 */
abstract class AuthenticationActivity<T : AuthenticationViewModel> : AppCompatActivity() {

    protected abstract val viewModel: T

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        observe(viewModel.onPkceLogin, ::onPkceLogin)
        observe(viewModel.onCredentials, ::onCredentials)
        observe(viewModel.onError, ::onError)
    }

    protected fun onPkceLogin(endpoint: String) {
        if (viewModel.isReLogin) {
            viewModel.pkceAuth.reLogin(this, REQUEST_CODE_AUTHENTICATE)
        } else {
            viewModel.pkceAuth.login(endpoint, this, REQUEST_CODE_AUTHENTICATE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_AUTHENTICATE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                viewModel.onPkceAuthCancelled()
            } else {
                data?.let { viewModel.pkceAuth.handleActivityResult(it) }
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    /**
     * Called when [credentials] become available.
     */
    abstract fun onCredentials(credentials: Credentials)

    /**
     * Called on [error] during the authentication process.
     */
    abstract fun onError(error: String)

    private companion object {
        const val REQUEST_CODE_AUTHENTICATE = 20
    }
}
