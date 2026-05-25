package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentAcademicHistoryBinding

class AcademicHistoryFragment : Fragment() {

    private var _binding: FragmentAcademicHistoryBinding? = null
    private val binding get() = _binding!!

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

        setupUI()
        setupListeners()
    }

    private fun setupUI() {
        binding.tvGpaValue.text = UserSession.gpa
        binding.tvSksValue.text = UserSession.sks
        binding.tvSelectedSemester.text = UserSession.semester
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Show Overlay when clicking semester selector or filter button
        binding.spinnerSemester.setOnClickListener {
            showFilterOverlay()
        }

        binding.btnFilter.setOnClickListener {
            showFilterOverlay()
        }

        // Overlay options
        binding.optionGenap.setOnClickListener {
            updateSelectedSemester(getString(R.string.sem_genap_24_25))
            hideFilterOverlay()
        }

        binding.optionGanjil.setOnClickListener {
            updateSelectedSemester(getString(R.string.sem_ganjil_25_26))
            hideFilterOverlay()
        }

        binding.optionSemua.setOnClickListener {
            updateSelectedSemester(getString(R.string.sem_all))
            hideFilterOverlay()
        }

        binding.btnCloseOverlay.setOnClickListener {
            hideFilterOverlay()
        }

        binding.overlayBackground.setOnClickListener {
            hideFilterOverlay()
        }
        
        binding.btnReset.setOnClickListener {
            updateSelectedSemester("Genap 2025/2026")
        }
    }

    private fun showFilterOverlay() {
        binding.overlayBackground.visibility = View.VISIBLE
        binding.cardFilterOverlay.visibility = View.VISIBLE
        // Optional: Add animation
    }

    private fun hideFilterOverlay() {
        binding.overlayBackground.visibility = View.GONE
        binding.cardFilterOverlay.visibility = View.GONE
    }

    private fun updateSelectedSemester(semester: String) {
        UserSession.semester = semester
        binding.tvSelectedSemester.text = semester
        // Here you would typically trigger a data reload based on the filter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
