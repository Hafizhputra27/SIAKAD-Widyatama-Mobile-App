package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityLoginBinding
import android.view.View

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val prefs by lazy { getSharedPreferences("SIAKAD_PREFS", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (prefs.getBoolean("IS_LOGGED_IN", false)) {
            navigateToDashboard(prefs.getString("NPM", "") ?: "")
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        binding.btnLogin.setOnClickListener {
            val studentId = binding.etStudentId.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (studentId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, R.string.login_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show()
            navigateToDashboard(studentId)
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Reset password functionality coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToDashboard(npm: String) {
        prefs.edit().apply {
            putBoolean("IS_LOGGED_IN", true)
            putString("NPM", npm)
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