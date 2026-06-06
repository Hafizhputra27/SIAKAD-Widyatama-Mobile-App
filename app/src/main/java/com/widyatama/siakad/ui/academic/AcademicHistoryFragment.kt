package com.widyatama.siakad.ui.academic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.widyatama.siakad.data.local.SharedPrefManager
import com.widyatama.siakad.data.model.AcademicResult
import com.widyatama.siakad.data.model.TranscriptSummary
import com.widyatama.siakad.databinding.FragmentAcademicHistoryBinding

class AcademicHistoryFragment : Fragment() {

    private var _binding: FragmentAcademicHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AcademicHistoryViewModel by viewModels()
    private val sharedPref by lazy { SharedPrefManager.getInstance(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAcademicHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTabLayout()
        setupClickListeners()
        setupObservers()

        val npm = sharedPref.npm
        val semester = sharedPref.semester
        if (npm.isNotEmpty()) {
            viewModel.loadAcademicData(npm, semester)
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> showUTSData()
                    1 -> showUASData()
                    2 -> showTranscriptData()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupClickListeners() {
        binding.btnBack?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnFilter?.setOnClickListener {
            showFilterOverlay()
        }

        binding.btnCloseOverlay?.setOnClickListener {
            hideFilterOverlay()
        }

        binding.optionGenap?.setOnClickListener {
            selectSemester("Genap 2024/2025")
        }

        binding.optionGanjil?.setOnClickListener {
            selectSemester("Ganjil 2025/2026")
        }

        binding.optionSemua?.setOnClickListener {
            selectSemester("Semua Semester")
            val npm = sharedPref.npm
            if (npm.isNotEmpty()) {
                viewModel.loadAllSemesters(npm)
            }
            hideFilterOverlay()
        }
    }

    private fun setupObservers() {
        viewModel.utsResults.observe(viewLifecycleOwner) { results ->
            // Update table with UTS data
            // For now, show in a simple way - in real app, populate the TableLayout
            if (results.isEmpty() && binding.tabLayout.selectedTabPosition == 0) {
                showEmptyState("Tidak ada data UTS")
            } else if (binding.tabLayout.selectedTabPosition == 0) {
                showDataInTable(results)
            }
        }

        viewModel.uasResults.observe(viewLifecycleOwner) { results ->
            if (results.isEmpty() && binding.tabLayout.selectedTabPosition == 1) {
                showEmptyState("Tidak ada data UAS")
            } else if (binding.tabLayout.selectedTabPosition == 1) {
                showDataInTable(results)
            }
        }

        viewModel.transcriptSummaries.observe(viewLifecycleOwner) { summaries ->
            if (summaries.isEmpty() && binding.tabLayout.selectedTabPosition == 2) {
                showEmptyState("Tidak ada data transkrip")
            } else if (binding.tabLayout.selectedTabPosition == 2) {
                showTranscriptData(summaries)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Loading state - can be extended with a progress indicator if needed
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun showUTSData() {
        val results = viewModel.utsResults.value ?: emptyList()
        if (results.isEmpty()) {
            showEmptyState("Tidak ada data UTS")
        } else {
            showDataInTable(results)
        }
    }

    private fun showUASData() {
        val results = viewModel.uasResults.value ?: emptyList()
        if (results.isEmpty()) {
            showEmptyState("Tidak ada data UAS")
        } else {
            showDataInTable(results)
        }
    }

    private fun showTranscriptData() {
        val summaries = viewModel.transcriptSummaries.value ?: emptyList()
        if (summaries.isEmpty()) {
            showEmptyState("Tidak ada data transkrip")
        } else {
            showTranscriptData(summaries)
        }
    }

    private fun showDataInTable(results: List<AcademicResult>) {
        // In a real implementation, dynamically populate the TableLayout
        // For now, update summary stats
        updateSummaryStats(results)
    }

    private fun showTranscriptData(summaries: List<TranscriptSummary>) {
        // Show transcript data aggregated from academic_results
        if (summaries.isNotEmpty()) {
            val first = summaries.first() // Highest semester first (sorted descending)
            binding.tvGpaValue?.text = String.format(java.util.Locale("id", "ID"), "%.2f", first.ipkSemester)
            binding.tvSksValue?.text = "${first.sksSemester}"
        }
    }

    private fun updateSummaryStats(results: List<AcademicResult>) {
        if (results.isNotEmpty()) {
            val totalSks = results.sumOf { it.sks }
            val totalMutu = results.sumOf { it.mutu }
            val ipk = if (totalSks > 0) totalMutu.toDouble() / totalSks else 0.0
            binding.tvGpaValue?.text = String.format(java.util.Locale("id", "ID"), "%.2f", ipk)
            binding.tvSksValue?.text = "$totalSks"
        }
    }

    private fun showEmptyState(message: String) {
        // Show empty state UI
        binding.tvGpaValue?.text = "0.00"
        binding.tvSksValue?.text = "0"
    }

    private fun showFilterOverlay() {
        binding.overlayBackground?.visibility = View.VISIBLE
        binding.cardFilterOverlay?.visibility = View.VISIBLE
    }

    private fun hideFilterOverlay() {
        binding.overlayBackground?.visibility = View.GONE
        binding.cardFilterOverlay?.visibility = View.GONE
    }

    private fun selectSemester(semesterName: String) {
        binding.tvSelectedSemester?.text = semesterName
        hideFilterOverlay()

        val npm = sharedPref.npm
        if (npm.isNotEmpty()) {
            // Parse semester from name or load all
            viewModel.loadAllSemesters(npm)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}