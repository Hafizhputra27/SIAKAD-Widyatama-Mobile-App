package com.widyatama.siakad.data.model

import java.util.Date

sealed class NotificationItem(
    open val id: String,
    open val title: String,
    open val subtitle: String,
    open val timestamp: Date?,
    open val type: NotificationType
) {
    data class PengumumanItem(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val timestamp: Date?,
        val priority: String
    ) : NotificationItem(id, title, subtitle, timestamp, NotificationType.PENGUMUMAN)

    data class TagihanItem(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val timestamp: Date?,
        val total: Int,
        val status: String
    ) : NotificationItem(id, title, subtitle, timestamp, NotificationType.TAGIHAN)

    data class PresensiItem(
        override val id: String,
        override val title: String,
        override val subtitle: String,
        override val timestamp: Date?,
        val courseName: String,
        val jamMulai: String
    ) : NotificationItem(id, title, subtitle, timestamp, NotificationType.PRESENSI)
}

enum class NotificationType {
    PENGUMUMAN, TAGIHAN, PRESENSI
}