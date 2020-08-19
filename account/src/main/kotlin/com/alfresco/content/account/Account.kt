package com.alfresco.content.account

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle

data class Account(
    val id: String,
    val authState: String,
    val authType: String,
    val authConfig: String,
    val serverUrl: String,
    val displayName: String? = null,
    val email: String? = null,
    val myFiles: String? = null
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
            myFiles: String
        ) {
            val b = Bundle()
            b.putString(authTypeKey, authType)
            b.putString(authConfigKey, authConfig)
            b.putString(serverKey, serverUrl)
            b.putString(displayNameKey, displayName)
            b.putString(emailKey, email)
            b.putString(myFilesKey, myFiles)
            val acc = Account(id, context.getString(R.string.android_auth_account_type))
            AccountManager.get(context).addAccountExplicitly(acc, authState, b)
        }

        fun update(context: Context, id: String, authState: String) {
            val am = AccountManager.get(context)
            val acc = getAndroidAccount(context)
            if (acc?.name == id) {
                am.setPassword(acc, authState)
            }
        }

        fun update(
            context: Context,
            id: String,
            authState: String,
            displayName: String,
            email: String,
            myFiles: String
        ) {
            val am = AccountManager.get(context)
            val acc = getAndroidAccount(context)
            am.setPassword(acc, authState)
            am.setUserData(acc, displayNameKey, displayName)
            am.setUserData(acc, emailKey, email)
            am.setUserData(acc, myFilesKey, myFiles)

            if (acc?.name != id) {
                am.renameAccount(acc, id, null, null)
            }
        }

        fun delete(activity: Activity, callback: () -> Unit) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                AccountManager.get(activity).removeAccount(getAndroidAccount(activity), activity, {
                    callback()
                }, null)
            } else {
                @Suppress("DEPRECATION")
                AccountManager.get(activity).removeAccount(getAndroidAccount(activity), {
                    callback()
                }, null)
            }
        }

        fun getAccount(context: Context): com.alfresco.content.account.Account? {
            val am = AccountManager.get(context)
            val accountList = am.getAccountsByType(context.getString(R.string.android_auth_account_type))
            if (accountList.isNotEmpty()) {
                val acc = accountList[0]
                return Account(
                    acc.name,
                    am.getPassword(acc),
                    am.getUserData(acc, authTypeKey),
                    am.getUserData(acc, authConfigKey),
                    am.getUserData(acc, serverKey),
                    am.getUserData(acc, displayNameKey),
                    am.getUserData(acc, emailKey),
                    am.getUserData(acc, myFilesKey)
                )
            }
            return null
        }

        private fun getAndroidAccount(context: Context): Account? {
            val am = AccountManager.get(context)
            val accountList = am.getAccountsByType(context.getString(R.string.android_auth_account_type))
            return accountList.first()
        }
    }
}
