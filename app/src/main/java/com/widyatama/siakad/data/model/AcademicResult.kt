package com.widyatama.siakad.data.model

import com.google.firebase.firestore.PropertyName

data class AcademicResult(
    @get:PropertyName("semester") val semester: Int = 0,
    @get:PropertyName("mataKuliahId") val courseCode: String = "",
    @get:PropertyName("mataKuliahName") val courseName: String = "",
    @get:PropertyName("sks") val sks: Int = 0,
    @get:PropertyName("nilaiHuruf") val grade: String = "",
    @get:PropertyName("nilaiAngka") val nilaiAngka: Int = 0,
    @get:PropertyName("mutu") val mutu: Double = 0.0,
    @get:PropertyName("status") val status: String = "LULUS"
)