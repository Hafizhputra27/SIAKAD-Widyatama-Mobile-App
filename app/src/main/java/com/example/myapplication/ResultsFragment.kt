package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.ResultAdapter
import com.example.myapplication.databinding.FragmentResultsBinding
import com.example.myapplication.model.CourseResult

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

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
    }

    private fun setupRecyclerView() {
        val results = listOf(
            CourseResult("Pendidikan Kewarganegaraan", 2, 8.0, "A", R.drawable.ic_calendar),
            CourseResult("Dasar Sistem Operasi", 3, 10.5, "B+", R.drawable.ic_settings),
            CourseResult("Struktur Data & Algoritma", 4, 16.0, "A", R.drawable.ic_calendar),
            CourseResult("Bahasa Inggris Teknik", 2, 8.0, "A", R.drawable.ic_calendar)
        )

        binding.rvResultCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ResultAdapter(results)
            isNestedScrollingEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}