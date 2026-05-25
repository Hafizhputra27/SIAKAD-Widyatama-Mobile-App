package com.widyatama.siakad.core.utils

import java.text.NumberFormat
import java.util.Locale

object FormatUtils {
    fun formatCurrency(amount: Long): String {
        val formatter = NumberFormat.getIntegerInstance(Locale("id", "ID"))
        return "Rp " + formatter.format(amount)
    }

    fun formatGpa(gpa: Double): String {
        return String.format("%.2f", gpa)
    }
}
