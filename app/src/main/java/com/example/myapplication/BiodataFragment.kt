package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.FragmentBiodataBinding

class BiodataFragment : Fragment() {

    private var _binding: FragmentBiodataBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBiodataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSave.setOnClickListener {
            saveData()
        }

        binding.btnReset.setOnClickListener {
            resetFields()
        }
    }

    private fun loadUserData() {
        binding.tvBiodataName.text = UserSession.name.replace(" ", "\n")
        binding.tvBiodataNim.text = UserSession.nim
        binding.etKtp.setText(UserSession.ktp)
        binding.etGender.setText(UserSession.gender)
        binding.etBirth.setText(UserSession.birth)
        binding.etReligion.setText(UserSession.religion)
        binding.etNationality.setText(UserSession.nationality)
        binding.etEmail.setText(UserSession.email)
        binding.etPhone.setText(UserSession.phone)
        binding.etAddress.setText(UserSession.address)
    }

    private fun saveData() {
        UserSession.ktp = binding.etKtp.text.toString()
        UserSession.gender = binding.etGender.text.toString()
        UserSession.birth = binding.etBirth.text.toString()
        UserSession.religion = binding.etReligion.text.toString()
        UserSession.nationality = binding.etNationality.text.toString()
        UserSession.email = binding.etEmail.text.toString()
        UserSession.phone = binding.etPhone.text.toString()
        UserSession.address = binding.etAddress.text.toString()

        Toast.makeText(requireContext(), "Biodata berhasil disimpan", Toast.LENGTH_SHORT).show()
        parentFragmentManager.popBackStack()
    }

    private fun resetFields() {
        UserSession.resetToDefault()
        loadUserData()
        Toast.makeText(requireContext(), "Data telah di-reset", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
