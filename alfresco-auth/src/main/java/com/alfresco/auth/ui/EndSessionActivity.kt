package com.alfresco.auth.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alfresco.auth.AuthConfig
import com.alfresco.auth.AuthType
import com.alfresco.auth.pkce.PkceAuthService
import kotlinx.coroutines.launch
import net.openid.appauth.AuthState
import org.json.JSONException

/**
 * Companion [ViewModel] to [EndSessionActivity] for invoking the logout procedure.
 */
open class EndSessionViewModel(
    context: Context,
    authType: AuthType?,
    authState: String,
    authConfig: AuthConfig
) : ViewModel() {
    private val authType = authType
    private val authService: PkceAuthService?

    init {
        val state = try { AuthState.jsonDeserialize(authState) } catch (ex: JSONException) { null }

        authService = if (authType == AuthType.PKCE) {
            PkceAuthService(context, state, authConfig)
        } else {
            null
        }
    }

    /**
     * Invoke logout procedure, presenting extra activities if necessary.
     */
    fun logout(activity: Activity, requestCode: Int) {
        viewModelScope.launch {
            if (authType == AuthType.PKCE) {
                authService?.endSession(activity, requestCode)
            } else {
                activity.setResult(Activity.RESULT_OK)
                activity.finish()
            }
        }
    }
}

/**
 * Abstract activity that will trigger logout [onResume] and return the result to the caller.
 * Can be used with all [AuthType]s.
 */
abstract class EndSessionActivity<out T : EndSessionViewModel> : AppCompatActivity() {
    protected abstract val viewModel: T

    override fun onResume() {
        super.onResume()

        viewModel.logout(this, REQUEST_CODE_END_SESSION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_END_SESSION) {
            if (resultCode == Activity.RESULT_CANCELED) {
                setResult(Activity.RESULT_CANCELED)
                finish()
            } else {
                setResult(Activity.RESULT_OK)
                finish()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private companion object {
        const val REQUEST_CODE_END_SESSION = 1
    }
}
