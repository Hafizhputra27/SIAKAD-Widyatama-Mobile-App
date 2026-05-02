package com.example.myapplication.data

import com.example.myapplication.model.Course

object CourseData {
    fun getCourses(): ArrayList<Course> {
        return arrayListOf(
            Course(
                id = 1,
                courseName = "Pengantar Kecerdasan Buatan",
                lecturerName = "Rikky Wisnu Nugraha, S.Kom., M.T.",
                classTime = "08:00 - 10:30",
                roomLocation = "C218"
            ),
            Course(
                id = 2,
                courseName = "Statistik dan Probabilitas",
                lecturerName = "Ir. Sri Lestari, S.T., M.Kom.",
                classTime = "11:00 - 12:30",
                roomLocation = "A302"
            ),
            Course(
                id = 3,
                courseName = "Pemrograman Mobile",
                lecturerName = "Dani Hamdani, S.T., M.Kom.",
                classTime = "13:00 - 15:30",
                roomLocation = "Lab D105"
            ),
            Course(
                id = 4,
                courseName = "Basis Data Lanjutan",
                lecturerName = "Prof. Budi Santoso, M.Kom.",
                classTime = "15:30 - 18:00",
                roomLocation = "Lab D106"
            ),
            Course(
                id = 5,
                courseName = "Komputer Grafik",
                lecturerName = "Dr. Adi Wijaya, S.Kom., M.T.",
                classTime = "08:00 - 10:30",
                roomLocation = "B301"
            )
        )
    }
}