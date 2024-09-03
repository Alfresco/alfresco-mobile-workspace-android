package com.alfresco.content.account

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Marked as SecureSharedPreferencesManager
 */
class SecureSharedPreferencesManager(private val context: Context) {

    private val masterKey: MasterKey = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    fun saveCredentials(email: String, password: String, displayName: String, auth0HostName: String, auth0ClientId: String) {
        try {
            val encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                KEY_PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

            encryptedSharedPreferences.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_DISPLAY_NAME, displayName)
                .putString(KEY_PASSWORD, password)
                .putString(KEY_HOST_NAME, auth0HostName)
                .putString(KEY_CLIENT_ID, auth0ClientId)
                .apply()

            Log.d(TAG, "Credentials saved securely")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving credentials: ${e.message}")
        }
    }

    fun savePassword(password: String) {
        try {
            val encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                KEY_PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

            encryptedSharedPreferences.edit()
                .putString(KEY_PASSWORD, password)
                .apply()

            Log.d(TAG, "Password saved securely")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving credentials: ${e.message}")
        }
    }

    fun getSavedCredentials(): Triple<String, String, String>? {
        try {
            val encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                KEY_PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

            val email = encryptedSharedPreferences.getString(KEY_EMAIL, null)
            val password = encryptedSharedPreferences.getString(KEY_PASSWORD, null)
            val displayName = encryptedSharedPreferences.getString(KEY_DISPLAY_NAME, null)

            if (email != null && password != null && displayName != null) {
                return Triple(email, password, displayName)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving credentials: ${e.message}")
        }

        return null
    }

    fun getSavedAuth0Data(): Pair<String, String>? {
        try {
            val encryptedSharedPreferences = EncryptedSharedPreferences.create(
                context,
                KEY_PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

            val hostName = encryptedSharedPreferences.getString(KEY_HOST_NAME, null)
            val clientId = encryptedSharedPreferences.getString(KEY_CLIENT_ID, null)

            if (hostName != null && clientId != null) {
                return Pair(hostName, clientId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving credentials: ${e.message}")
        }

        return null
    }

    companion object {
        const val KEY_DISPLAY_NAME = "display_name"
        const val KEY_EMAIL = "email"
        const val KEY_PASSWORD = "password"
        const val KEY_PREF_NAME = "secure_prefs"
        const val KEY_HOST_NAME = "host_name"
        const val KEY_CLIENT_ID = "client_id"

        val TAG: String = SecureSharedPreferencesManager::class.java.simpleName
    }
}
