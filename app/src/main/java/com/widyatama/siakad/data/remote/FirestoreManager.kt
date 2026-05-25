package com.widyatama.siakad.data.remote

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.widyatama.siakad.core.constants.AppConstants
import com.widyatama.siakad.core.utils.ValidationUtils
import com.widyatama.siakad.data.model.*

class FirestoreManager private constructor() {
    private val db = FirebaseFirestore.getInstance()

    companion object {
        @Volatile private var instance: FirestoreManager? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: FirestoreManager().also { instance = it }
        }
    }

    // ── AUTH ──────────────────────────────────────────────────────────────
    fun loginMahasiswa(npm: String, password: String, onResult: (Student?, String?) -> Unit) {
        db.collection(AppConstants.COL_MAHASISWA).document(npm).get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) { onResult(null, "NPM tidak ditemukan"); return@addOnSuccessListener }
                val student = doc.toObject(Student::class.java)
                if (student != null && ValidationUtils.verifyPassword(password, student.passwordHash)) {
                    onResult(student, null)
                } else {
                    onResult(null, "Password salah")
                }
            }
            .addOnFailureListener { onResult(null, it.message) }
    }

    fun registerMahasiswa(student: Student, onResult: (Boolean, String?) -> Unit) {
        db.collection(AppConstants.COL_MAHASISWA).document(student.npm).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) { onResult(false, "NPM sudah terdaftar"); return@addOnSuccessListener }
                db.collection(AppConstants.COL_MAHASISWA).document(student.npm)
                    .set(student)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { onResult(false, it.message) }
            }
            .addOnFailureListener { onResult(false, it.message) }
    }

    fun getMahasiswa(npm: String, onResult: (Student?, String?) -> Unit) {
        db.collection(AppConstants.COL_MAHASISWA).document(npm).get()
            .addOnSuccessListener { doc ->
                onResult(doc.toObject(Student::class.java), null)
            }
            .addOnFailureListener { onResult(null, it.message) }
    }

    // ── COURSES & SCHEDULE ────────────────────────────────────────────────
    fun getCoursesByDay(hari: String, semester: Int, onResult: (List<Course>, String?) -> Unit) {
        db.collection(AppConstants.COL_COURSES)
            .whereEqualTo("hari", hari)
            .whereEqualTo("semester", semester)
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject(Course::class.java) }
                    .sortedBy { it.jamMulai }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(emptyList(), it.message) }
    }

    fun getAllCoursesForSemester(semester: Int, onResult: (List<Course>, String?) -> Unit) {
        db.collection(AppConstants.COL_COURSES)
            .whereEqualTo("semester", semester)
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject(Course::class.java) }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(emptyList(), it.message) }
    }

    // ── ATTENDANCE / PRESENSI ─────────────────────────────────────────────
    fun recordPresensi(presensi: Presensi, onResult: (Boolean, String?) -> Unit) {
        // Check duplicate first
        db.collection(AppConstants.COL_PRESENSI)
            .whereEqualTo("npm", presensi.npm)
            .whereEqualTo("mataKuliahId", presensi.mataKuliahId)
            .whereEqualTo("pertemuanId", presensi.pertemuanId)
            .get()
            .addOnSuccessListener { snap ->
                if (!snap.isEmpty) { onResult(false, "Presensi sudah tercatat untuk pertemuan ini"); return@addOnSuccessListener }
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
        db.collection(AppConstants.COL_PRESENSI)
            .whereEqualTo("npm", npm)
            .whereEqualTo("mataKuliahId", mataKuliahId)
            .whereEqualTo("status", "HADIR")
            .get()
            .addOnSuccessListener { snap ->
                val hadir = snap.size()
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

    fun getTranskrip(npm: String, onResult: (List<Transkrip>, String?) -> Unit) {
        db.collection(AppConstants.COL_MAHASISWA).document(npm)
            .collection(AppConstants.SUBCOL_TRANSKRIP)
            .orderBy("semester", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject(Transkrip::class.java) }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(emptyList(), it.message) }
    }

    // ── PAYMENTS / TAGIHAN ────────────────────────────────────────────────
    fun getTagihanAktif(npm: String, onResult: (List<Payment>, String?) -> Unit) {
        db.collection(AppConstants.COL_MAHASISWA).document(npm)
            .collection(AppConstants.SUBCOL_TAGIHAN)
            .whereEqualTo("status", "BELUM_LUNAS")
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject(Payment::class.java) }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(emptyList(), it.message) }
    }

    fun getHistoryPembayaran(npm: String, onResult: (List<Payment>, String?) -> Unit) {
        db.collection(AppConstants.COL_MAHASISWA).document(npm)
            .collection(AppConstants.SUBCOL_TAGIHAN)
            .whereEqualTo("status", "LUNAS")
            .orderBy("tanggalBayar", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject(Payment::class.java) }
                onResult(list, null)
            }
            .addOnFailureListener { onResult(emptyList(), it.message) }
    }

    // ── PENGUMUMAN ────────────────────────────────────────────────────────
    fun getPengumuman(limit: Long = 5, onResult: (List<Pengumuman>, String?) -> Unit) {
        db.collection(AppConstants.COL_PENGUMUMAN)
            .whereEqualTo("isActive", true)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { it.toObject(Pengumuman::class.java) }
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
}
