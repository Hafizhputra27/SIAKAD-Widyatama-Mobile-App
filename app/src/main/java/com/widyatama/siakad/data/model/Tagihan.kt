package com.widyatama.siakad.data.model

import com.google.firebase.Timestamp

data class Tagihan(
    val tagihanId: String = "",
    val judul: String = "",
    val tahunAjaran: String = "",
    val nominal: Long = 0L,
    val diskon: Long = 0L,
    val batasWaktu: Timestamp? = null,
    val status: String = "BELUM_LUNAS",
    val tipe: String = "TAGIHAN"
)