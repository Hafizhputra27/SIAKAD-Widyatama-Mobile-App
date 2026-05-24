package com.widyatama.siakad.data.model

data class AcademicResult(
    val semester: Int = 0,
    val courseCode: String = "",
    val courseName: String = "",
    val sks: Int = 0,
    val grade: String = "",
    val mutu: Double = 0.0
)