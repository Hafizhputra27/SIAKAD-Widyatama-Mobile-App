package com.widyatama.siakad.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.widyatama.siakad.data.model.Course
import com.widyatama.siakad.data.model.NotificationItem
import com.widyatama.siakad.data.model.NotificationType
import com.widyatama.siakad.data.model.Pengumuman
import com.widyatama.siakad.data.model.Student
import com.widyatama.siakad.data.remote.FirestoreManager
import java.util.Calendar
import java.util.Date

class DashboardViewModel : ViewModel() {

    private val firestoreManager = FirestoreManager.getInstance()

    private val _studentData = MutableLiveData<Student?>()
    val studentData: LiveData<Student?> = _studentData

    private val _todayCourses = MutableLiveData<List<Course>>(emptyList())
    val todayCourses: LiveData<List<Course>> = _todayCourses

    private val _pengumuman = MutableLiveData<List<Pengumuman>>(emptyList())
    val pengumuman: LiveData<List<Pengumuman>> = _pengumuman

    private val _notifications = MutableLiveData<List<NotificationItem>>(emptyList())
    val notifications: LiveData<List<NotificationItem>> = _notifications

    private val _unreadNotificationCount = MutableLiveData(0)
    val unreadNotificationCount: LiveData<Int> = _unreadNotificationCount

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _pengumumanError = MutableLiveData<String?>()
    val pengumumanError: LiveData<String?> = _pengumumanError

    fun loadAllDashboardData(npm: String, semester: Int) {
        _isLoading.value = true
        loadStudentData(npm)
        loadTodayCourses(npm, semester)
        loadPengumuman()
    }

    fun loadStudentData(npm: String) {
        if (npm.isEmpty()) {
            _errorMessage.value = "NPM tidak ditemukan"
            _isLoading.value = false
            return
        }
        firestoreManager.getMahasiswa(npm) { student, error ->
            if (error != null) {
                _errorMessage.postValue(error)
            } else {
                _studentData.postValue(student)
            }
            checkLoadingComplete()
        }
    }

    fun loadTodayCourses(npm: String, semester: Int) {
        val hariIni = getCurrentDayName()
        if (hariIni == "Sabtu" || hariIni == "Minggu") {
            _todayCourses.postValue(emptyList())
            checkLoadingComplete()
            return
        }
        firestoreManager.getCoursesForStudent(npm, semester) { courses, error ->
            if (error != null) {
                _errorMessage.postValue(error)
                _todayCourses.postValue(emptyList())
            } else {
                val todayList = courses.filter { it.hari.equals(hariIni, ignoreCase = true) }
                _todayCourses.postValue(todayList)
            }
            checkLoadingComplete()
        }
    }

    fun loadPengumuman() {
        _pengumumanError.postValue(null)
        firestoreManager.getPengumuman(limit = 5) { list, error ->
            if (error != null) {
                _pengumumanError.postValue(error)
                _pengumuman.postValue(emptyList())
            } else {
                val sorted = (list ?: emptyList())
                    .sortedWith(
                        compareByDescending<Pengumuman> { it.createdAt?.seconds ?: 0L }
                            .thenByDescending {
                                when (it.priority.uppercase()) {
                                    "HIGH" -> 3
                                    "NORMAL" -> 2
                                    "LOW" -> 1
                                    else -> 0
                                }
                            }
                    )
                _pengumuman.postValue(sorted)
            }
            checkLoadingComplete()
        }
    }

    // ── NOTIFICATIONS ──────────────────────────────────────────────────────

    fun checkNotifications(
        npm: String,
        lastSeenPengumuman: Long,
        lastSeenTagihan: Long,
        lastSeenPresensi: Long
    ) {
        val sincePengumuman = Timestamp(Date(lastSeenPengumuman))
        val sinceTagihan = Timestamp(Date(lastSeenTagihan))

        firestoreManager.getUnreadPengumumanCount(sincePengumuman) { pengumumanCount, _ ->
            firestoreManager.getUnreadTagihanCount(npm, sinceTagihan) { tagihanCount, _ ->
                firestoreManager.getUpcomingPertemuan(npm) { pertemuanList, _ ->
                    val totalUnread = pengumumanCount + tagihanCount + pertemuanList.size
                    _unreadNotificationCount.postValue(totalUnread)

                    // Build notification list (all items, sorted by time)
                    val notifList = mutableListOf<NotificationItem>()

                    // Get pengumuman for notification list
                    firestoreManager.getPengumuman(limit = 20) { pengumumanList, _ ->
                        pengumumanList?.filter { it.isActive }?.forEach { p ->
                            notifList.add(
                                NotificationItem.PengumumanItem(
                                    id = p.id,
                                    title = p.title,
                                    subtitle = p.content,
                                    timestamp = p.createdAt?.toDate(),
                                    priority = p.priority
                                )
                            )
                        }

                        // Get tagihan for notification list
                        firestoreManager.getTagihanAktif(npm) { tagihanList, _ ->
                            tagihanList.forEach { t ->
                                notifList.add(
                                    NotificationItem.TagihanItem(
                                        id = t.id,
                                        title = t.title,
                                        subtitle = "Status: ${t.status}",
                                        timestamp = t.createdAt,
                                        total = t.total,
                                        status = t.status
                                    )
                                )
                            }

                            // Add presensi reminder
                            pertemuanList.forEach { pert ->
                                notifList.add(
                                    NotificationItem.PresensiItem(
                                        id = pert.id,
                                        title = "Pertemuan ${pert.nomorPertemuan} - ${pert.courseName}",
                                        subtitle = "${pert.jamMulai} - ${pert.jamSelesai}",
                                        timestamp = pert.tanggal,
                                        courseName = pert.courseName,
                                        jamMulai = pert.jamMulai
                                    )
                                )
                            }

                            // Sort by timestamp descending
                            val sorted = notifList.sortedByDescending { it.timestamp?.time ?: 0L }
                            _notifications.postValue(sorted)
                        }
                    }
                }
            }
        }
    }

    fun markAllAsRead(sharedPref: com.widyatama.siakad.data.local.SharedPrefManager) {
        sharedPref.markAllNotificationsAsRead()
        _unreadNotificationCount.postValue(0)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearPengumumanError() {
        _pengumumanError.value = null
    }

    private fun getCurrentDayName(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "Senin"
            Calendar.TUESDAY -> "Selasa"
            Calendar.WEDNESDAY -> "Rabu"
            Calendar.THURSDAY -> "Kamis"
            Calendar.FRIDAY -> "Jumat"
            Calendar.SATURDAY -> "Sabtu"
            Calendar.SUNDAY -> "Minggu"
            else -> "Senin"
        }
    }

    @Volatile
    private var completedCount = 0
    private val totalRequests = 3

    private fun checkLoadingComplete() {
        completedCount++
        if (completedCount >= totalRequests) {
            _isLoading.postValue(false)
            completedCount = 0
        }
    }
}