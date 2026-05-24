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
    val semesterBerjalan: Int = 1
)