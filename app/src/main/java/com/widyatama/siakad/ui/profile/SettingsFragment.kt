package com.widyatama.siakad.ui.profile

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.widyatama.siakad.data.local.SharedPrefManager
import com.widyatama.siakad.data.remote.FirestoreManager
import com.widyatama.siakad.databinding.FragmentSettingsBinding
import com.widyatama.siakad.ui.auth.LoginActivity

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val sharedPref by lazy { SharedPrefManager.getInstance(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivSettings.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        // Hide seed database button in production
        binding.btnSeedDatabase.visibility = View.GONE

        setupMenuActions()
        restoreSwitchStates()
    }

    private fun setupMenuActions() {
        // Ubah Password
        binding.itemUbahPassword.setOnClickListener {
            showUbahPasswordDialog()
        }

        // Keamanan
        binding.itemKeamanan.setOnClickListener {
            showKeamananDialog()
        }

        // Push Notification
        binding.switchPushNotif.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.savePushNotif(isChecked)
            handlePushNotificationToggle(isChecked)
        }

        // Email Notification
        binding.switchEmailNotif.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.saveEmailNotif(isChecked)
        }

        // Dark Mode
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            sharedPref.saveDarkMode(isChecked)
            applyDarkMode(isChecked)
        }

        // Bahasa
        binding.itemBahasa.setOnClickListener {
            showBahasaDialog()
        }

        // Pusat Bantuan
        binding.itemPusatBantuan.setOnClickListener {
            dialAdmin()
        }

        // Tentang Aplikasi
        binding.itemTentangAplikasi.setOnClickListener {
            showTentangDialog()
        }
    }

    private fun restoreSwitchStates() {
        binding.switchPushNotif.isChecked = sharedPref.getPushNotif()
        binding.switchEmailNotif.isChecked = sharedPref.getEmailNotif()
        binding.switchDarkMode.isChecked = sharedPref.getDarkMode()
    }

    private fun applyDarkMode(enabled: Boolean) {
        val mode = if (enabled) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    private fun handlePushNotificationToggle(enabled: Boolean) {
        val topic = "all_students"
        if (enabled) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Notifikasi push diaktifkan", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal mengaktifkan notifikasi: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Notifikasi push dinonaktifkan", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Gagal menonaktifkan notifikasi: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun dialAdmin() {
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = android.net.Uri.parse("tel:0858585858")
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Tidak dapat membuka dialer", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showKeamananDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Keamanan Akun")
            .setMessage(
                "• Sesi login aktif menggunakan Firebase Authentication\n" +
                "• Password di-enkripsi secara aman\n" +
                "• Untuk keamanan, logout jika menggunakan perangkat bersama\n\n" +
                "Untuk mengganti password, gunakan menu 'Ubah Password'."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showBahasaDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Bahasa Aplikasi")
            .setMessage("Bahasa Indonesia telah diatur sebagai bahasa default aplikasi.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showTentangDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Tentang SIAKAD")
            .setMessage(
                "SIAKAD Widyatama\n" +
                "Sistem Informasi Akademik\n\n" +
                "Universitas Widyatama\n" +
                "Versi 1.0.0\n\n" +
                "Dikembangkan untuk mendukung\nkegiatan akademik mahasiswa."
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showUbahPasswordDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 20, 60, 20)
        }

        val etPasswordLama = TextInputEditText(requireContext()).apply {
            hint = "Password Lama"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val etPasswordBaru = TextInputEditText(requireContext()).apply {
            hint = "Password Baru (min 8 karakter)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        val etKonfirmasi = TextInputEditText(requireContext()).apply {
            hint = "Konfirmasi Password Baru"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        layout.addView(etPasswordLama)
        layout.addView(View(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(0, 16)
        })
        layout.addView(etPasswordBaru)
        layout.addView(View(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(0, 16)
        })
        layout.addView(etKonfirmasi)

        AlertDialog.Builder(requireContext())
            .setTitle("Ubah Password")
            .setView(layout)
            .setPositiveButton("Simpan") { dialog, _ ->
                val passwordLama = etPasswordLama.text.toString()
                val passwordBaru = etPasswordBaru.text.toString()
                val konfirmasi = etKonfirmasi.text.toString()

                if (passwordLama.isEmpty() || passwordBaru.isEmpty() || konfirmasi.isEmpty()) {
                    Toast.makeText(requireContext(), "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (passwordBaru.length < 8) {
                    Toast.makeText(requireContext(), "Password baru minimal 8 karakter", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (passwordBaru != konfirmasi) {
                    Toast.makeText(requireContext(), "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val user = FirebaseAuth.getInstance().currentUser
                if (user == null) {
                    Toast.makeText(requireContext(), "Session tidak valid, login ulang", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val npm = sharedPref.npm
                val email = "${npm}@widyatama.ac.id"
                val credential = EmailAuthProvider.getCredential(email, passwordLama)

                user.reauthenticate(credential)
                    .addOnSuccessListener {
                        user.updatePassword(passwordBaru)
                            .addOnSuccessListener {
                                FirestoreManager.getInstance().updatePassword(npm, passwordLama, passwordBaru) { success, error ->
                                    if (success) {
                                        Toast.makeText(requireContext(), "Password berhasil diubah!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(requireContext(), "Password diubah, tapi gagal update database: ${error}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                dialog.dismiss()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Gagal mengubah password: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Password lama salah", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun logout() {
        FirestoreManager.getInstance().signOut(requireContext())

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
