package com.widyatama.siakad.ui.results

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.widyatama.siakad.R
import com.widyatama.siakad.adapter.ResultAdapter
import com.widyatama.siakad.core.utils.NetworkUtils
import com.widyatama.siakad.data.local.SharedPrefManager
import com.widyatama.siakad.data.model.AcademicResult
import com.widyatama.siakad.databinding.FragmentResultsBinding
import java.util.Locale

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResultsViewModel by viewModels()
    private val sharedPref by lazy { SharedPrefManager.getInstance(requireContext()) }

    private var allResultsBySemester: Map<Int, List<AcademicResult>> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        checkNetworkStatus()
        loadAcademicData()
    }

    private fun setupRecyclerView() {
        binding.rvResultCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ResultAdapter(emptyList())
            isNestedScrollingEnabled = false
        }
    }

    private fun setupObservers() {
        viewModel.student.observe(viewLifecycleOwner) { student ->
            student?.let { updateSummaryUI(it) }
        }

        viewModel.academicResults.observe(viewLifecycleOwner) { bySemester ->
            showLoading(false)
            allResultsBySemester = bySemester

            if (bySemester.isEmpty()) {
                showEmptyState("Belum ada data nilai.")
                return@observe
            }

            setupSemesterChips(bySemester)

            val latestSemester = bySemester.keys.first()
            showResultsForSemester(bySemester[latestSemester] ?: emptyList())
            updateSemesterStats(bySemester[latestSemester] ?: emptyList())
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                showLoading(false)
                showEmptyState(it)
                viewModel.clearError()
            }
        }
    }

    private fun checkNetworkStatus() {
        if (!NetworkUtils.isConnected(requireContext())) {
            Snackbar.make(
                binding.root,
                "Tidak ada koneksi internet. Menampilkan data tersimpan.",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun loadAcademicData() {
        val npm = sharedPref.npm
        if (npm.isEmpty()) {
            showEmptyState("NPM tidak ditemukan. Silakan login ulang.")
            return
        }
        showLoading(true)
        viewModel.loadResults(npm)
    }

    private fun updateSummaryUI(student: com.widyatama.siakad.data.model.Student) {
        val ipk = student.ipkKumulatif
        binding.tvCgpa.text = String.format(Locale("id", "ID"), "%.2f", ipk)

        val totalSks = student.totalSksLulus
        val targetSks = student.totalSksTarget
        binding.tvTotalCredits.text = "$totalSks / $targetSks"

        val progress = ((ipk / 4.0) * 100).toInt().coerceIn(0, 100)
        binding.progressSemester.progress = progress
        binding.tvProgressPercentage.text = "$progress%"

        val ipkColorRes = when {
            ipk >= 3.5 -> R.color.green_success
            ipk >= 3.0 -> R.color.blue_light
            ipk >= 2.5 -> R.color.orange_accent
            else -> R.color.red_error
        }
        binding.tvCgpa.setTextColor(ContextCompat.getColor(requireContext(), ipkColorRes))

        binding.tvStatusBadge.text = student.status
        val statusColor = if (student.status.equals("AKTIF", ignoreCase = true)) {
            "#22C55E"
        } else {
            "#9CA3AF"
        }
        binding.tvStatusBadge.setTextColor(android.graphics.Color.parseColor(statusColor))

        val motivasi = when {
            ipk >= 3.5 -> "Luar biasa! IPK Anda sangat memuaskan. Pertahankan prestasi ini."
            ipk >= 3.0 -> "Bagus! IPK Anda di atas rata-rata. Terus tingkatkan."
            ipk >= 2.5 -> "Cukup baik. Ada ruang untuk peningkatan. Semangat!"
            else -> "IPK perlu ditingkatkan. Konsultasikan dengan dosen pembimbing."
        }
        binding.tvMotivasi.text = motivasi
    }

    private fun setupSemesterChips(bySemester: Map<Int, List<AcademicResult>>) {
        binding.chipGroupSemester.removeAllViews()

        bySemester.keys.forEachIndexed { index, semester ->
            val chip = Chip(requireContext()).apply {
                text = "Semester $semester"
                isCheckable = true
                isChecked = (index == 0)

                if (isChecked) {
                    setChipBackgroundColorResource(R.color.navy_blue)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                } else {
                    setChipBackgroundColorResource(R.color.light_gray)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                }

                setOnClickListener {
                    for (i in 0 until binding.chipGroupSemester.childCount) {
                        val child = binding.chipGroupSemester.getChildAt(i) as Chip
                        child.setChipBackgroundColorResource(R.color.light_gray)
                        child.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                    }
                    setChipBackgroundColorResource(R.color.navy_blue)
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

                    showResultsForSemester(bySemester[semester] ?: emptyList())
                    updateSemesterStats(bySemester[semester] ?: emptyList())
                }
            }
            binding.chipGroupSemester.addView(chip)
        }
    }

    private fun updateSemesterStats(results: List<AcademicResult>) {
        val semSks = results.sumOf { it.sks }
        val semGpa = if (results.isNotEmpty() && semSks > 0) {
            results.sumOf { it.mutu * it.sks } / semSks.toDouble()
        } else 0.0

        binding.tvSemSks.text = semSks.toString()
        binding.tvSemGpa.text = String.format(Locale("id", "ID"), "%.2f", semGpa)
    }

    private fun showResultsForSemester(results: List<AcademicResult>) {
        binding.rvResultCourses.adapter = ResultAdapter(results)
    }

    private fun showEmptyState(message: String) {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.tvEmptyMessage.text = message
        binding.rvResultCourses.visibility = View.GONE
        binding.chipGroupSemester.visibility = View.GONE
    }

    private fun showLoading(isLoading: Boolean) {
        binding.rvResultCourses.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
