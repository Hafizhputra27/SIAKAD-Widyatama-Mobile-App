package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.CourseAdapter
import com.example.myapplication.data.CourseData
import com.example.myapplication.databinding.FragmentDashboardBinding

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireActivity().getSharedPreferences("SIAKAD_PREFS", Context.MODE_PRIVATE)
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
        displayWelcomeMessage()
        setupCourseRecyclerView()
        setupProfileMenu()
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

    private fun setupProfileMenu() {
        binding.ivProfile.setOnClickListener { view ->
            showProfilePopup(view)
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