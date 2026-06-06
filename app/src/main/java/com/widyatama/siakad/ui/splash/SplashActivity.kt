package com.widyatama.siakad.ui.splash

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.widyatama.siakad.data.local.SharedPrefManager
import com.widyatama.siakad.databinding.ActivitySplashBinding
import com.widyatama.siakad.ui.auth.LoginActivity
import com.widyatama.siakad.ui.dashboard.DashboardActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashDuration = 2500L // 2.5 detik total

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startLogoAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            checkSessionAndNavigate()
        }, splashDuration)
    }

    private fun startLogoAnimation() {
        // Animasi scale: 0.6f → 1.0f
        val scaleX = ObjectAnimator.ofFloat(binding.ivLogo, "scaleX", 0.6f, 1.0f)
        val scaleY = ObjectAnimator.ofFloat(binding.ivLogo, "scaleY", 0.6f, 1.0f)

        // Animasi alpha: 0f → 1f
        val alpha = ObjectAnimator.ofFloat(binding.ivLogo, "alpha", 0f, 1f)

        // Animasi transY: 30f → 0f (naik sedikit)
        val transY = ObjectAnimator.ofFloat(binding.ivLogo, "translationY", 30f, 0f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha, transY)
            duration = 900
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun checkSessionAndNavigate() {
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val sharedPref = SharedPrefManager.getInstance(this)

        val isLoggedIn = firebaseUser != null && sharedPref.isLoggedIn && sharedPref.npm.isNotEmpty()

        val intent = if (isLoggedIn) {
            Intent(this, DashboardActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java)
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
