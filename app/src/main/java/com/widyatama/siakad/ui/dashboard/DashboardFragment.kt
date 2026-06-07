package com.widyatama.siakad.ui.dashboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.widyatama.siakad.R
import com.widyatama.siakad.adapter.CourseAdapter
import com.widyatama.siakad.adapter.NotificationAdapter
import com.widyatama.siakad.adapter.PengumumanAdapter
import com.widyatama.siakad.data.local.SharedPrefManager
import com.widyatama.siakad.data.remote.FirestoreManager
import com.widyatama.siakad.databinding.FragmentDashboardBinding
import com.widyatama.siakad.ui.attendance.QrScannerActivity
import com.widyatama.siakad.ui.auth.LoginActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val sharedPref by lazy { SharedPrefManager.getInstance(requireContext()) }
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var pengumumanAdapter: PengumumanAdapter
    private lateinit var notificationAdapter: NotificationAdapter

    private var currentScheduleZoom = 100
    private val minZoom = 80
    private val maxZoom = 150

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchScanner()
        } else {
            Toast.makeText(context, "Izin kamera diperlukan untuk scan presensi", Toast.LENGTH_SHORT).show()
        }
    }

    private val scanResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val status = data?.getStringExtra("status")
            val mataKuliah = data?.getStringExtra("mataKuliah") ?: ""
            if (status == "SUCCESS") {
                Toast.makeText(context, "Presensi berhasil! $mataKuliah", Toast.LENGTH_LONG).show()
                refreshDashboardData()
            } else {
                val error = data?.getStringExtra("error") ?: "Gagal scan presensi"
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupDateHeader()
        setupCourseRecyclerView()
        setupPengumumanPreview()
        setupNotificationIcon()
        setupScanPresensiButton()
        setupObservers()
        checkNetworkStatus()
        loadDashboardData()
    }

    private fun checkNetworkStatus() {
        if (!com.widyatama.siakad.core.utils.NetworkUtils.isConnected(requireContext())) {
            com.google.android.material.snackbar.Snackbar.make(
                binding.root,
                "Tidak ada koneksi internet. Menampilkan data tersimpan.",
                com.google.android.material.snackbar.Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun setupDateHeader() {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val hariIni = when (dayOfWeek) {
            Calendar.MONDAY -> "Senin"
            Calendar.TUESDAY -> "Selasa"
            Calendar.WEDNESDAY -> "Rabu"
            Calendar.THURSDAY -> "Kamis"
            Calendar.FRIDAY -> "Jumat"
            Calendar.SATURDAY -> "Sabtu"
            Calendar.SUNDAY -> "Minggu"
            else -> "Senin"
        }

        val tanggal = calendar.get(Calendar.DAY_OF_MONTH)
        val bulan = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(Date())
        binding.tvJadwalHeader.text = "Jadwal hari ini"
        binding.tvDate.text = "$hariIni, $tanggal $bulan"
    }

    private fun setupObservers() {
        viewModel.studentData.observe(viewLifecycleOwner) { student ->
            if (student != null) {
                binding.tvWelcome.text = "Selamat Datang, ${student.name}"
                binding.tvIpk.text = String.format(Locale("id", "ID"), "%.2f", student.ipkKumulatif)
                sharedPref.semester = student.semesterBerjalan
            } else {
                val name = sharedPref.name
                binding.tvWelcome.text = if (name.isNotEmpty()) "Selamat Datang, $name" else "Selamat Datang"
            }
        }

        viewModel.todayCourses.observe(viewLifecycleOwner) { courses ->
            binding.tvJadwalSubtitle.text = "${courses.size} mata kuliah"
            if (courses.isEmpty()) {
                binding.rvCourses.visibility = View.GONE
                binding.cardEmptyJadwal.visibility = View.VISIBLE
                binding.tvEmptyJadwal.text = "Tidak ada jadwal kuliah hari ini"
            } else {
                binding.rvCourses.visibility = View.VISIBLE
                binding.cardEmptyJadwal.visibility = View.GONE
                courseAdapter.updateData(courses)
            }
        }

        viewModel.pengumuman.observe(viewLifecycleOwner) { list ->
            if (list.isNotEmpty()) {
                binding.rvPengumumanPreview.visibility = View.VISIBLE
                binding.tvEmptyPengumuman.visibility = View.GONE
                val previewList = list.take(3)
                pengumumanAdapter = PengumumanAdapter(previewList)
                binding.rvPengumumanPreview.adapter = pengumumanAdapter
            } else if (viewModel.pengumumanError.value == null) {
                binding.rvPengumumanPreview.visibility = View.GONE
                binding.tvEmptyPengumuman.visibility = View.VISIBLE
                binding.tvEmptyPengumuman.text = "Tidak ada pengumuman"
            }
        }

        viewModel.pengumumanError.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e("DashboardFragment", "Pengumuman error: $it")
                binding.rvPengumumanPreview.visibility = View.GONE
                binding.tvEmptyPengumuman.visibility = View.VISIBLE
                binding.tvEmptyPengumuman.text = "Gagal memuat pengumuman"
                val message = if (it.contains("PERMISSION_DENIED", ignoreCase = true)) {
                    "Gagal memuat pengumuman. Coba login ulang."
                } else {
                    "Gagal memuat pengumuman: ${it.take(80)}"
                }
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    message,
                    com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                ).show()
                viewModel.clearPengumumanError()
            }
        }

        viewModel.unreadNotificationCount.observe(viewLifecycleOwner) { count ->
            binding.viewNotificationBadge.visibility = if (count > 0) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Optional: show/hide loading indicator
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Log.e("DashboardFragment", "Firestore error: $it")
                if (it.contains("FAILED_PRECONDITION") || it.contains("requires an index")) {
                    Log.e("SIAKAD_INDEX", "=== PERLU DEPLOY FIRESTORE INDEX ===")
                    Log.e("SIAKAD_INDEX", "Jalankan: firebase deploy --only firestore:indexes")
                }
                com.google.android.material.snackbar.Snackbar.make(
                    binding.root,
                    "Gagal memuat data dashboard",
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show()
                viewModel.clearError()
            }
        }
    }

    private fun loadDashboardData() {
        val npm = sharedPref.npm
        val semester = sharedPref.semester
        if (npm.isNotEmpty()) {
            viewModel.loadAllDashboardData(npm, semester)
            viewModel.checkNotifications(
                npm,
                sharedPref.getLastSeenPengumuman(),
                sharedPref.getLastSeenTagihan(),
                sharedPref.getLastSeenPresensi()
            )
        } else {
            Log.e("DashboardFragment", "NPM kosong, tidak bisa memuat data")
        }
    }

    private fun refreshDashboardData() {
        loadDashboardData()
    }

    private fun setupScanPresensiButton() {
        binding.cardScanPresensi.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchScanner()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchScanner() {
        val intent = Intent(requireContext(), QrScannerActivity::class.java)
        scanResultLauncher.launch(intent)
    }

    private fun setupCourseRecyclerView() {
        courseAdapter = CourseAdapter(emptyList())
        binding.rvCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = courseAdapter
        }
    }

    private fun setupPengumumanPreview() {
        pengumumanAdapter = PengumumanAdapter(emptyList())
        binding.rvPengumumanPreview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pengumumanAdapter
        }

        binding.tvViewAllAnnouncements.setOnClickListener {
            showNotificationBottomSheet()
        }
    }

    private fun setupNotificationIcon() {
        binding.flNotificationIcon.setOnClickListener {
            showNotificationBottomSheet()
        }
    }

    private fun showNotificationBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val sheetBinding = com.widyatama.siakad.databinding.BottomSheetNotificationsBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(sheetBinding.root)

        val notifications = viewModel.notifications.value ?: emptyList()
        if (notifications.isEmpty()) {
            sheetBinding.rvNotifications.visibility = View.GONE
            sheetBinding.tvEmptyNotifications.visibility = View.VISIBLE
        } else {
            sheetBinding.rvNotifications.apply {
                layoutManager = LinearLayoutManager(requireContext())
                notificationAdapter = NotificationAdapter(notifications) { item ->
                    // Handle notification click
                    when (item.type) {
                        com.widyatama.siakad.data.model.NotificationType.PENGUMUMAN -> {
                            // Stay in bottom sheet or show detail
                        }
                        com.widyatama.siakad.data.model.NotificationType.TAGIHAN -> {
                            // Navigate to tagihan tab
                        }
                        com.widyatama.siakad.data.model.NotificationType.PRESENSI -> {
                            // Navigate to schedule
                        }
                    }
                }
                adapter = notificationAdapter
            }
            sheetBinding.tvEmptyNotifications.visibility = View.GONE
        }

        sheetBinding.tvMarkAllRead.setOnClickListener {
            viewModel.markAllAsRead(sharedPref)
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    private fun showProfilePopup(anchor: View) {
        val popup = PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.profile_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menuLogout -> {
                    logout()
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun logout() {
        FirestoreManager.getInstance().signOut(requireContext())

        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
