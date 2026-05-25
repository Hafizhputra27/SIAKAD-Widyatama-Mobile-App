package com.widyatama.siakad.ui.schedule

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.widyatama.siakad.R
import com.widyatama.siakad.adapter.CourseAdapter
import com.widyatama.siakad.data.remote.FirestoreManager
import com.widyatama.siakad.databinding.FragmentScheduleBinding
import com.widyatama.siakad.data.model.Course

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var courseAdapter: CourseAdapter
    private val firestoreManager = FirestoreManager.getInstance()
    private var allCourses: List<Course> = emptyList()

    private val prefs by lazy {
        requireActivity().getSharedPreferences("SIAKAD_PREFS", Context.MODE_PRIVATE)
    }

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
        setupRecyclerView()
        setupDayChips()
        loadCoursesFromFirestore()
        
        binding.chipMonday.performClick()
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
            binding.chipFriday to "Jumat"
        )

        chips.forEach { (chip, day) ->
            chip.setOnClickListener {
                updateActiveChip(chip.id)
                filterCoursesByDay(day)
            }
        }
    }

    private fun loadCoursesFromFirestore() {
        showLoading(true)
        val semester = prefs.getInt("CURRENT_SEMESTER", 1)
        firestoreManager.getAllCoursesForSemester(semester) { courses, error ->
            activity?.runOnUiThread {
                showLoading(false)
                if (error != null) {
                    allCourses = emptyList()
                    courseAdapter.updateData(emptyList())
                    updateSummary(emptyList())
                } else {
                    allCourses = courses
                    filterCoursesByDay("Senin")
                }
            }
        }
    }

    private fun filterCoursesByDay(day: String) {
        val courses = allCourses.filter { it.hari.equals(day, ignoreCase = true) }
        courseAdapter.updateData(courses)
        updateSummary(courses)
    }

    private fun updateActiveChip(selectedChipId: Int) {
        val chipIds = listOf(
            binding.chipMonday.id,
            binding.chipTuesday.id,
            binding.chipWednesday.id,
            binding.chipThursday.id,
            binding.chipFriday.id
        )

        chipIds.forEach { id ->
            val chip = binding.root.findViewById<com.google.android.material.chip.Chip>(id)
            if (id == selectedChipId) {
                chip.setChipBackgroundColorResource(R.color.navy_blue)
                chip.setTextColor(resources.getColor(R.color.white, null))
            } else {
                chip.setChipBackgroundColorResource(R.color.light_gray)
                chip.setTextColor(resources.getColor(R.color.navy_blue, null))
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
