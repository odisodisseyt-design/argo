package com.textworld.app.data.token

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.textworld.app.core.Constants

/**
 * Opaque, long-lived session token in EncryptedSharedPreferences backed by the
 * Android Keystore. The password is NEVER stored on the device.
 */
class SecureTokenStore(context: Context) {
    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            Constants.SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    val token: String? get() = prefs.getString(Constants.TOKEN_KEY, null)
    val userId: Int get() = prefs.getInt(Constants.USER_ID_KEY, 0)
    val userName: String get() = prefs.getString(Constants.USER_NAME_KEY, "").orEmpty()

    fun save(token: String, userId: Int, userName: String) {
        prefs.edit()
            .putString(Constants.TOKEN_KEY, token)
            .putInt(Constants.USER_ID_KEY, userId)
            .putString(Constants.USER_NAME_KEY, userName)
            .apply()
    }

    fun clear() = prefs.edit().clear().apply()

    val isLoggedIn: Boolean get() = !token.isNullOrBlank()
}
