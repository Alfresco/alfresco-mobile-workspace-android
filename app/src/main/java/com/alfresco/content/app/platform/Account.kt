package com.alfresco.content.app.platform

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle
import com.alfresco.content.app.BuildConfig

data class Account(
    val username: String,
    val authState: String,
    val authType: String,
    val serverUrl: String
) {

    companion object {
        fun createAccount(
            context: Context,
            username: String,
            authState: String,
            authType: String,
            serverUrl: String
        ) {
            val b = Bundle()
            b.putString("type", authType)
            b.putString("server", serverUrl)
            val acc =
                Account(username, BuildConfig.APPLICATION_ID) // TODO: does it need to be unique?
            AccountManager.get(context).addAccountExplicitly(acc, authState, b)
        }

        fun getAccount(context: Context): com.alfresco.content.app.platform.Account? {
            val am = AccountManager.get(context)
            val accountList = am.getAccountsByType(BuildConfig.APPLICATION_ID)
            if (accountList.isNotEmpty()) {
                val acc = accountList[0]
                return Account(
                    acc.name,
                    am.getPassword(acc),
                    am.getUserData(acc, "type"),
                    am.getUserData(acc, "server")
                )
            }
            return null
        }
    }
}
