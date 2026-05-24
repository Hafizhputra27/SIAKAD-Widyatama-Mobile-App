package com.widyatama.siakad.data.model

data class AcademicYear(
    val id: Int,
    val year: String,
    val totalInstallments: String,
    val nearestDeadline: String,
    val totalPaid: String,
    val progress: Int,
    val status: String
)