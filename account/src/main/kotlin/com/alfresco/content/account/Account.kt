package com.alfresco.content.account

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle

data class Account(
    val username: String,
    val authState: String,
    val authType: String,
    val authConfig: String,
    val serverUrl: String
) {
    companion object {
        private const val type = "com.alfresco.content.app.debug" // TODO:

        fun createAccount(
            context: Context,
            username: String,
            authState: String,
            authType: String,
            authConfig: String,
            serverUrl: String
        ) {
            val b = Bundle()
            b.putString("type", authType)
            b.putString("config", authConfig)
            b.putString("server", serverUrl)
            val acc = Account(username, type) // TODO: does it need to be unique?
            AccountManager.get(context).addAccountExplicitly(acc, authState, b)
        }

        fun getAccount(context: Context): com.alfresco.content.account.Account? {
            val am = AccountManager.get(context)
            val accountList = am.getAccountsByType(type)
            if (accountList.isNotEmpty()) {
                val acc = accountList[0]
                return Account(
                    acc.name,
                    am.getPassword(acc),
                    am.getUserData(acc, "type"),
                    am.getUserData(acc, "config"),
                    am.getUserData(acc, "server")
                )
            }
            return null
        }
    }
}
