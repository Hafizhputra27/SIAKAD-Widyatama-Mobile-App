package com.widyatama.siakad.data.model

data class JadwalItem(
    val mataKuliahId: String = "",
    val pertemuanId: String = "",
    val namaMataKuliah: String = "",
    val kode: String = "",
    val sks: Int = 0,
    val tipe: String = "WAJIB",
    val hari: String = "",
    val jamMulai: String = "",
    val jamSelesai: String = "",
    val ruangan: String = "",
    val gedung: String = "",
    val dosenNama: String = "",
    val totalHadir: Int = 0,
    val totalPertemuan: Int = 14
)