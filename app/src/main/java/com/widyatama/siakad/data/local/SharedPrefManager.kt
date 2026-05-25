package com.widyatama.siakad.data.local

import android.content.Context
import com.widyatama.siakad.core.constants.AppConstants

class SharedPrefManager private constructor(context: Context) {
    private val prefs = context.getSharedPreferences(AppConstants.PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        @Volatile private var instance: SharedPrefManager? = null
        fun getInstance(context: Context) = instance ?: synchronized(this) {
            instance ?: SharedPrefManager(context.applicationContext).also { instance = it }
        }
    }

    var npm: String get() = prefs.getString(AppConstants.KEY_NPM, "") ?: ""
        set(value) = prefs.edit().putString(AppConstants.KEY_NPM, value).apply()
    var name: String get() = prefs.getString(AppConstants.KEY_NAME, "") ?: ""
        set(value) = prefs.edit().putString(AppConstants.KEY_NAME, value).apply()
    var prodi: String get() = prefs.getString(AppConstants.KEY_PRODI, "") ?: ""
        set(value) = prefs.edit().putString(AppConstants.KEY_PRODI, value).apply()
    var email: String get() = prefs.getString(AppConstants.KEY_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(AppConstants.KEY_EMAIL, value).apply()
    var isLoggedIn: Boolean get() = prefs.getBoolean(AppConstants.KEY_IS_LOGGED_IN, false)
        set(value) = prefs.edit().putBoolean(AppConstants.KEY_IS_LOGGED_IN, value).apply()
    var rememberMe: Boolean get() = prefs.getBoolean(AppConstants.KEY_REMEMBER_ME, false)
        set(value) = prefs.edit().putBoolean(AppConstants.KEY_REMEMBER_ME, value).apply()
    var semester: Int get() = prefs.getInt(AppConstants.KEY_SEMESTER, 1)
        set(value) = prefs.edit().putInt(AppConstants.KEY_SEMESTER, value).apply()

    fun clearSession() = prefs.edit().clear().apply()
}
