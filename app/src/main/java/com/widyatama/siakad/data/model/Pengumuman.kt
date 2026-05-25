package com.widyatama.siakad.data.model

import com.google.firebase.Timestamp

data class Pengumuman(
    val pengumumanId: String = "",
    val judul: String = "",
    val isi: String = "",
    val tipe: String = "",
    val isAktif: Boolean = true,
    val createdAt: Timestamp? = null
)