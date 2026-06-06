package com.widyatama.siakad.data.model

data class Course(
    val code: String = "",
    val name: String = "",
    val sks: Int = 0,
    val type: String = "",
    val semester: Int = 0,
    val lecturerId: String = "",
    val roomId: String = "",
    val hari: String = "",
    val jamMulai: String = "",
    val jamSelesai: String = "",
    val room: String = "",
    val lecturer: String = "",
    val enrolledCount: Int = 0,
    val attendance: Int = 0,
    val totalAttendance: Int = 0,
    val isActive: Boolean = true
) {
    /** Display time range (e.g. "08:00 - 10:00") */
    val jamDisplay: String
        get() = if (jamSelesai.isNotEmpty()) "$jamMulai - $jamSelesai" else jamMulai
}
