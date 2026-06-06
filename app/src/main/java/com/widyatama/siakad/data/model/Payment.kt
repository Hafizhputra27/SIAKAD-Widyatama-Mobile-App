package com.widyatama.siakad.data.model

import com.google.firebase.firestore.PropertyName
import java.util.Date

data class Payment(
    val id: String = "",
    @get:PropertyName("judul") val title: String = "",
    @get:PropertyName("tipe") val type: String = "",
    @get:PropertyName("total") val total: Int = 0,
    @get:PropertyName("jatuhTempo") val dueDate: Date? = null,
    @get:PropertyName("tanggalBayar") val paymentDate: Date? = null,
    @get:PropertyName("paymentMethod") val paymentMethod: String = "",
    @get:PropertyName("status") val status: String = "",
    @get:PropertyName("isLunas") val isPaid: Boolean = false,
    @get:PropertyName("tahunAjaran") val academicYear: String = "",
    @get:PropertyName("semester") val semester: Int = 0,
    @get:PropertyName("diskon") val discount: Int = 0,
    @get:PropertyName("createdAt") val createdAt: Date? = null
)