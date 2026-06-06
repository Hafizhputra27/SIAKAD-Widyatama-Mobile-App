package com.widyatama.siakad.data.model

data class Student(
    val npm: String = "",
    val name: String = "",
    val major: String = "",
    val campusEmail: String = "",
    val passwordHash: String = "",
    val photoUrl: String = "",
    val status: String = "",
    val kelas: String = "",
    val angkatan: Int = 0,
    val ipkKumulatif: Double = 0.0,
    val totalSksLulus: Int = 0,
    val totalSksTarget: Int = 0,
    val semesterBerjalan: Int = 1,
    // Biodata fields (new)
    val ktp: String = "",
    val gender: String = "",
    val birthDate: String = "",
    val religion: String = "",
    val nationality: String = "",
    val personalEmail: String = "",
    val phone: String = "",
    val address: String = ""
)
