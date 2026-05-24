package com.widyatama.siakad.ui.dashboard

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.widyatama.siakad.R
import com.widyatama.siakad.adapter.CourseAdapter
import com.widyatama.siakad.data.remote.FirestoreManager
import com.widyatama.siakad.databinding.FragmentDashboardBinding
import com.widyatama.siakad.data.model.Course
import com.widyatama.siakad.ui.attendance.QrScannerActivity
import com.widyatama.siakad.ui.auth.LoginActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireActivity().getSharedPreferences("SIAKAD_PREFS", Context.MODE_PRIVATE)
    }

    private val firestoreManager = FirestoreManager.getInstance()
    private lateinit var courseAdapter: CourseAdapter

    private var currentScheduleZoom = 100
    private val minZoom = 80
    private val maxZoom = 150
    private val zoomStep = 10

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
                loadJadwalHariIni()
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
        displayWelcomeMessage()
        setupCourseRecyclerView()
        setupScheduleZoom()
        loadJadwalHariIni()
        setupScanPresensiButton()
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

    private fun displayWelcomeMessage() {
        val npm = prefs.getString("NPM", "") ?: ""
        if (npm.isNotEmpty()) {
            firestoreManager.getMahasiswa(npm) { student, error ->
                activity?.runOnUiThread {
                    if (error != null || student == null) {
                        Log.e("DashboardFragment", "Failed to load student: $error")
                        val username = arguments?.getString("USER_NAME")
                        if (!username.isNullOrEmpty()) {
                            binding.tvWelcome.text = "Selamat Datang, $username"
                        }
                    } else {
                        binding.tvWelcome.text = "Selamat Datang, ${student.name}"
                        binding.tvIpk.text = String.format("%.2f", student.ipkKumulatif)
                        prefs.edit().putInt("CURRENT_SEMESTER", student.semesterBerjalan).apply()
                    }
                }
            }
        } else {
            val username = arguments?.getString("USER_NAME")
            if (!username.isNullOrEmpty()) {
                binding.tvWelcome.text = "Selamat Datang, $username"
            }
        }
    }

    private fun setupScheduleZoom() {
        binding.cardZoomSchedule.setOnClickListener {
            showZoomOptions()
        }
    }

    private fun showZoomOptions() {
        val popup = PopupMenu(requireContext(), binding.cardZoomSchedule)
        popup.menu.add(0, 1, 0, "80% (Small)")
        popup.menu.add(0, 2, 1, "100% (Normal)")
        popup.menu.add(0, 3, 2, "120% (Large)")
        popup.menu.add(0, 4, 3, "150% (Extra Large)")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> setScheduleZoom(80)
                2 -> setScheduleZoom(100)
                3 -> setScheduleZoom(120)
                4 -> setScheduleZoom(150)
            }
            true
        }

        popup.show()
    }

    private fun setScheduleZoom(zoom: Int) {
        currentScheduleZoom = zoom.coerceIn(minZoom, maxZoom)
        binding.tvScheduleZoomLevel.text = "$currentScheduleZoom%"

        val scale = currentScheduleZoom / 100f
        binding.rvCourses.scaleX = scale
        binding.rvCourses.scaleY = scale

        binding.ivZoomSchedule.setImageResource(
            when {
                currentScheduleZoom <= 100 -> R.drawable.ic_zoom_in
                currentScheduleZoom <= 120 -> R.drawable.ic_zoom_in
                else -> R.drawable.ic_zoom_in
            }
        )
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

    private fun loadJadwalHariIni() {
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

        Log.d("JADWAL_DEBUG", "DashboardFragment: hariIni = '$hariIni', dayOfWeek = $dayOfWeek")

        if (hariIni == "Sabtu" || hariIni == "Minggu") {
            binding.rvCourses.visibility = View.GONE
            binding.cardEmptyJadwal.visibility = View.VISIBLE
            binding.tvEmptyJadwal.text = "Tidak ada jadwal kuliah hari ini"
            binding.tvJadwalSubtitle.text = "0 mata kuliah"
            return
        }

        val npm = prefs.getString("NPM", "") ?: ""
        val semester = prefs.getInt("CURRENT_SEMESTER", 1)

        firestoreManager.getCoursesByDay(hariIni, semester) { courses, error ->
            activity?.runOnUiThread {
                if (error != null) {
                    Log.e("DashboardFragment", "Error loading jadwal: $error")
                    binding.rvCourses.visibility = View.GONE
                    binding.cardEmptyJadwal.visibility = View.VISIBLE
                    binding.tvEmptyJadwal.text = "Gagal memuat jadwal"
                    binding.tvJadwalSubtitle.text = "Error"
                    return@runOnUiThread
                }

                binding.tvJadwalSubtitle.text = "${courses.size} mata kuliah"
                if (courses.isEmpty()) {
                    binding.rvCourses.visibility = View.GONE
                    binding.cardEmptyJadwal.visibility = View.VISIBLE
                    binding.tvEmptyJadwal.text = "Tidak ada jadwal kuliah hari ini"
                    binding.tvJadwalSubtitle.text = "0 mata kuliah"
                } else {
                    binding.rvCourses.visibility = View.VISIBLE
                    binding.cardEmptyJadwal.visibility = View.GONE
                    courseAdapter.updateData(courses)
                    setScheduleZoom(currentScheduleZoom)
                }
            }
        }
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
        prefs.edit().clear().apply()

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
