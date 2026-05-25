package com.widyatama.siakad.core.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    fun formatDate(date: Date, pattern: String = "dd MMMM yyyy"): String {
        return SimpleDateFormat(pattern, Locale("id", "ID")).format(date)
    }

    fun getDayName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            java.util.Calendar.MONDAY -> "Senin"
            java.util.Calendar.TUESDAY -> "Selasa"
            java.util.Calendar.WEDNESDAY -> "Rabu"
            java.util.Calendar.THURSDAY -> "Kamis"
            java.util.Calendar.FRIDAY -> "Jumat"
            java.util.Calendar.SATURDAY -> "Sabtu"
            java.util.Calendar.SUNDAY -> "Minggu"
            else -> "Senin"
        }
    }
}
