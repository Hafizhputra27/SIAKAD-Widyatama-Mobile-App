package com.widyatama.siakad.data.remote

import com.widyatama.siakad.data.model.Course
import com.widyatama.siakad.data.model.Lecturer
import com.widyatama.siakad.data.model.Room
import com.widyatama.siakad.data.model.Student
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class FirestoreSeeder {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun seedAllData(
        onProgress: (String) -> Unit,
        onComplete: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        seedStudents(onProgress) {
            seedLecturers(onProgress) {
                seedRooms(onProgress) {
                    seedCourses(onProgress) {
                        onComplete()
                    }
                }
            }
        }
    }

    private fun seedStudents(onProgress: (String) -> Unit, onComplete: () -> Unit) {
        val students = listOf(
            Student(
                npm = "241111011",
                name = "Yansen",
                major = "Sistem Informasi",
                campusEmail = "yansen@widyatama.ac.id",
                passwordHash = "SeededPassword123",
                photoUrl = "",
                status = "AKTIF",
                kelas = "REGULER",
                angkatan = 2024,
                ipkKumulatif = 3.82,
                totalSksLulus = 72,
                totalSksTarget = 144,
                semesterBerjalan = 4
            ),
            Student(
                npm = "241111038",
                name = "Jeni",
                major = "Sistem Informasi",
                campusEmail = "jeni@widyatama.ac.id",
                passwordHash = "SeededPassword123",
                photoUrl = "",
                status = "AKTIF",
                kelas = "REGULER",
                angkatan = 2024,
                ipkKumulatif = 3.75,
                totalSksLulus = 68,
                totalSksTarget = 144,
                semesterBerjalan = 4
            ),
            Student(
                npm = "241111043",
                name = "Edo",
                major = "Sistem Informasi",
                campusEmail = "edo@widyatama.ac.id",
                passwordHash = "SeededPassword123",
                photoUrl = "",
                status = "AKTIF",
                kelas = "REGULER",
                angkatan = 2024,
                ipkKumulatif = 3.90,
                totalSksLulus = 80,
                totalSksTarget = 144,
                semesterBerjalan = 4
            )
        )

        val batch = db.batch()
        students.forEach { student ->
            val ref = db.collection("mahasiswa").document(student.npm)
            batch.set(ref, student)
        }

        batch.commit()
            .addOnSuccessListener {
                onProgress("Seeded ${students.size} students")
                onComplete()
            }
            .addOnFailureListener { onComplete() }
    }

    private fun seedLecturers(onProgress: (String) -> Unit, onComplete: () -> Unit) {
        val lecturers = listOf(
            Lecturer(nidn = "001", name = "Dani Hamdani, S.Kom., M.T.", email = "dani@widyatama.ac.id", department = "Mobile Development"),
            Lecturer(nidn = "002", name = "DR. R.A.E. Virgana Targa Sapanji, S.Kom., M.T.", email = "virgana@widyatama.ac.id", department = "AI & Machine Learning"),
            Lecturer(nidn = "003", name = "Rikky Wisnu Nugraha", email = "rikky@widyatama.ac.id", department = "Computer Vision"),
            Lecturer(nidn = "004", name = "Ir. Ucu Nugraha, S.T., M.Kom.", email = "ucu@widyatama.ac.id", department = "IT Management"),
            Lecturer(nidn = "005", name = "Ir. Sri Lestari, M.T.", email = "sri@widyatama.ac.id", department = "Statistics")
        )

        val batch = db.batch()
        lecturers.forEach { lecturer ->
            val ref = db.collection("lecturers").document(lecturer.nidn)
            batch.set(ref, lecturer)
        }

        batch.commit()
            .addOnSuccessListener {
                onProgress("Seeded ${lecturers.size} lecturers")
                onComplete()
            }
            .addOnFailureListener { onComplete() }
    }

    private fun seedRooms(onProgress: (String) -> Unit, onComplete: () -> Unit) {
        val rooms = listOf(
            Room(roomName = "Ruangan B409", building = "Gedung B", floor = 4),
            Room(roomName = "Ruangan C206", building = "Gedung C", floor = 2),
            Room(roomName = "Ruangan C218", building = "Gedung C", floor = 2),
            Room(roomName = "Ruangan K106", building = "Gedung K", floor = 1),
            Room(roomName = "Ruangan A101", building = "Gedung A", floor = 1)
        )

        val batch = db.batch()
        rooms.forEachIndexed { index, room ->
            val ref = db.collection("rooms").document("ROOM${index + 1}")
            batch.set(ref, room)
        }

        batch.commit()
            .addOnSuccessListener {
                onProgress("Seeded ${rooms.size} rooms")
                onComplete()
            }
            .addOnFailureListener { onComplete() }
    }

    private fun seedCourses(onProgress: (String) -> Unit, onComplete: () -> Unit) {
        val courses = listOf(
            Course(
                code = "IF301",
                name = "Pemrograman Mobile",
                sks = 3,
                type = "WAJIB",
                semester = 5,
                hari = "Senin",
                jamMulai = "08:00 - 10:30 WIB",
                room = "Ruangan B409 (Gedung B)",
                lecturer = "Dani Hamdani, S.Kom., M.T.",
                enrolledCount = 45,
                attendance = 10,
                totalAttendance = 14
            ),
            Course(
                code = "IF302",
                name = "e-Government",
                sks = 3,
                type = "WAJIB",
                semester = 5,
                hari = "Senin",
                jamMulai = "11:00 - 13:30 WIB",
                room = "Ruangan C206 (Gedung C)",
                lecturer = "DR. R.A.E. Virgana Targa Sapanji, S.Kom., M.T.",
                enrolledCount = 32,
                attendance = 10,
                totalAttendance = 14
            ),
            Course(
                code = "IF303",
                name = "[e] Pengantar Kecerdasan Buatan",
                sks = 4,
                type = "WAJIB",
                semester = 5,
                hari = "Selasa",
                jamMulai = "09:45 - 11:23 WIB",
                room = "Ruangan C218 (Gedung C)",
                lecturer = "Rikky Wisnu Nugraha",
                enrolledCount = 45,
                attendance = 8,
                totalAttendance = 14
            ),
            Course(
                code = "IF304",
                name = "[e] Manajemen Risiko IT",
                sks = 5,
                type = "PILIHAN",
                semester = 5,
                hari = "Selasa",
                jamMulai = "12:45 - 14:30 WIB",
                room = "Ruangan K106 (Gedung K)",
                lecturer = "Ir. Ucu Nugraha, S.T., M.Kom.",
                enrolledCount = 32,
                attendance = 9,
                totalAttendance = 14
            ),
            Course(
                code = "IF305",
                name = "SAP AC010 Business Processes in Financial Accounting",
                sks = 4,
                type = "WAJIB",
                semester = 5,
                hari = "Rabu",
                jamMulai = "08:00 - 10:30 WIB",
                room = "Ruangan B409 (Gedung B)",
                lecturer = "Dani Hamdani, S.Kom., M.T.",
                enrolledCount = 45,
                attendance = 11,
                totalAttendance = 14
            ),
            Course(
                code = "IF306",
                name = "Desain Pengalaman Pengguna",
                sks = 5,
                type = "PILIHAN",
                semester = 5,
                hari = "Rabu",
                jamMulai = "11:00 - 13:30 WIB",
                room = "Ruangan C206 (Gedung C)",
                lecturer = "Ir. Ucu Nugraha, S.T., M.Kom.",
                enrolledCount = 32,
                attendance = 7,
                totalAttendance = 14
            ),
            Course(
                code = "IF307",
                name = "Sains Data",
                sks = 4,
                type = "WAJIB",
                semester = 5,
                hari = "Kamis",
                jamMulai = "08:00 - 10:30 WIB",
                room = "Ruangan B409 (Gedung B)",
                lecturer = "DR. R.A.E. Virgana Targa Sapanji, S.Kom., M.T.",
                enrolledCount = 45,
                attendance = 10,
                totalAttendance = 14
            ),
            Course(
                code = "IF308",
                name = "Statistika",
                sks = 5,
                type = "PILIHAN",
                semester = 5,
                hari = "Kamis",
                jamMulai = "11:00 - 13:30 WIB",
                room = "Ruangan C206 (Gedung C)",
                lecturer = "Ir. Sri Lestari, M.T.",
                enrolledCount = 32,
                attendance = 8,
                totalAttendance = 14
            )
        )

        val batch = db.batch()
        courses.forEach { course ->
            val ref = db.collection("courses").document(course.code)
            batch.set(ref, course)
        }

        batch.commit()
            .addOnSuccessListener {
                onProgress("Seeded ${courses.size} courses")
                onComplete()
            }
            .addOnFailureListener { onComplete() }
    }
}