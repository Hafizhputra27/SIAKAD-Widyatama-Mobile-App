package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

            if (validateCredentials(studentId, password)) {
                navigateToDashboard()
            } else {
                Toast.makeText(this, R.string.login_failed, Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Reset password functionality coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateCredentials(studentId: String, password: String): Boolean {
        return studentId.isNotEmpty() && password.length >= 4
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }
}