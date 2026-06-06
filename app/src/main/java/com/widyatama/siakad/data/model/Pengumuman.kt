package com.widyatama.siakad.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Pengumuman(
    @get:PropertyName("id") val id: String = "",
    @get:PropertyName("title") val title: String = "",
    @get:PropertyName("content") val content: String = "",
    @get:PropertyName("isActive") val isActive: Boolean = true,
    @get:PropertyName("priority") val priority: String = "NORMAL",
    @get:PropertyName("createdAt") val createdAt: Timestamp? = null
)