package com.example.myapplication.data

import com.example.myapplication.model.Course

object CourseData {
    fun getCourses(): List<Course> {
        return listOf("Senin", "Selasa", "Rabu", "Kamis").flatMap { getCoursesByDay(it) }
    }

    fun getCoursesByDay(day: String): List<Course> {
        return when (day) {
            "Senin" -> listOf(
                Course(1, "Pemrograman Mobile", "WAJIB", "08:00 - 10:30 WIB", "Ruangan B409 (Gedung B)", "Dr. Ahmad Sutrisno, M.T.", 45, "Senin", 3),
                Course(2, "e-Government", "PILIHAN", "11:00 - 13:30 WIB", "Ruangan C206 (Gedung C)", "Siti Aminah, S.Kom., M.I.T.", 32, "Senin", 3)
            )
            "Selasa" -> listOf(
                Course(3, "[e] Pengantar Kecerdasan Buatan", "WAJIB", "09:45 - 11:235 WIB", "Ruangan C218 (Gedung C)", "Dr. Ahmad Sutrisno, M.T.", 45, "Selasa", 4),
                Course(4, "[e] Manajemen Risiko IT", "PILIHAN", "12:45 - 14:30 WIB", "Ruangan K106 (Gedung K)", "Siti Aminah, S.Kom., M.I.T.", 32, "Selasa", 5)
            )
            "Rabu" -> listOf(
                Course(5, "SAP AC010 Business Processes in Financial Accounting", "WAJIB", "08:00 - 10:30 WIB", "Ruangan B409 (Gedung B)", "Dr. Ahmad Sutrisno, M.T.", 45, "Rabu", 4),
                Course(6, "Desain Pengalaman Pengguna", "PILIHAN", "11:00 - 13:30 WIB", "Ruangan C206 (Gedung C)", "Siti Aminah, S.Kom., M.I.T.", 32, "Rabu", 5)
            )
            "Kamis" -> listOf(
                Course(7, "Sains Data", "WAJIB", "08:00 - 10:30 WIB", "Ruangan B409 (Gedung B)", "Dr. Ahmad Sutrisno, M.T.", 45, "Kamis", 4),
                Course(8, "Statistika", "PILIHAN", "11:00 - 13:30 WIB", "Ruangan C206 (Gedung C)", "Siti Aminah, S.Kom., M.I.T.", 32, "Kamis", 5)
            )
            else -> emptyList()
        }
    }
}