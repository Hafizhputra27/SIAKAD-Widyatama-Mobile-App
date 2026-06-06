package com.widyatama.siakad.data.model

import java.util.Date

data class Pertemuan(
    val id: String = "",
    val courseId: String = "",
    val courseName: String = "",
    val nomorPertemuan: Int = 0,
    val tanggal: Date? = null,
    val jamMulai: String = "",
    val jamSelesai: String = "",
    val isActive: Boolean = true
)