package com.alfresco.content.account

import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import com.alfresco.content.account.SecureSharedPreferencesManager.Companion.KEY_DISPLAY_NAME
import com.alfresco.content.account.SecureSharedPreferencesManager.Companion.KEY_EMAIL
import com.alfresco.content.account.SecureSharedPreferencesManager.Companion.KEY_PASSWORD
import android.accounts.Account as AndroidAccount

data class Account(
    val id: String,
    val authState: String,
    val authType: String,
    val authConfig: String,
    val serverUrl: String,
    val displayName: String? = null,
    val email: String? = null,
    val myFiles: String? = null,
) {
    companion object {
        private const val displayNameKey = "displayName"
        private const val emailKey = "email"
        private const val authTypeKey = "type"
        private const val authConfigKey = "config"
        private const val serverKey = "server"
        private const val myFilesKey = "myFiles"

        fun createAccount(
            context: Context,
            id: String,
            authState: String,
            authType: String,
            authConfig: String,
            serverUrl: String,
            displayName: String,
            email: String,
            myFiles: String,
        ) {
            val sharedSecure = SecureSharedPreferencesManager(context)

            val b = Bundle()
            b.putString(authTypeKey, authType)
            b.putString(authConfigKey, authConfig)
            b.putString(serverKey, serverUrl)
            b.putString(displayNameKey, KEY_DISPLAY_NAME)
            b.putString(emailKey, KEY_EMAIL)
            b.putString(myFilesKey, myFiles)
            val acc = AndroidAccount(id, context.getString(R.string.android_auth_account_type))

            // Save credentials securely using the SecureSharedPreferencesManager
            sharedSecure.saveCredentials(email, authState, displayName)

            AccountManager.get(context).addAccountExplicitly(acc, KEY_PASSWORD, b)
        }

        fun update(context: Context, id: String, authState: String) {
            val am = AccountManager.get(context)
            val acc = getAndroidAccount(context)
            val sharedSecure = SecureSharedPreferencesManager(context)
            if (acc?.name == id) {
                // Save credentials securely using the SecureSharedPreferencesManager
                sharedSecure.savePassword(authState)
                am.setPassword(acc, KEY_PASSWORD)
            }
        }

        fun update(
            context: Context,
            id: String,
            authState: String,
            displayName: String,
            email: String,
            myFiles: String,
        ) {
            val sharedSecure = SecureSharedPreferencesManager(context)
            val am = AccountManager.get(context)
            val acc = getAndroidAccount(context)

            // Save credentials securely using the SecureSharedPreferencesManager
            sharedSecure.saveCredentials(email, authState, displayName)

            am.setPassword(acc, KEY_PASSWORD)
            am.setUserData(acc, displayNameKey, KEY_DISPLAY_NAME)
            am.setUserData(acc, emailKey, KEY_EMAIL)
            am.setUserData(acc, myFilesKey, myFiles)

            if (acc?.name != id) {
                am.renameAccount(acc, id, null, null)
            }
        }

        fun delete(context: Context, callback: () -> Unit) {
            AccountManager.get(context)
                .removeAccount(getAndroidAccount(context), null, {
                    callback()
                }, null)
        }

        fun getAccount(context: Context): Account? {
            val am = AccountManager.get(context)
            val accountList = am.getAccountsByType(context.getString(R.string.android_auth_account_type))
            val sharedSecure = SecureSharedPreferencesManager(context)
            // get credentials securely using the SecureSharedPreferencesManager
            val secureCredentials = sharedSecure.getSavedCredentials()
            if (accountList.isNotEmpty() && secureCredentials != null) {
                val acc = accountList[0]
                return Account(
                    acc.name,
                    secureCredentials.second,
                    am.getUserData(acc, authTypeKey),
                    am.getUserData(acc, authConfigKey),
                    am.getUserData(acc, serverKey),
                    secureCredentials.third,
                    secureCredentials.first,
                    am.getUserData(acc, myFilesKey),
                )
            }
            return null
        }

        private fun getAndroidAccount(context: Context): AndroidAccount? {
            val am = AccountManager.get(context)
            val accountList = am.getAccountsByType(context.getString(R.string.android_auth_account_type))
            return accountList.first()
        }
    }
}
