package com.alfresco.auth.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.alfresco.android.aims.R
import com.alfresco.auth.AuthConfig
import com.alfresco.auth.AuthType
import com.alfresco.auth.config.defaultConfig
import com.alfresco.auth.data.AppConfigDetails
import com.alfresco.auth.data.LiveEvent
import com.alfresco.auth.data.MutableLiveEvent
import com.alfresco.auth.ui.AuthenticationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(
    private val applicationContext: Context,
    authType: AuthType?,
    authState: String?,
    authConfig: AuthConfig?,
    endpoint: String?,
    val isExtension: Boolean,
) : AuthenticationViewModel() {
    lateinit var authConfig: AuthConfig
    override var context = applicationContext

    private val _hasNavigation = MutableLiveData<Boolean>()
    private val _step = MutableLiveData<Step>()
    private val _onShowHelp = MutableLiveEvent<Int>()
    private val _onShowSettings = MutableLiveEvent<Int>()
    private val _onSaveSettings = MutableLiveEvent<Int>()

    val hasNavigation: LiveData<Boolean> get() = _hasNavigation
    val step: LiveData<Step> get() = _step
    val onShowHelp: LiveEvent<Int> = _onShowHelp
    val onShowSettings: LiveEvent<Int> = _onShowSettings
    val onSaveSettings: LiveEvent<Int> = _onSaveSettings
    val isLoading = MutableLiveData<Boolean>()
    val identityUrl = MutableLiveData("")
    val applicationUrl = MutableLiveData("")

    val connectEnabled: LiveData<Boolean>
    val ssoLoginEnabled: LiveData<Boolean>

    lateinit var authConfigEditor: AuthConfigEditor
        private set

    private val previousAppEndpoint: String? = endpoint
    private val previousAuthState: String? = authState

    val canonicalApplicationUrl: String
        get() {
            return previousAppEndpoint
                ?: discoveryService.contentServiceUrl(applicationUrl.value!!).toString()
        }

    // Used for display purposes
    val applicationUrlHost: String
        get() {
            return Uri.parse(canonicalApplicationUrl).host ?: ""
        }

    init {
        if (previousAuthState != null) {
            isReLogin = true

            if (authType == AuthType.PKCE) {
                moveToStep(Step.EnterPkceCredentials)
            } else {
                moveToStep(Step.EnterBasicCredentials)
            }
        } else {
            moveToStep(Step.InputIdentityServer)
        }

        if (authConfig != null) {
            this.authConfig = authConfig
        } else {
            loadSavedConfig()
        }

        connectEnabled = identityUrl.map { it.isNotBlank() }
        ssoLoginEnabled = applicationUrl.map { it.isNotBlank() }
    }

    fun setHasNavigation(enableNavigation: Boolean) {
        _hasNavigation.value = enableNavigation
    }

    fun startEditing() {
        authConfigEditor = AuthConfigEditor()
        authConfigEditor.reset(authConfig)
    }

    fun connect() {
        isLoading.value = true
        loadSavedConfig()

        try {
            checkAuthType(identityUrl.value!!, authConfig, ::onAuthType)
        } catch (ex: Exception) {
            _onError.value = ex.message
        }
    }

    private fun onAuthType(
        authType: AuthType,
        appConfigDetails: AppConfigDetails?,
    ) {

        var mobileSettings = appConfigDetails?.mobileSettings

        when {
            mobileSettings != null -> {

                var additionalParams = mutableMapOf<String, String>()
                if (mobileSettings.audience?.isNotEmpty() == true) {
                    val key = mobileSettings::audience.name
                    val value = mobileSettings.audience

                    additionalParams[key] = value!!
                }

                authConfig = AuthConfig(
                    https = mobileSettings.https == true,
                    port = mobileSettings.port?.toString() ?: "",
                    host = mobileSettings.host,
                    contentServicePath = mobileSettings.contentServicePath ?: "",
                    realm = mobileSettings.realm ?: "",
                    clientId = mobileSettings.android.clientId,
                    redirectUrl = mobileSettings.android.redirectUri,
                    scope = mobileSettings.scope,
                    secret = mobileSettings.secret ?: "",
                    additionalParams = additionalParams,
                )
            }
        }

        when (authType) {
            AuthType.PKCE -> {
                viewModelScope.launch {

                    val isContentServicesInstalled =
                        withContext(Dispatchers.IO) {
                            discoveryService.isContentServiceInstalled(identityUrl.value ?: "")
                        }
                    if (isContentServicesInstalled) {
                        applicationUrl.value = identityUrl.value
                        moveToStep(Step.EnterPkceCredentials)
                    } else {
                        moveToStep(Step.InputAppServer)
                    }
                }
            }

            AuthType.BASIC -> {
                moveToStep(Step.EnterBasicCredentials)
            }

            AuthType.UNKNOWN -> {
                _onError.value = context.getString(R.string.auth_error_check_connect_url)
            }
        }
    }

    fun ssoLogin() {
        isLoading.value = true

        val endpoint = requireNotNull(identityUrl.value)
        pkceLogin(endpoint, authConfig, previousAuthState)
    }

    override fun onPkceAuthCancelled() {
        if (isReLogin) {
            moveToStep(Step.Cancelled)
        } else {
            isLoading.value = false
        }
    }

    fun showSettings() {
        _onShowSettings.value = 0
    }

    fun showWelcomeHelp() {
        _onShowHelp.value = R.string.auth_help_identity_body
    }

    fun showSettingsHelp() {
        _onShowHelp.value = R.string.auth_help_settings_body
    }

    fun showSsoHelp() {
        _onShowHelp.value = R.string.auth_help_sso_body
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    private fun loadSavedConfig() {
        val sharedPrefs = applicationContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val configJson = sharedPrefs.getString(SHARED_PREFS_CONFIG_KEY, null) ?: ""

        authConfig = AuthConfig.jsonDeserialize(configJson) ?: AuthConfig.defaultConfig
    }

    fun saveConfigChanges() {
        val config = authConfigEditor.get()
        config.scope = authConfig.scope

        // Save state to persistent storage
        val sharedPrefs = applicationContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        val editor = sharedPrefs.edit()
        editor.putString(SHARED_PREFS_CONFIG_KEY, config.jsonSerialize())
        editor.apply()

        // Update local field
        authConfig = config

        // Reset the editor (will update changed state)
        authConfigEditor.reset(config)

        _onSaveSettings.value = 0
    }

    private fun moveToStep(step: Step) {
        this.isLoading.value = false

        when (step) {
            Step.InputIdentityServer -> {
            }

            Step.InputAppServer -> {
                applicationUrl.value = ""
            }

            Step.EnterBasicCredentials -> {
                // Assume application url is the same as identity for basic auth
                applicationUrl.value = identityUrl.value
            }

            Step.EnterPkceCredentials -> {
            }

            Step.Cancelled -> {
            }
        }

        _step.value = step
    }

    enum class Step {
        InputIdentityServer,
        InputAppServer,
        EnterBasicCredentials,
        EnterPkceCredentials,
        Cancelled,
    }

    val basicAuth = BasicAuth()

    inner class BasicAuth {
        private val _enabled = MediatorLiveData<Boolean>()

        val email = MutableLiveData<String>()
        val password = MutableLiveData<String>()
        val enabled: LiveData<Boolean> get() = _enabled

        init {
            _enabled.addSource(email, this::onFieldChange)
            _enabled.addSource(password, this::onFieldChange)
        }

        private fun onFieldChange(
            @Suppress("UNUSED_PARAMETER") value: String,
        ) {
            _enabled.value = !email.value.isNullOrEmpty() && !password.value.isNullOrEmpty()
        }

        fun login() {
            isLoading.value = true
            basicLogin(email.value ?: "", password.value ?: "")
        }
    }

    companion object {
        private const val SHARED_PREFS_NAME = "org.activiti.aims.android.auth"
        private const val SHARED_PREFS_CONFIG_KEY = "config"

        const val EXTRA_IS_LOGIN = "is_login"
        const val EXTRA_IS_EXTENSION = "is_extension"
        const val EXTRA_ENDPOINT = "endpoint"
        const val EXTRA_AUTH_TYPE = "authType"
        const val EXTRA_AUTH_STATE = "authState"
        const val EXTRA_AUTH_CONFIG = "authConfig"

        fun with(
            context: Context,
            intent: Intent,
        ): LoginViewModel {
            var config: AuthConfig? = null
            var stateString: String? = null
            var authType: AuthType? = null
            var endpoint: String? = null
            var isExtension = false

            val extras = intent.extras
            if (extras != null) {
                config =
                    try {
                        AuthConfig.jsonDeserialize(extras.getString(EXTRA_AUTH_CONFIG)!!)
                    } catch (ex: Exception) {
                        null
                    }

                stateString = extras.getString(EXTRA_AUTH_STATE)
                endpoint = extras.getString(EXTRA_ENDPOINT)
                isExtension = extras.getBoolean(EXTRA_IS_EXTENSION)
                authType = extras.getString(EXTRA_AUTH_TYPE)?.let { AuthType.fromValue(it) }
            }

            return LoginViewModel(context, authType, stateString, config, endpoint, isExtension)
        }
    }

    class AuthConfigEditor {
        private lateinit var source: AuthConfig
        private val _changed = MediatorLiveData<Boolean>()

        val https = MutableLiveData<Boolean>()
        val port = MutableLiveData<String>()
        val contentServicePath = MutableLiveData<String>()
        val realm = MutableLiveData<String>()
        val clientId = MutableLiveData<String>()
        private var redirectUrl: String = ""

        val changed: LiveData<Boolean> get() = _changed

        init {
            _changed.addSource(https, this::onChange)
            _changed.addSource(port, this::onChange)
            _changed.addSource(contentServicePath, this::onChange)
            _changed.addSource(realm, this::onChange)
            _changed.addSource(clientId, this::onChange)
        }

        /**
         * This function is meant to change the port when the user interacts with it.
         *
         * It is important that this function is bound [android.view.View.OnClickListener]
         * instead of [android.widget.CompoundButton.setOnCheckedChangeListener] or as
         * a [MediatorLiveData]  object as it will change the [port] incorrectly when
         * loading the bindings.
         */
        fun onHttpsToggle() {
            port.value = if (https.value == true) DEFAULT_HTTPS_PORT else DEFAULT_HTTP_PORT
        }

        private fun onChange(
            @Suppress("UNUSED_PARAMETER") value: Boolean,
        ) {
            onChange()
        }

        private fun onChange(
            @Suppress("UNUSED_PARAMETER") value: String,
        ) {
            onChange()
        }

        private fun onChange() {
            _changed.value = get() != source
        }

        fun reset(config: AuthConfig) {
            source = config
            load(config)
        }

        fun resetToDefaultConfig() {
            // Source is not changed as resetting to default does not commit changes
            load(AuthConfig.defaultConfig)
        }

        private fun load(config: AuthConfig) {
            https.value = config.https
            port.value = config.port
            contentServicePath.value = config.contentServicePath
            realm.value = config.realm
            clientId.value = config.clientId
            redirectUrl = config.redirectUrl
            onChange()
        }

        fun get(): AuthConfig {
            return AuthConfig(
                https = https.value ?: false,
                port = port.value ?: "",
                contentServicePath = contentServicePath.value ?: "",
                realm = realm.value ?: "",
                clientId = clientId.value ?: "",
                redirectUrl = redirectUrl,
            )
        }

        companion object {
            private const val DEFAULT_HTTP_PORT = "80"
            private const val DEFAULT_HTTPS_PORT = "443"
        }
    }
}
