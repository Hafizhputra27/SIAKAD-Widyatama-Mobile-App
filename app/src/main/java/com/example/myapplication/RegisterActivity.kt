package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val db: FirebaseFirestore = Firebase.firestore

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
                name.isEmpty() || npm.isEmpty() || major.isEmpty() || campusEmail.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    Toast.makeText(this, R.string.empty_field_error, Toast.LENGTH_SHORT).show()
                }
                password != confirmPassword -> {
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
        val studentData = hashMapOf(
            "name" to name,
            "npm" to npm,
            "major" to major,
            "campusEmail" to campusEmail,
            "password" to password
        )

        db.collection("mahasiswa").document(npm)
            .set(studentData)
            .addOnSuccessListener {
                // Update UserSession with registered data
                UserSession.name = name
                UserSession.nim = npm
                UserSession.major = major
                UserSession.email = campusEmail

                Toast.makeText(this, R.string.registration_success, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Registrasi gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}