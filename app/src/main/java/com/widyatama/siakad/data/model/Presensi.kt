package com.widyatama.siakad.data.model

import com.google.firebase.firestore.PropertyName
import com.google.firebase.Timestamp

data class Presensi(
    val id: String = "",
    val npm: String = "",
    @get:PropertyName("mataKuliahId") val mataKuliahId: String = "",
    @get:PropertyName("courseId") val courseId: String = "",
    val pertemuanId: String = "",
    val status: String = "HADIR",
    @get:PropertyName("timestamp") val timestamp: Timestamp? = null,
    @get:PropertyName("scanMethod") val scanMethod: String = "QR_CODE",
    @get:PropertyName("mahasiswaName") val mahasiswaName: String = "",
    @get:PropertyName("deviceInfo") val deviceInfo: String = ""
)