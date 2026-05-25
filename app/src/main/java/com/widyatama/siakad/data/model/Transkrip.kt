package com.widyatama.siakad.data.model

data class Transkrip(
    val semesterId: String = "",
    val namaResmi: String = "",
    val ipkSemester: Double = 0.0,
    val sksSemester: Int = 0,
    val ipkKumulatif: Double = 0.0,
    val totalSksLulus: Int = 0,
    val totalSksTarget: Int = 144,
    val isAktif: Boolean = false,
    val mataKuliah: List<Map<String, Any>> = emptyList()
)