package com.widyatama.siakad.ui.results

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.widyatama.siakad.adapter.ResultAdapter
import com.widyatama.siakad.data.remote.FirestoreManager
import com.widyatama.siakad.databinding.FragmentResultsBinding
import com.widyatama.siakad.data.model.CourseResult

class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!

    private val firestoreManager = FirestoreManager.getInstance()
    private val prefs by lazy {
        requireActivity().getSharedPreferences("SIAKAD_PREFS", Context.MODE_PRIVATE)
    }

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
        loadAcademicResults()
    }

    private fun setupRecyclerView() {
        binding.rvResultCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ResultAdapter(emptyList())
            isNestedScrollingEnabled = false
        }
    }

    private fun loadAcademicResults() {
        val npm = prefs.getString("NPM", "") ?: ""
        if (npm.isEmpty()) {
            binding.rvResultCourses.adapter = ResultAdapter(emptyList())
            return
        }

        val semester = prefs.getInt("CURRENT_SEMESTER", 1)

        showLoading(true)
        firestoreManager.getAcademicResults(npm, semester) { results, error ->
            activity?.runOnUiThread {
                showLoading(false)
                if (error != null) {
                    binding.rvResultCourses.adapter = ResultAdapter(emptyList())
                    return@runOnUiThread
                }
                val courseResults = results.map { result ->
                    CourseResult(
                        name = result.courseName,
                        sks = result.sks,
                        mutu = result.mutu,
                        grade = result.grade,
                        iconRes = null
                    )
                }
                binding.rvResultCourses.adapter = ResultAdapter(courseResults)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.rvResultCourses.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
