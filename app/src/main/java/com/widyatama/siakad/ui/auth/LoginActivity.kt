package com.widyatama.siakad.ui.auth

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.widyatama.siakad.R
import com.widyatama.siakad.data.remote.FirestoreManager
import com.widyatama.siakad.databinding.ActivityLoginBinding
import com.widyatama.siakad.ui.dashboard.DashboardActivity
import android.view.View

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val prefs by lazy { getSharedPreferences("SIAKAD_PREFS", Context.MODE_PRIVATE) }
    private val firestoreManager = FirestoreManager.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (prefs.getBoolean("IS_LOGGED_IN", false)) {
            val npm = prefs.getString("NPM", "") ?: ""
            val studentName = prefs.getString("STUDENT_NAME", "") ?: ""
            navigateToDashboard(npm, studentName)
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        binding.btnLogin.setOnClickListener {
            val npm = binding.etStudentId.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (npm.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            firestoreManager.loginMahasiswa(npm, password) { student, error ->
                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
                } else if (student != null) {
                    Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show()
                    navigateToDashboard(npm, student.name, student.semesterBerjalan)
                }
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Reset password functionality coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToDashboard(npm: String, studentName: String, semester: Int = 1) {
        prefs.edit().apply {
            putBoolean("IS_LOGGED_IN", true)
            putString("NPM", npm)
            putString("STUDENT_NAME", studentName)
            putInt("CURRENT_SEMESTER", semester)
            apply()
        }

        val intent = Intent(this, DashboardActivity::class.java)
        intent.putExtra("USER_NAME", npm)
        startActivity(intent)
        finish()
    }

    fun navigateToRegister(view: View) {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}
