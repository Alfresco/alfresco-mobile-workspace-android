package com.alfresco.dbp.sample

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.alfresco.auth.pkce.PkceAuthConfig
import com.alfresco.auth.pkce.PkceAuthService
import com.alfresco.dbp.sample.common.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : BaseViewModel() {

    private val pkceAuthService = PkceAuthService()

    /**
     * Private mutable implementation that should be used inside the viewmodel
     * as opposed to [authResult] which is the one that is being observed from the outside
     */
    private val _authResult = MutableLiveData<PkceAuthUiModel>()

    /**
     * Used to send the rates
     */
    val authResult: LiveData<PkceAuthUiModel> get() = _authResult

    /**
     * Private mutable implementation that should be used inside the viewmodel
     * as opposed to [refreshResult] which is the one that is being observed from the outside
     */
    private val _refreshResult = MutableLiveData<PkceRefreshUiModel>()

    /**
     * Used to send the rates
     */
    val refreshResult: LiveData<PkceRefreshUiModel> get() = _refreshResult


    /**
     * Private mutable implementation that should be used inside the viewmodel
     * as opposed to [token] which is the one that is being observed from the outside
     */
    private val _pkceAuthConfig = MutableLiveData<PkceAuthConfig>()

    /**
     * Used to send the rates
     */
    val pkceAuthConfig: LiveData<PkceAuthConfig> get() = _pkceAuthConfig

    private var defaultPkceAuthConfig = PkceAuthConfig(
            https = false,
            clientId = "iosapp",
            redirectUrl = "iosapp://fake.url.here/auth",
            realm = "alfresco",
            issuerUrl = "http://alfresco-identity-service.mobile.dev.alfresco.me")

    init {
        _pkceAuthConfig.value = defaultPkceAuthConfig
    }

    fun changeSettings(realm: String, clientId: String, redirectUrl: String) {
        defaultPkceAuthConfig = defaultPkceAuthConfig.copy(
                realm = realm, clientId = clientId, redirectUrl = redirectUrl)

        _pkceAuthConfig.value = defaultPkceAuthConfig
    }

    fun initiateLogin(issuerUrl: String, activity: Activity, requestCode: Int) {
        _isLoading.value = true
        defaultPkceAuthConfig = defaultPkceAuthConfig.copy(issuerUrl = issuerUrl)

        viewModelScope.launch(Dispatchers.Main) {
            try {
                pkceAuthService.initiateLogin(activity, requestCode, defaultPkceAuthConfig)
            } catch (exception: Exception) {
                _isLoading.value = false
                _authResult.value = PkceAuthUiModel(false, error = exception.message)
            }
        }
    }

    fun handleResult(intent: Intent) {
        viewModelScope.launch {
            val tokenResult = pkceAuthService.getAuthResponse(intent)

            tokenResult.onSuccess { _authResult.value = PkceAuthUiModel(true, it.accessToken) }

            tokenResult.onError { _authResult.value = PkceAuthUiModel(false, error = it.message) }
        }
    }

    fun refreshToken() {
        viewModelScope.launch {
            val refreshResult = pkceAuthService.refreshToken()

            refreshResult.onSuccess { _refreshResult.value = PkceRefreshUiModel(true, it.accessToken) }

            refreshResult.onError { _refreshResult.value = PkceRefreshUiModel(false, error = it.message) }
        }
    }

    fun signOut() {
        pkceAuthService.signOut()
    }
}

/**
 * Ui model for the result of pkce auth action
 */
data class PkceAuthUiModel(
        val success: Boolean,
        val accessToken: String? = null,
        val error: String? = null
)

/**
 * Ui model for the result of refresh token action
 */
data class PkceRefreshUiModel(
        val success: Boolean,
        val refreshToken: String? = null,
        val error: String? = null
)
