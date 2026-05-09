package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.CourseAdapter
import com.example.myapplication.data.CourseData
import com.example.myapplication.databinding.FragmentScheduleBinding
import com.example.myapplication.model.Course

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!
    private lateinit var courseAdapter: CourseAdapter

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
        
        // Default to Senin
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
                val courses = CourseData.getCoursesByDay(day)
                courseAdapter.updateData(courses)
                updateSummary(courses)
            }
        }
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
            binding.tvNextCourseTime.text = firstCourse.time.split(" ")[0] + " AM"
        } else {
            binding.tvNextCourseLabel.text = "No classes today"
            binding.tvNextCourseTime.text = "-"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}