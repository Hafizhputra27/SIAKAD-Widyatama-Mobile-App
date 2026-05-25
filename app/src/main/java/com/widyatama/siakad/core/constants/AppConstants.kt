package com.widyatama.siakad.core.constants

object AppConstants {
    const val PREF_NAME = "siakad_prefs"
    const val KEY_NPM = "logged_npm"
    const val KEY_NAME = "logged_name"
    const val KEY_PRODI = "logged_prodi"
    const val KEY_EMAIL = "logged_email"
    const val KEY_IS_LOGGED_IN = "is_logged_in"
    const val KEY_REMEMBER_ME = "remember_me"
    const val KEY_SEMESTER = "current_semester"

    // Firestore Collections
    const val COL_MAHASISWA = "mahasiswa"
    const val COL_COURSES = "courses"
    const val COL_LECTURERS = "lecturers"
    const val COL_ROOMS = "rooms"
    const val COL_PRESENSI = "presensi"
    const val COL_PENGUMUMAN = "pengumuman"

    // Sub-collections
    const val SUBCOL_ACADEMIC_RESULTS = "academic_results"
    const val SUBCOL_PAYMENTS = "payments"
    const val SUBCOL_TAGIHAN = "tagihan"
    const val SUBCOL_TRANSKRIP = "transkrip"
    const val SUBCOL_PENGATURAN = "pengaturan"
}
