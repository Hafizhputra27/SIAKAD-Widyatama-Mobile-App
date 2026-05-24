package com.widyatama.siakad.ui.auth

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.widyatama.siakad.data.remote.FirestoreManager
import com.widyatama.siakad.databinding.ActivityRegisterBinding
import com.widyatama.siakad.data.model.Student
import com.widyatama.siakad.R
import com.widyatama.siakad.core.utils.ValidationUtils

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val firestoreManager = FirestoreManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        binding.btnRegister.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val npm = binding.etNpm.text.toString().trim()
            val major = binding.etMajor.text.toString().trim()
            val campusEmail = binding.etCampusEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            when {
                !ValidationUtils.isNotEmpty(name, npm, major, campusEmail, password, confirmPassword) -> {
                    Toast.makeText(this, R.string.empty_field_error, Toast.LENGTH_SHORT).show()
                }
                !ValidationUtils.isValidNpmFormat(npm) -> {
                    Toast.makeText(this, "NPM harus 8+ digit angka", Toast.LENGTH_SHORT).show()
                }
                !ValidationUtils.isValidCampusEmail(campusEmail) -> {
                    Toast.makeText(this, "Gunakan email kampus @widyatama.ac.id", Toast.LENGTH_SHORT).show()
                }
                !ValidationUtils.passwordsMatch(password, confirmPassword) -> {
                    Toast.makeText(this, R.string.password_mismatch_error, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    saveStudentData(name, npm, major, campusEmail, password)
                }
            }
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun saveStudentData(name: String, npm: String, major: String, campusEmail: String, password: String) {
        val student = Student(
            npm = npm,
            name = name,
            major = major,
            campusEmail = campusEmail,
            passwordHash = ValidationUtils.hashPassword(password),
            semesterBerjalan = 1
        )

        firestoreManager.registerMahasiswa(student) { success, error ->
            if (success) {
                Toast.makeText(this, R.string.registration_success, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Registrasi gagal: ${error ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
