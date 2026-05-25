package com.widyatama.siakad.data.model

import com.google.firebase.Timestamp

data class Payment(
    val tagihanId: String = "",
    val judul: String = "",
    val tahunAjaran: String = "",
    val nominal: Long = 0L,
    val diskon: Long = 0L,
    val batasWaktu: Timestamp? = null,
    val status: String = "BELUM_LUNAS",
    val tipe: String = "TAGIHAN",
    val tanggalBayar: Timestamp? = null
)

data class PaymentItem(
    val name: String = "",
    val amount: Long = 0,
    val status: String = "BELUM BAYAR"
)
