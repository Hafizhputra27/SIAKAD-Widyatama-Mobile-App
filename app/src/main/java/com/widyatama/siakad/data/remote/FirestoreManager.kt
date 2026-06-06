package com.widyatama.siakad.data.remote

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.widyatama.siakad.core.constants.AppConstants
import com.widyatama.siakad.core.utils.ValidationUtils
import com.widyatama.siakad.data.local.SharedPrefManager
import com.widyatama.siakad.data.model.*

class FirestoreManager private constructor() {
    private val db: FirebaseFirestore

    init {
        val settings = com.google.firebase.firestore.firestoreSettings {
            setLocalCacheSettings(com.google.firebase.firestore.persistentCacheSettings {})
        }
        db = FirebaseFirestore.getInstance()
        db.firestoreSettings = settings
    }

    companion object {
        @Volatile private var instance: FirestoreManager? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: FirestoreManager().also { instance = it }
        }
    }

    // ── AUTH ──────────────────────────────────────────────────────────────
    fun signOut(context: Context) {
        FirebaseAuth.getInstance().signOut()
        SharedPrefManager.getInstance(context).clearSession()
    }

    fun getMahasiswa(npm: String, onResult: (Student?, String?) -> Unit) {
        db.collection(AppConstants.COL_MAHASISWA).document(npm).get()
            .addOnSuccessListener { doc ->
                // Provide more detailed debug info when document is missing or mapping fails
                if (doc.exists()) {
                    val student = try {
                        doc.toObject(Student::class.java)
                    } catch (e: Exception) {
                        android.util.Log.e("FirestoreManager", "getMahasiswa: toObject parse error for docId=${doc.id}: ${e.message}")
                        null
                    }
                    if (student != null) {
                        onResult(student, null)
                    } else {
                        onResult(null, "Failed to parse mahasiswa data for id=${doc.id}")
                    }
                } else {
                    android.util.Log.w("FirestoreManager", "getMahasiswa: document NOT FOUND for id=$npm | doc.id=${doc.id} | data=${doc.data}")
                    onResult(null, "Document not found for id=$npm")
                }
            }
            .addOnFailureListener { e ->
                android.util.Log.e("FirestoreManager", "getMahasiswa: failure for id=$npm: ${e.message}")
                onResult(null, e.message)
            }
    }

    // ── COURSES & SCHEDULE ────────────────────────────────────────────────
    fun getCoursesByDay(hari: String, semester: Int, onResult: (List<Course>, String?) -> Unit) {
        // Client-side filter: ambil by hari saja, filter semester & isActive di client
        // (menghindari composite index Firestore)
        db.collection(AppConstants.COL_COURSES)
            .whereEqualTo("hari", hari)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents
                    .filter {
                        (it.getLong("semester")?.toInt() == semester) &&
                        (it.getBoolean("isActive") ?: true)
                    }
                    .mapNotNull { it.toObject(Course::class.java) }
                    .sortedBy { it.jamMulai }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(emptyList(), it.message) }
    }

    fun getAllCoursesForSemester(semester: Int, onResult: (List<Course>, String?) -> Unit) {
        // Client-side filter: ambil by semester saja, filter isActive di client
        db.collection(AppConstants.COL_COURSES)
            .whereEqualTo("semester", semester)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents
                    .filter { it.getBoolean("isActive") ?: true }
                    .mapNotNull { it.toObject(Course::class.java) }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(emptyList(), it.message) }
    }

    fun getCoursesForStudent(npm: String, semester: Int, onResult: (List<Course>, String?) -> Unit) {
        // Helper to fetch attendance count for each course
        val fetchAttendanceAndComplete: (List<Course>, String?) -> Unit = { rawCourses, error ->
            if (error != null) {
                onResult(rawCourses, error)
            } else {
                db.collection(AppConstants.COL_PRESENSI)
                    .whereEqualTo("npm", npm)
                    .get()
                    .addOnSuccessListener { presensiSnap ->
                        val attendanceMap = mutableMapOf<String, Int>()
                        for (doc in presensiSnap.documents) {
                            val cId = doc.getString("courseId") ?: doc.getString("mataKuliahId")
                            val status = doc.getString("status")
                            if (cId != null && status == "HADIR") {
                                attendanceMap[cId] = (attendanceMap[cId] ?: 0) + 1
                            }
                        }
                        val updatedCourses = rawCourses.map { course ->
                            course.copy(attendance = attendanceMap[course.code] ?: 0)
                        }
                        onResult(updatedCourses, null)
                    }
                    .addOnFailureListener { e ->
                        onResult(rawCourses, e.message)
                    }
            }
        }

        // Step 1: Get enrolled courseIds from pertemuan
        db.collection(AppConstants.COL_PERTEMUAN)
            .whereArrayContains("enrolledNpms", npm)
            .get()
            .addOnSuccessListener { pertemuanSnap ->
                val courseIds = pertemuanSnap.documents.mapNotNull { it.getString("courseId") }.distinct()
                if (courseIds.isEmpty()) {
                    // Fallback: show all active courses for semester if no enrollment data yet
                    getAllCoursesForSemester(semester) { courses, error ->
                        fetchAttendanceAndComplete(courses, error)
                    }
                    return@addOnSuccessListener
                }

                // Step 2: Get course details by courseId (batch up to 30 for Firestore 'in' limit)
                val batches = courseIds.chunked(30)
                val allCourses = mutableListOf<Course>()
                var completed = 0
                var hasError: String? = null

                batches.forEach { batch ->
                    db.collection(AppConstants.COL_COURSES)
                        .whereIn(com.google.firebase.firestore.FieldPath.documentId(), batch)
                        .get()
                        .addOnSuccessListener { courseSnap ->
                            val courses = courseSnap.documents
                                .filter { it.getLong("semester")?.toInt() == semester && (it.getBoolean("isActive") ?: true) }
                                .mapNotNull { it.toObject(Course::class.java) }
                            synchronized(allCourses) { allCourses.addAll(courses) }
                            completed++
                            if (completed == batches.size) {
                                fetchAttendanceAndComplete(allCourses.sortedBy { it.jamMulai }, hasError)
                            }
                        }
                        .addOnFailureListener { e ->
                            hasError = e.message
                            completed++
                            if (completed == batches.size) {
                                fetchAttendanceAndComplete(allCourses.sortedBy { it.jamMulai }, hasError)
                            }
                        }
                }
            }
            .addOnFailureListener { onResult(emptyList(), it.message) }
    }

    // ── ATTENDANCE / PRESENSI ─────────────────────────────────────────────
    fun recordPresensi(presensi: Presensi, onResult: (Boolean, String?) -> Unit) {
        // Check duplicate first — client-side filter untuk hindari composite index
        db.collection(AppConstants.COL_PRESENSI)
            .whereEqualTo("npm", presensi.npm)
            .get()
            .addOnSuccessListener { snap ->
                val alreadyExists = snap.documents.any { doc ->
                    doc.getString("mataKuliahId") == presensi.mataKuliahId &&
                    doc.getString("pertemuanId") == presensi.pertemuanId
                }
                if (alreadyExists) {
                    onResult(false, "Presensi sudah tercatat untuk pertemuan ini")
                    return@addOnSuccessListener
                }
                val docRef = db.collection(AppConstants.COL_PRESENSI).document()
                val newPresensi = presensi.copy(id = docRef.id)
                docRef.set(newPresensi)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { onResult(false, it.message) }
            }
            .addOnFailureListener { onResult(false, it.message) }
    }

    fun getAttendanceSummary(npm: String, mataKuliahId: String, totalPertemuan: Int,
                              onResult: (AttendanceSummary?, String?) -> Unit) {
        // Client-side filter: ambil by npm saja, filter mataKuliahId & status di client
        db.collection(AppConstants.COL_PRESENSI)
            .whereEqualTo("npm", npm)
            .get()
            .addOnSuccessListener { snap ->
                val hadir = snap.documents.count { doc ->
                    doc.getString("mataKuliahId") == mataKuliahId &&
                    doc.getString("status") == "HADIR"
                }
                val summary = AttendanceSummary(
                    kodeKuliah = mataKuliahId,
                    totalHadir = hadir,
                    totalPertemuan = totalPertemuan,
                    persentaseHadir = if (totalPertemuan > 0) (hadir.toDouble() / totalPertemuan) * 100 else 0.0
                )
                onResult(summary, null)
            }
            .addOnFailureListener { onResult(null, it.message) }
    }

    // ── ACADEMIC RESULTS ──────────────────────────────────────────────────
    fun getAcademicResults(npm: String, semester: Int, onResult: (List<AcademicResult>, String?) -> Unit) {
        db.collection(AppConstants.COL_MAHASISWA).document(npm)
            .collection(AppConstants.SUBCOL_ACADEMIC_RESULTS)
            .whereEqualTo("semester", semester)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject(AcademicResult::class.java) }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(emptyList(), it.message) }
    }

    fun getAllAcademicResults(npm: String, onResult: (List<AcademicResult>, String?) -> Unit) {
        db.collection(AppConstants.COL_MAHASISWA).document(npm)
            .collection(AppConstants.SUBCOL_ACADEMIC_RESULTS)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject(AcademicResult::class.java) }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(emptyList(), it.message) }
    }

    // ── PAYMENTS / TAGIHAN ────────────────────────────────────────────────
    fun getTagihanAktif(npm: String, onResult: (List<Payment>, String?) -> Unit) {
        db.collection(AppConstants.COL_MAHASISWA).document(npm)
            .collection(AppConstants.SUBCOL_TAGIHAN)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents
                    .filter { it.getString("status") == "BELUM_LUNAS" || it.getString("status") == "PROSES" }
                    .mapNotNull { parsePaymentWithFallback(it) }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(emptyList(), it.message) }
    }

    fun getHistoryPembayaran(npm: String, onResult: (List<Payment>, String?) -> Unit) {
        // Client-side filter & sort: hindari composite index where + orderBy
        db.collection(AppConstants.COL_MAHASISWA).document(npm)
            .collection(AppConstants.SUBCOL_TAGIHAN)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents
                    .filter { it.getString("status") == "LUNAS" }
                    .mapNotNull { parsePaymentWithFallback(it) }
                    .sortedByDescending { it.paymentDate }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(emptyList(), it.message) }
    }

    private fun parsePaymentWithFallback(doc: com.google.firebase.firestore.DocumentSnapshot): Payment? {
        return try {
            Payment(
                id = doc.id,
                title = doc.getString("judul")
                    ?: doc.getString("nama")
                    ?: doc.getString("keterangan")
                    ?: "Tagihan",
                type = doc.getString("tipe") ?: "TAGIHAN",
                total = (doc.getDouble("total")
                    ?: doc.getDouble("nominal")
                    ?: doc.getLong("total")?.toDouble()
                    ?: doc.getLong("nominal")?.toDouble()
                    ?: 0.0).toInt(),
                dueDate = (doc.getTimestamp("jatuhTempo")
                    ?: doc.getTimestamp("batasWaktu")
                    ?: doc.getTimestamp("deadline")
                    ?: doc.getTimestamp("jatuh_tempo"))?.toDate(),
                paymentDate = (doc.getTimestamp("tanggalBayar")
                    ?: doc.getTimestamp("tanggal_bayar")
                    ?: doc.getTimestamp("paymentDate"))?.toDate(),
                paymentMethod = doc.getString("paymentMethod") ?: "",
                status = doc.getString("status") ?: "BELUM_LUNAS",
                isPaid = doc.getBoolean("isLunas")
                    ?: doc.getString("status")?.equals("LUNAS", ignoreCase = true)
                    ?: false,
                academicYear = doc.getString("tahunAjaran")
                    ?: doc.getString("tahun_ajaran")
                    ?: "-",
                semester = (doc.getLong("semester")?.toInt()
                    ?: doc.getDouble("semester")?.toInt()
                    ?: 0),
                discount = (doc.getDouble("diskon")
                    ?: doc.getDouble("discount")
                    ?: doc.getLong("diskon")?.toDouble()
                    ?: 0.0).toInt(),
                createdAt = doc.getTimestamp("createdAt")?.toDate()
            )
        } catch (e: Exception) {
            android.util.Log.e("TAGIHAN", "Parse error: ${e.message} | doc: ${doc.data}")
            null
        }
    }

    // ── PENGUMUMAN ────────────────────────────────────────────────────────
    fun getPengumuman(limit: Long = 5, onResult: (List<Pengumuman>, String?) -> Unit) {
        // Client-side filter & sort: hindari composite index where + orderBy
        db.collection(AppConstants.COL_PENGUMUMAN)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents
                    .filter { it.getBoolean("isActive") ?: it.getBoolean("isAktif") ?: false }
                    .mapNotNull { it.toObject(Pengumuman::class.java) }
                    .sortedByDescending { it.createdAt }
                    .take(limit.toInt())
                onResult(list, null)
            }
            .addOnFailureListener { onResult(emptyList(), it.message) }
    }

    // ── NOTIFICATIONS ──────────────────────────────────────────────────────
    fun getUnreadPengumumanCount(since: com.google.firebase.Timestamp, onResult: (Int, String?) -> Unit) {
        db.collection(AppConstants.COL_PENGUMUMAN)
            .get()
            .addOnSuccessListener { snap ->
                val count = snap.documents.count {
                    val isActive = it.getBoolean("isActive") ?: it.getBoolean("isAktif") ?: false
                    val createdAt = it.getTimestamp("createdAt")
                    isActive && createdAt != null && createdAt > since
                }
                onResult(count, null)
            }
            .addOnFailureListener { onResult(0, it.message) }
    }

    fun getUnreadTagihanCount(npm: String, since: com.google.firebase.Timestamp, onResult: (Int, String?) -> Unit) {
        db.collection(AppConstants.COL_MAHASISWA).document(npm)
            .collection(AppConstants.SUBCOL_TAGIHAN)
            .get()
            .addOnSuccessListener { snap ->
                val count = snap.documents.count {
                    val createdAt = it.getTimestamp("createdAt")
                    val status = it.getString("status") ?: ""
                    createdAt != null && createdAt > since && status != "LUNAS"
                }
                onResult(count, null)
            }
            .addOnFailureListener { onResult(0, it.message) }
    }

    fun getUpcomingPertemuan(npm: String, onResult: (List<com.widyatama.siakad.data.model.Pertemuan>, String?) -> Unit) {
        val now = com.google.firebase.Timestamp.now()
        val next24h = com.google.firebase.Timestamp(now.seconds + 86400, 0)
        db.collection(AppConstants.COL_PERTEMUAN)
            .whereArrayContains("enrolledNpms", npm)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents
                    .filter {
                        val tanggal = it.getTimestamp("tanggal")
                        val isActive = it.getBoolean("isActive") ?: true
                        tanggal != null && tanggal >= now && tanggal <= next24h && isActive
                    }
                    .mapNotNull { doc ->
                        try {
                            com.widyatama.siakad.data.model.Pertemuan(
                                id = doc.id,
                                courseId = doc.getString("courseId") ?: "",
                                courseName = doc.getString("courseName") ?: "",
                                nomorPertemuan = doc.getLong("nomorPertemuan")?.toInt() ?: 0,
                                tanggal = doc.getTimestamp("tanggal")?.toDate(),
                                jamMulai = doc.getString("jamMulai") ?: "",
                                jamSelesai = doc.getString("jamSelesai") ?: "",
                                isActive = doc.getBoolean("isActive") ?: true
                            )
                        } catch (e: Exception) { null }
                    }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(emptyList(), it.message) }
    }

    // ── USER PREFERENCES ─────────────────────────────────────────────────
    fun getUserPreferences(npm: String, onResult: (UserPreferences?, String?) -> Unit) {
        db.collection(AppConstants.COL_MAHASISWA).document(npm)
            .collection(AppConstants.SUBCOL_PENGATURAN).document("preferences").get()
            .addOnSuccessListener { onResult(it.toObject(UserPreferences::class.java), null) }
            .addOnFailureListener { onResult(null, it.message) }
    }

    fun saveUserPreferences(npm: String, prefs: UserPreferences, onResult: (Boolean, String?) -> Unit) {
        db.collection(AppConstants.COL_MAHASISWA).document(npm)
            .collection(AppConstants.SUBCOL_PENGATURAN).document("preferences")
            .set(prefs)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.message) }
    }

    fun updatePassword(npm: String, oldPassword: String, newPassword: String, onResult: (Boolean, String?) -> Unit) {
        db.collection(AppConstants.COL_MAHASISWA).document(npm).get()
            .addOnSuccessListener { doc ->
                val student = doc.toObject(Student::class.java)
                if (student == null || !ValidationUtils.verifyPassword(oldPassword, student.passwordHash)) {
                    onResult(false, "Password lama salah")
                    return@addOnSuccessListener
                }
                val newPasswordHash = ValidationUtils.hashPassword(newPassword)
                doc.reference.update("passwordHash", newPasswordHash)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { onResult(false, it.message) }
            }
            .addOnFailureListener { onResult(false, it.message) }
    }

    fun updatePhotoUrl(npm: String, photoUrl: String, callback: (Boolean) -> Unit) {
        db.collection(AppConstants.COL_MAHASISWA).document(npm)
            .update("photoUrl", photoUrl)
            .addOnSuccessListener { callback(true) }
            .addOnFailureListener { callback(false) }
    }

    fun updateBiodata(student: Student, onResult: (Boolean, String?) -> Unit) {
        val updates = hashMapOf<String, Any>(
            "ktp" to student.ktp,
            "gender" to student.gender,
            "birthDate" to student.birthDate,
            "religion" to student.religion,
            "nationality" to student.nationality,
            "personalEmail" to student.personalEmail,
            "phone" to student.phone,
            "address" to student.address,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        db.collection(AppConstants.COL_MAHASISWA).document(student.npm)
            .update(updates)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.message) }
    }

    fun saveFcmToken(npm: String, token: String, onResult: (Boolean) -> Unit) {
        val data = hashMapOf(
            "fcmToken" to token,
            "updatedAt" to com.google.firebase.Timestamp.now()
        )
        db.collection(AppConstants.COL_MAHASISWA).document(npm)
            .collection(AppConstants.SUBCOL_PENGATURAN).document("preferences")
            .update(data as Map<String, Any>)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }
}
