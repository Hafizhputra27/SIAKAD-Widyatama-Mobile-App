package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityDashboardBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViews()
    }

    private fun setupViews() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    true
                }
                R.id.nav_admin -> {
                    showFeatureComingSoon("Admin")
                    true
                }
                R.id.nav_schedule -> {
                    showFeatureComingSoon("Schedule")
                    true
                }
                R.id.nav_results -> {
                    showFeatureComingSoon("Results")
                    true
                }
                R.id.nav_courses -> {
                    showFeatureComingSoon("Courses")
                    true
                }
                else -> false
            }
        }
    }

    private fun showFeatureComingSoon(feature: String) {
        // Placeholder for future navigation - shows dashboard for now
    }
}