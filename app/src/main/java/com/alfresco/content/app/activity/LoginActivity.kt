package com.alfresco.content.app.activity

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.alfresco.auth.AuthConfig
import com.alfresco.auth.Credentials
import com.alfresco.content.account.Account
import com.alfresco.content.app.R
import com.alfresco.content.data.PeopleRepository
import com.alfresco.content.models.Person
import com.alfresco.content.session.Session
import kotlinx.coroutines.launch

class LoginActivity : com.alfresco.auth.activity.LoginActivity() {

    override fun onCredentials(credentials: Credentials, endpoint: String, authConfig: AuthConfig) {
        val account = Account(credentials.username, credentials.authState, credentials.authType, authConfig.jsonSerialize(), endpoint, null, null)
        val context = applicationContext

        lifecycleScope.launch {
            try {
                val session = Session(context, account)
                val person = PeopleRepository(session).me()
                processAccountInformation(person, credentials, authConfig, endpoint)
                navigateToMain()
            } catch (ex: Exception) {
                onError(R.string.auth_error_wrong_credentials)
            }
        }
    }

    private fun processAccountInformation(person: Person, myFiles: Node, credentials: Credentials, authConfig: AuthConfig, endpoint: String) {
        if (!viewModel.isReLogin) {
            Account.createAccount(
                this,
                person.id,
                credentials.authState,
                credentials.authType,
                authConfig.jsonSerialize(),
                endpoint,
                person.displayName ?: "",
                person.email
            )
        } else {
            Account.update(
                this,
                person.id,
                credentials.authState,
                person.displayName ?: "",
                person.email
            )
        }
    }

    private fun navigateToMain() {
        val i = Intent(this, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
        finish()
    }
}
