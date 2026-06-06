package com.widyatama.siakad.ui.biodata

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.widyatama.siakad.R
import com.widyatama.siakad.data.local.SharedPrefManager
import com.widyatama.siakad.data.model.Student
import com.widyatama.siakad.databinding.FragmentBiodataBinding

class BiodataFragment : Fragment() {

    private var _binding: FragmentBiodataBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BiodataViewModel by viewModels()
    private val sharedPref by lazy { SharedPrefManager.getInstance(requireContext()) }

    private var currentStudent: Student? = null

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

        setupObservers()
        setupClickListeners()

        val npm = sharedPref.npm
        if (npm.isNotEmpty()) {
            viewModel.loadBiodata(npm)
        }
    }

    private fun setupObservers() {
        viewModel.studentData.observe(viewLifecycleOwner) { student ->
            student?.let {
                currentStudent = it
                bindDataToUI(it)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Loading state handled by disabling save button
            binding.btnSave.isEnabled = !isLoading
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }

        viewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Biodata berhasil disimpan!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun bindDataToUI(student: Student) {
        binding.tvBiodataName.text = student.name.ifEmpty { "Nama Mahasiswa" }
        binding.tvBiodataNim.text = student.npm.ifEmpty { "-" }
        binding.etKtp.setText(student.ktp)
        binding.etGender.setText(student.gender)
        binding.etBirth.setText(student.birthDate)
        binding.etReligion.setText(student.religion)
        binding.etNationality.setText(student.nationality)
        binding.etEmail.setText(student.personalEmail)
        binding.etPhone.setText(student.phone)
        binding.etAddress.setText(student.address)
    }

    private fun setupClickListeners() {
        binding.btnBack?.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnSave.setOnClickListener {
            saveBiodata()
        }

        binding.btnReset.setOnClickListener {
            currentStudent?.let { bindDataToUI(it) }
            Toast.makeText(requireContext(), "Data dikembalikan ke semula", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveBiodata() {
        val current = currentStudent ?: return

        val updatedStudent = current.copy(
            ktp = binding.etKtp.text.toString().trim(),
            gender = binding.etGender.text.toString().trim(),
            birthDate = binding.etBirth.text.toString().trim(),
            religion = binding.etReligion.text.toString().trim(),
            nationality = binding.etNationality.text.toString().trim(),
            personalEmail = binding.etEmail.text.toString().trim(),
            phone = binding.etPhone.text.toString().trim(),
            address = binding.etAddress.text.toString().trim()
        )

        viewModel.saveBiodata(updatedStudent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
