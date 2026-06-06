package com.widyatama.siakad.ui.schedule

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.widyatama.siakad.R
import com.widyatama.siakad.adapter.CourseAdapter
import com.widyatama.siakad.adapter.SemesterAdapter
import com.widyatama.siakad.core.utils.NetworkUtils
import com.widyatama.siakad.data.local.SharedPrefManager
import com.widyatama.siakad.databinding.FragmentScheduleBinding
import com.widyatama.siakad.data.model.Course

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var courseAdapter: CourseAdapter
    private val viewModel: ScheduleViewModel by viewModels()
    private var allCourses: List<Course> = emptyList()

    private val prefs by lazy {
        requireActivity().getSharedPreferences("SIAKAD_PREFS", Context.MODE_PRIVATE)
    }
    private val sharedPref by lazy { SharedPrefManager.getInstance(requireContext()) }
    private var selectedSemester: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupSemesterSelector()
        setupRecyclerView()
        setupDayChips()
        setupObservers()
        checkNetworkStatus()
        loadCourses()

        binding.chipMonday.performClick()
    }

    private fun setupObservers() {
        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            allCourses = courses
            showLoading(false)
            showEmptyState(courses.isEmpty())
            if (courses.isNotEmpty()) {
                filterCoursesByDay("Senin")
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupUI()
    }

    private fun setupUI() {
        selectedSemester = prefs.getInt("CURRENT_SEMESTER", 1)
        binding.tvCurrentSemester.text = "Semester $selectedSemester"
    }

    private fun setupSemesterSelector() {
        binding.cardSemesterSelector.setOnClickListener {
            showSemesterBottomSheet()
        }
    }

    private fun showSemesterBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val sheetBinding = com.widyatama.siakad.databinding.BottomSheetSemesterBinding.inflate(layoutInflater)
        bottomSheetDialog.setContentView(sheetBinding.root)

        // Build list 1..semesterBerjalan
        val semesterBerjalan = sharedPref.semester.coerceAtLeast(1)
        val semesterList = (1..semesterBerjalan).toList()

        sheetBinding.rvSemesterOptions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SemesterAdapter(semesterList, selectedSemester) { chosenSemester ->
                selectedSemester = chosenSemester
                prefs.edit().putInt("CURRENT_SEMESTER", chosenSemester).apply()
                binding.tvCurrentSemester.text = "Semester $chosenSemester"
                loadCourses()
                bottomSheetDialog.dismiss()
            }
        }

        bottomSheetDialog.show()
    }

    private fun setupRecyclerView() {
        courseAdapter = CourseAdapter(emptyList())
        binding.rvCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = courseAdapter
        }
    }

    private fun setupDayChips() {
        val chips = mapOf(
            binding.chipMonday to "Senin",
            binding.chipTuesday to "Selasa",
            binding.chipWednesday to "Rabu",
            binding.chipThursday to "Kamis",
            binding.chipFriday to "Jumat",
            binding.chipSaturday to "Sabtu"
        )

        chips.forEach { (chip, day) ->
            chip.setOnClickListener {
                updateActiveChip(chip.id)
                filterCoursesByDay(day)
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

    private fun loadCourses() {
        showLoading(true)
        val npm = sharedPref.npm
        viewModel.loadCourses(npm, selectedSemester)
    }

    private fun filterCoursesByDay(day: String) {
        val courses = allCourses.filter { it.hari.equals(day, ignoreCase = true) }
        courseAdapter.updateData(courses)
        updateSummary(courses)
        showEmptyState(courses.isEmpty() && allCourses.isNotEmpty())
    }

    private fun showEmptyState(show: Boolean) {
        binding.rvCourses.visibility = if (show) View.GONE else View.VISIBLE
        binding.layoutEmptyState.visibility = if (show) View.VISIBLE else View.GONE
        if (show) {
            binding.tvTotalCredits.text = "0 SKS"
            binding.tvNextCourseLabel.text = "Tidak ada jadwal"
            binding.tvNextCourseTime.text = "-"
        }
    }

    private fun updateActiveChip(selectedChipId: Int) {
        val chipIds = listOf(
            binding.chipMonday.id,
            binding.chipTuesday.id,
            binding.chipWednesday.id,
            binding.chipThursday.id,
            binding.chipFriday.id,
            binding.chipSaturday.id
        )

        chipIds.forEach { id ->
            val chip = binding.root.findViewById<com.google.android.material.chip.Chip>(id)
            if (id == selectedChipId) {
                chip.setChipBackgroundColorResource(R.color.navy_blue)
                chip.setTextColor(resources.getColor(R.color.white, null))
            } else {
                chip.setChipBackgroundColorResource(R.color.light_gray)
                chip.setTextColor(resources.getColor(R.color.text_secondary, null))
            }
        }
    }

    private fun updateSummary(courses: List<Course>) {
        val totalCredits = courses.sumOf { it.sks }
        binding.tvTotalCredits.text = "$totalCredits SKS"

        if (courses.isNotEmpty()) {
            val firstCourse = courses[0]
            binding.tvNextCourseLabel.text = "Next: ${firstCourse.name}"
            binding.tvNextCourseTime.text = firstCourse.jamMulai.split(" ")[0] + " AM"
        } else {
            binding.tvNextCourseLabel.text = "No classes today"
            binding.tvNextCourseTime.text = "-"
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.rvCourses.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
