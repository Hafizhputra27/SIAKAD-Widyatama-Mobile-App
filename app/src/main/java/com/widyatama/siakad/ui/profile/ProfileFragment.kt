package com.widyatama.siakad.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.widyatama.siakad.R
import com.widyatama.siakad.data.local.SharedPrefManager
import com.widyatama.siakad.data.remote.FirestoreManager
import com.widyatama.siakad.databinding.FragmentProfileBinding
import com.widyatama.siakad.ui.auth.LoginActivity
import com.widyatama.siakad.ui.academic.AcademicHistoryFragment
import com.widyatama.siakad.ui.biodata.BiodataFragment

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val sharedPref by lazy { SharedPrefManager.getInstance(requireContext()) }
    private val firestoreManager by lazy { FirestoreManager.getInstance() }

    private var currentStudent: com.widyatama.siakad.data.model.Student? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            result.data?.data?.let { uploadPhotoToFirebase(it) }
        }
    }

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
        val npm = sharedPref.npm
        if (npm.isEmpty()) return

        firestoreManager.getMahasiswa(npm) { student, error ->
            if (!isAdded || _binding == null) return@getMahasiswa
            if (error != null || student == null) {
                Toast.makeText(requireContext(), "Gagal memuat data profil", Toast.LENGTH_SHORT).show()
                return@getMahasiswa
            }

            currentStudent = student

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                if (_binding == null) return@launchWhenStarted
                binding.tvProfileName.text = student.name
                binding.tvProfileNpm.text = student.npm
                binding.tvProfileMajor.text = student.major
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

        binding.ivEditAvatar.setOnClickListener {
            showPhotoOptions()
        }

        binding.itemMenuBiodata.setOnClickListener {
            navigateToBiodata()
        }

        binding.itemMenuRiwayatAkademik.setOnClickListener {
            navigateToAcademicHistory()
        }

        binding.itemMenuDokumen.setOnClickListener {
            showDokumenDialog()
        }

        binding.itemMenuPengaturanLanjutan.setOnClickListener {
            navigateToSettings()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showPhotoOptions() {
        val options = arrayOf("Pilih dari Galeri", "Hapus Foto")
        AlertDialog.Builder(requireContext())
            .setTitle("Foto Profil")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                    1 -> deleteProfilePhoto()
                }
            }
            .show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }

    private fun uploadPhotoToFirebase(uri: Uri) {
        val npm = sharedPref.npm
        if (npm.isEmpty()) return

        Toast.makeText(requireContext(), "Mengupload foto...", Toast.LENGTH_SHORT).show()

        val storageRef = FirebaseStorage.getInstance()
            .reference.child("photos/mahasiswa/$npm.jpg")

        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    firestoreManager.updatePhotoUrl(npm, downloadUri.toString()) { success ->
                        if (success) {
                            Glide.with(this).load(downloadUri).circleCrop()
                                .placeholder(R.drawable.ic_person).into(binding.ivAvatar)
                            Toast.makeText(requireContext(), "Foto berhasil diupdate", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Gagal update foto di database", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal upload foto", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteProfilePhoto() {
        val npm = sharedPref.npm
        if (npm.isEmpty()) return

        firestoreManager.updatePhotoUrl(npm, "") { success ->
            if (success) {
                binding.ivAvatar.setImageResource(R.drawable.ic_person)
                Toast.makeText(requireContext(), "Foto dihapus", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToBiodata() {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right
            )
            .replace(R.id.fragmentContainer, BiodataFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToAcademicHistory() {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right
            )
            .replace(R.id.fragmentContainer, AcademicHistoryFragment())
            .addToBackStack(null)
            .commit()
    }

    private fun showDokumenDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Dokumen Akademik")
            .setMessage("Fitur unduh KRS dan KHS akan segera tersedia.\n\nUntuk saat ini, silakan hubungi bagian akademik untuk mendapatkan dokumen.")
            .setPositiveButton("OK", null)
            .show()
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
        FirestoreManager.getInstance().signOut(requireContext())

        val intent = Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        requireActivity().finish()
    }

    private fun navigateToSettings() {
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right, R.anim.slide_out_left,
                R.anim.slide_in_left, R.anim.slide_out_right
            )
            .replace(R.id.fragmentContainer, SettingsFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
