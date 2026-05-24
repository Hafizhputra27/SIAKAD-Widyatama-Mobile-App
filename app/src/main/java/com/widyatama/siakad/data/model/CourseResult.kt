package com.widyatama.siakad.data.model

data class CourseResult(
    val name: String,
    val sks: Int,
    val mutu: Double,
    val grade: String,
    val iconRes: Int? = null
)