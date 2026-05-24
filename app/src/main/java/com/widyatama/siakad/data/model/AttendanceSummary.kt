package com.widyatama.siakad.data.model

data class AttendanceSummary(
    val kodeKuliah: String = "",
    val totalHadir: Int = 0,
    val totalPertemuan: Int = 0,
    val persentaseHadir: Double = 0.0
)
