package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.CourseAdapter
import com.example.myapplication.data.CourseData
import com.example.myapplication.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

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
        
        binding.cardScanPresensi.setOnClickListener {
            val scanDialog = ScanDialogFragment.newInstance()
            scanDialog.show(childFragmentManager, ScanDialogFragment.TAG)
        }

        displayWelcomeMessage()
        setupCourseRecyclerView()
    }

    private fun displayWelcomeMessage() {
        val username = arguments?.getString("USER_NAME")
        if (!username.isNullOrEmpty()) {
            binding.tvWelcome.text = "Selamat Datang, $username"
        }
    }

    private fun setupCourseRecyclerView() {
        val courses = CourseData.getCourses()
        binding.rvCourses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = CourseAdapter(courses)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}