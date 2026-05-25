package com.widyatama.siakad.ui.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.widyatama.siakad.R
import com.widyatama.siakad.data.remote.FirestoreManager
import com.widyatama.siakad.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.widyatama.siakad.ui.auth.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val prefs by lazy {
        requireActivity().getSharedPreferences("SIAKAD_PREFS", Context.MODE_PRIVATE)
    }

    private val firestoreManager by lazy { FirestoreManager.getInstance() }

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

        setupViews()
        loadProfileData()
    }

    private fun loadProfileData() {
        val npm = prefs.getString("NPM", "") ?: ""
        if (npm.isEmpty()) return

        firestoreManager.getMahasiswa(npm) { student, error ->
            if (error != null || student == null) {
                Toast.makeText(requireContext(), "Gagal memuat data profil", Toast.LENGTH_SHORT).show()
                return@getMahasiswa
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                binding.tvProfileName.text = student.name
                binding.tvProfileNpm.text = student.npm
                binding.tvProfileMajor.text = student.major
                binding.tvProfileAngkatan.text = "ANGKATAN ${student.angkatan}"
                binding.tvBadgeAktif.text = student.status
                binding.tvBadgeReguler.text = student.kelas
                binding.tvIpkValue.text = String.format("%.2f", student.ipkKumulatif)
                binding.tvSksValue.text = "${student.totalSksLulus}/${student.totalSksTarget}"
                binding.tvSemesterValue.text = student.semesterBerjalan.toString()

                val ipkProgress = ((student.ipkKumulatif / 4.0) * 100).toInt()
                binding.pbIpk.progress = ipkProgress

                if (student.photoUrl.isNotEmpty()) {
                    Glide.with(this@ProfileFragment)
                        .load(student.photoUrl)
                        .circleCrop()
                        .placeholder(com.widyatama.siakad.R.drawable.ic_person)
                        .into(binding.ivAvatar)
                }
            }
        }
    }

    private fun setupViews() {
        binding.ivSettingsTop.setOnClickListener {
            navigateToSettings()
        }

        binding.itemMenuUbahPassword.setOnClickListener {
            navigateToSettings()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Konfirmasi Keluar")
            .setMessage("Apakah Anda yakin ingin keluar dari sesi ini?")
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Keluar") { _, _ ->
                performLogout()
            }
            .setCancelable(true)
            .show()
    }

    private fun performLogout() {
        prefs.edit().clear().apply()

        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
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
