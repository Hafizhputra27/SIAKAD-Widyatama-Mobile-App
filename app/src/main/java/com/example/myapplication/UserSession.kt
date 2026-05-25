package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences

/**
 * A simple session to manage user data across the app using SharedPreferences.
 */
object UserSession {
    private const val PREF_NAME = "user_prefs"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var name: String
        get() = prefs.getString("name", "Ahmad Hafizh Karunia Putra") ?: "Ahmad Hafizh Karunia Putra"
        set(value) = prefs.edit().putString("name", value).apply()

    var nim: String
        get() = prefs.getString("nim", "20248831") ?: "20248831"
        set(value) = prefs.edit().putString("nim", value).apply()

    var major: String
        get() = prefs.getString("major", "S1 Teknik Informatika") ?: "S1 Teknik Informatika"
        set(value) = prefs.edit().putString("major", value).apply()

    var ktp: String
        get() = prefs.getString("ktp", "3201234567890001") ?: "3201234567890001"
        set(value) = prefs.edit().putString("ktp", value).apply()

    var gender: String
        get() = prefs.getString("gender", "Laki-laki") ?: "Laki-laki"
        set(value) = prefs.edit().putString("gender", value).apply()

    var birth: String
        get() = prefs.getString("birth", "Jakarta, 12 Agustus 2002") ?: "Jakarta, 12 Agustus 2002"
        set(value) = prefs.edit().putString("birth", value).apply()

    var religion: String
        get() = prefs.getString("religion", "Islam") ?: "Islam"
        set(value) = prefs.edit().putString("religion", value).apply()

    var nationality: String
        get() = prefs.getString("nationality", "Warga Negara Indonesia (WNI)") ?: "Warga Negara Indonesia (WNI)"
        set(value) = prefs.edit().putString("nationality", value).apply()

    var email: String
        get() = prefs.getString("email", "ahmad.hafizh@widyatama.ac.id") ?: "ahmad.hafizh@widyatama.ac.id"
        set(value) = prefs.edit().putString("email", value).apply()

    var phone: String
        get() = prefs.getString("phone", "+62 812-3456-7890") ?: "+62 812-3456-7890"
        set(value) = prefs.edit().putString("phone", value).apply()

    var address: String
        get() = prefs.getString("address", "Jl. Kemang Raya No. 123, Kel. Bangka, Kec. Mampang Prapatan, Jakarta Selatan, DKI Jakarta 12730") ?: "Jl. Kemang Raya No. 123, Kel. Bangka, Kec. Mampang Prapatan, Jakarta Selatan, DKI Jakarta 12730"
        set(value) = prefs.edit().putString("address", value).apply()

    var isLoggedIn: Boolean
        get() = prefs.getBoolean("is_logged_in", false)
        set(value) = prefs.edit().putBoolean("is_logged_in", value).apply()

    var gpa: String
        get() = prefs.getString("gpa", "3.88") ?: "3.88"
        set(value) = prefs.edit().putString("gpa", value).apply()

    var sks: String
        get() = prefs.getString("sks", "24") ?: "24"
        set(value) = prefs.edit().putString("sks", value).apply()

    var semester: String
        get() = prefs.getString("semester", "Genap 2024/2025") ?: "Genap 2024/2025"
        set(value) = prefs.edit().putString("semester", value).apply()

    fun resetToDefault() {
        prefs.edit().clear().apply()
    }
}
