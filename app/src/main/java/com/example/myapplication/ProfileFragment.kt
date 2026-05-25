package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        displayUserData()

        binding.btnSettingsTop.setOnClickListener {
            navigateToSettings()
        }

        binding.btnAccountSettings.setOnClickListener {
            navigateToSettings()
        }

        binding.btnBiodata.setOnClickListener {
            navigateToBiodata()
        }

        binding.btnRiwayatAkademik.setOnClickListener {
            navigateToRiwayatAkademik()
        }
    }

    override fun onResume() {
        super.onResume()
        displayUserData()
    }

    private fun displayUserData() {
        binding.tvProfileName.text = UserSession.name.replace(" ", "\n")
        binding.tvProfileNim.text = "NIM: ${UserSession.nim}"
        binding.tvProfileMajor.text = UserSession.major
        binding.tvProfileGpa.text = UserSession.gpa
        binding.tvProfileSks.text = UserSession.sks
        binding.tvProfileSemester.text = UserSession.semester
    }

    private fun navigateToBiodata() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, BiodataFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToRiwayatAkademik() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, AcademicHistoryFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToSettings() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, SettingsFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}