package com.alfresco.content.app.activity

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.alfresco.auth.AuthConfig
import com.alfresco.auth.Credentials
import com.alfresco.auth.activity.LoginViewModel.Companion.EXTRA_IS_LOGIN
import com.alfresco.content.account.Account
import com.alfresco.content.app.R
import com.alfresco.content.data.APIEvent
import com.alfresco.content.data.AnalyticsManager
import com.alfresco.content.data.BrowseRepository
import com.alfresco.content.data.OfflineRepository
import com.alfresco.content.data.PeopleRepository
import com.alfresco.content.data.SearchRepository
import com.alfresco.content.models.Person
import com.alfresco.content.session.Session
import kotlinx.coroutines.launch

class LoginActivity : com.alfresco.auth.activity.LoginActivity() {

    override fun onCredentials(credentials: Credentials, endpoint: String, authConfig: AuthConfig, isExtension: Boolean) {
        val account = Account(credentials.username, credentials.authState, credentials.authType, authConfig.jsonSerialize(), endpoint)
        val context = applicationContext

        lifecycleScope.launch {
            try {
                val session = Session(context, account)
                val person = PeopleRepository(session).me()
                val myFiles = BrowseRepository(session).myFilesNodeId()
                processAccountInformation(person, myFiles, credentials, authConfig, endpoint)
                AnalyticsManager(session).apiTracker(APIEvent.Login, true)
                if (isExtension) {
                    navigateToExtension()
                } else {
                    navigateToMain()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                onError(R.string.auth_error_wrong_credentials)
            }
        }
    }

    private fun processAccountInformation(person: Person, myFiles: String, credentials: Credentials, authConfig: AuthConfig, endpoint: String) {
        if (!viewModel.isReLogin) {
            Account.createAccount(
                this,
                person.id,
                credentials.authState,
                credentials.authType,
                authConfig.jsonSerialize(),
                endpoint,
                person.displayName ?: "",
                person.email,
                myFiles,
            )
        } else {
            val current = Account.getAccount(applicationContext)
            if (current?.id != person.id) {
                // Remove associated data if user changed
                SearchRepository().clearRecentSearch()
                OfflineRepository().cleanup()
            }

            Account.update(
                this,
                person.id,
                credentials.authState,
                person.displayName ?: "",
                person.email,
                myFiles,
            )
        }
    }

    private fun navigateToMain() {
        val i = Intent(this, MainActivity::class.java)
        intent.extras?.let { i.putExtras(it) }
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
        finish()
    }

    private fun navigateToExtension() {
        val i = Intent(this, ExtensionActivity::class.java)
        i.putExtra(EXTRA_IS_LOGIN, true)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
        finish()
    }
}
