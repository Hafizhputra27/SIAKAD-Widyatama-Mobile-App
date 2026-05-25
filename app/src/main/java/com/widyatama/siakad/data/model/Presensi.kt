package com.widyatama.siakad.data.model

import com.google.firebase.Timestamp

data class Presensi(
    val id: String = "",
    val npm: String = "",
    val mataKuliahId: String = "",
    val pertemuanId: String = "",
    val status: String = "HADIR",
    val waktu: Timestamp? = null,
    val metodeScan: String = "QR_CODE"
)
