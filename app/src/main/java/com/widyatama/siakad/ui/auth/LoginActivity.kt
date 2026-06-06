package com.widyatama.siakad.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.widyatama.siakad.data.remote.RetrofitClient
import com.widyatama.siakad.core.utils.ValidationUtils
import com.widyatama.siakad.data.local.SharedPrefManager
import com.widyatama.siakad.data.remote.FirestoreManager
import com.widyatama.siakad.databinding.ActivityLoginBinding
import com.widyatama.siakad.ui.dashboard.DashboardActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply dark mode preference before setting content view
        val sharedPref = SharedPrefManager.getInstance(this)
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
            if (sharedPref.getDarkMode()) androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
            else androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
        )

        // Pastikan FirebaseApp sudah ter-initialize
        if (FirebaseApp.getApps(this).isEmpty()) {
            Log.e(TAG, "FirebaseApp NOT initialized! Check google-services.json")
            Toast.makeText(this, "Firebase tidak terkonfigurasi. Hubungi admin.", Toast.LENGTH_LONG).show()
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        binding.btnLogin.setOnClickListener {
            if (isLoggingIn) return@setOnClickListener
            val now = System.currentTimeMillis()
            if (now < nextAllowedAttemptAt) {
                val secs = (nextAllowedAttemptAt - now) / 1000
                Toast.makeText(this, "Terlalu sering mencoba. Coba lagi dalam ${secs/60} menit.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            doLogin()
        }

        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(this, "Reset password functionality coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun doLogin() {
        // Prevent multiple parallel login attempts
        isLoggingIn = true
        binding.btnLogin.isEnabled = false

        val npm = binding.etStudentId.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        Log.d(TAG, "doLogin() called with npm='$npm', passwordLength=${password.length}")

        // Validasi input - NPM harus diisi dan hanya angka
        if (npm.isEmpty()) {
            binding.etStudentId.error = "NPM tidak boleh kosong"
            return
        }
        if (!npm.matches(Regex("^[0-9]+$"))) {
            binding.etStudentId.error = "NPM hanya boleh berisi angka"
            return
        }
        if (password.isEmpty()) {
            binding.etPassword.error = "Password tidak boleh kosong"
            return
        }
        if (password.length < 6) {
            binding.etPassword.error = "Password minimal 6 karakter"
            return
        }

        // Konversi NPM ke format email Firebase Auth
        val email = "${npm}@student.widyatama.ac.id"
        Log.d(TAG, "Attempting Firebase Auth sign-in with email='$email'")

        // Sign in dengan Firebase Auth (PRIMARY)
        FirebaseAuth.getInstance()
            .signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    Log.d(TAG, "Firebase Auth SUCCESS: user=${user?.uid}, email=${user?.email}")
                    // Firebase Auth berhasil → fetch data mahasiswa dari Firestore
                    // reset UI state
                    resetLoginState()
                    fetchMahasiswaData(npm)
                } else {
                    val exception = task.exception
                    Log.e(TAG, "Firebase Auth FAILED: ${exception?.javaClass?.simpleName} - ${exception?.message}")
                    
                    when (exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            Log.w(TAG, "Firebase Auth password salah. Mencoba fallback via web-admin API...")
                            // Fallback: coba login via web-admin API (lebih stabil daripada anonymous Firestore reads)
                            Toast.makeText(this, "Mencoba login via database...", Toast.LENGTH_SHORT).show()
                            doApiFallbackLogin(npm, password)
                        }
                        is FirebaseAuthInvalidUserException -> {
                            Log.e(TAG, "InvalidUser: User not found in Firebase Auth")
                            Toast.makeText(this, "NPM tidak terdaftar. Hubungi admin untuk aktivasi akun.", Toast.LENGTH_LONG).show()
                            resetLoginState()
                        }
                        is FirebaseNetworkException -> {
                            Log.e(TAG, "NetworkException: No internet")
                            Toast.makeText(this, "Tidak ada koneksi internet. Cek koneksi Anda.", Toast.LENGTH_LONG).show()
                            resetLoginState()
                        }
                        else -> {
                            // Check for rate limiting by error message
                            val errorMsg = exception?.message ?: ""
                            if (errorMsg.contains("blocked", ignoreCase = true) && errorMsg.contains("unusual activity", ignoreCase = true)) {
                                val cooldown = 5 * 60 * 1000L // 5 minutes
                                nextAllowedAttemptAt = System.currentTimeMillis() + cooldown
                                Toast.makeText(this, "Terlalu banyak percobaan. Coba lagi dalam 5 menit.", Toast.LENGTH_LONG).show()
                                Log.e(TAG, "Device temporarily blocked: $errorMsg")
                            } else {
                                Log.e(TAG, "Unknown error: $errorMsg")
                                Toast.makeText(this, "Login gagal: ${errorMsg.takeIf { it.isNotEmpty() } ?: "Error tidak diketahui"}", Toast.LENGTH_LONG).show()
                            }
                            resetLoginState()
                        }
                    }
                }
            }
    }

    // --- UI state helpers ---
    private var isLoggingIn = false
    private var nextAllowedAttemptAt: Long = 0L

    private fun resetLoginState() {
        isLoggingIn = false
        binding.btnLogin.isEnabled = true
    }

    /**
     * Fallback via web-admin API: GET /api/mahasiswa/{npm}
     * This avoids anonymous Firebase sign-in which is restricted in this project.
     */
    private fun doApiFallbackLogin(npm: String, password: String) {
        Log.d(TAG, "doApiFallbackLogin() called for npm='$npm'")

        val call = RetrofitClient.mahasiswaApiService.getMahasiswa(npm)
        call.enqueue(object : Callback<com.widyatama.siakad.data.remote.api.ApiResponse<com.widyatama.siakad.data.model.Student>> {
            override fun onResponse(
                call: Call<com.widyatama.siakad.data.remote.api.ApiResponse<com.widyatama.siakad.data.model.Student>>,
                response: Response<com.widyatama.siakad.data.remote.api.ApiResponse<com.widyatama.siakad.data.model.Student>>
            ) {
                if (response.isSuccessful) {
                    val body = response.body()
                    val student = body?.data
                    if (student == null) {
                        Log.e(TAG, "API fallback: student not found in API response for npm=$npm")
                        Toast.makeText(this@LoginActivity, "Data mahasiswa tidak ditemukan. Hubungi admin.", Toast.LENGTH_LONG).show()
                        resetLoginState()
                        return
                    }

                    val isPasswordValid = ValidationUtils.verifyPassword(password, student.passwordHash)
                    if (!isPasswordValid) {
                        Log.e(TAG, "API fallback: password verification failed for npm=$npm")
                        Toast.makeText(this@LoginActivity, "Password salah. Pastikan password sesuai dengan yang diberikan admin.", Toast.LENGTH_LONG).show()
                        resetLoginState()
                        return
                    }

                    if (student.status != "AKTIF") {
                        Log.e(TAG, "API fallback: student status is '${student.status}'")
                        Toast.makeText(this@LoginActivity, "Akun Anda tidak aktif (status: ${student.status}). Hubungi admin akademik.", Toast.LENGTH_LONG).show()
                        resetLoginState()
                        return
                    }

                    // Optionally attempt to auto-sync to Firebase Auth (best-effort)
                    try {
                        val auth = FirebaseAuth.getInstance()
                        val email = "${npm}@student.widyatama.ac.id"
                        val currentUser = auth.currentUser
                        if (currentUser != null && !currentUser.isAnonymous && currentUser.email == email) {
                            currentUser.updatePassword(password)
                        } else {
                            auth.createUserWithEmailAndPassword(email, password)
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "API fallback: auto-sync exception: ${e.message}")
                    }

                    // Simpan session & lanjut
                    saveSessionAndNavigate(student)
                } else {
                    Log.e(TAG, "API fallback failed: HTTP ${response.code()} | message=${response.message()}")
                    Toast.makeText(this@LoginActivity, "Gagal mengambil data dari server. Hubungi admin.", Toast.LENGTH_LONG).show()
                    resetLoginState()
                }
            }

            override fun onFailure(call: Call<com.widyatama.siakad.data.remote.api.ApiResponse<com.widyatama.siakad.data.model.Student>>, t: Throwable) {
                Log.e(TAG, "API fallback network failure: ${t.message}")
                Toast.makeText(this@LoginActivity, "Gagal terhubung ke server. Cek koneksi Anda.", Toast.LENGTH_LONG).show()
                resetLoginState()
            }
        })
    }


    private fun fetchMahasiswaData(npm: String) {
        Log.d(TAG, "Fetching mahasiswa data from Firestore for npm='$npm'")
        
        FirestoreManager.getInstance().getMahasiswa(npm) { student, error ->
            if (error != null) {
                Log.e(TAG, "Firestore getMahasiswa ERROR: $error")
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "Gagal mengambil data: $error", Toast.LENGTH_LONG).show()
                return@getMahasiswa
            }
            
            if (student == null) {
                Log.e(TAG, "Firestore getMahasiswa: student data is NULL")
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "Data mahasiswa tidak ditemukan di database. Hubungi admin.", Toast.LENGTH_LONG).show()
                return@getMahasiswa
            }

            Log.d(TAG, "Firestore getMahasiswa SUCCESS: name=${student.name}, status=${student.status}")

            if (student.status != "AKTIF") {
                Log.e(TAG, "Student status is '${student.status}', not AKTIF")
                FirebaseAuth.getInstance().signOut()
                Toast.makeText(this, "Akun Anda tidak aktif (status: ${student.status}). Hubungi admin akademik.", Toast.LENGTH_LONG).show()
                return@getMahasiswa
            }

            saveSessionAndNavigate(student)
        }
    }

    private fun saveSessionAndNavigate(student: com.widyatama.siakad.data.model.Student) {
        // Simpan session dengan SharedPrefManager
        val sharedPref = SharedPrefManager.getInstance(this)
        sharedPref.npm = student.npm
        sharedPref.name = student.name
        sharedPref.prodi = student.major
        sharedPref.email = student.campusEmail.ifEmpty { "${student.npm}@student.widyatama.ac.id" }
        sharedPref.semester = student.semesterBerjalan
        sharedPref.isLoggedIn = true

        Log.d(TAG, "Session saved: npm=${student.npm}, name=${student.name}")
        Toast.makeText(this, "Selamat datang, ${student.name}!", Toast.LENGTH_SHORT).show()

        // Navigate ke DashboardActivity
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
