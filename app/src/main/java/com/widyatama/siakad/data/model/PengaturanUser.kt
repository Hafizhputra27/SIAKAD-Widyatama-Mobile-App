package com.widyatama.siakad.data.model

import com.google.firebase.Timestamp

data class PengaturanUser(
    val pushNotifikasi: Boolean = false,
    val notifikasiEmail: Boolean = false,
    val updatedAt: Timestamp? = null
)