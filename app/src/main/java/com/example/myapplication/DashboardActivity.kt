package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.myapplication.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            loadFragment(DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString("USER_NAME", intent.getStringExtra("USER_NAME"))
                }
            })
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment().apply {
                        arguments = Bundle().apply {
                            putString("USER_NAME", intent.getStringExtra("USER_NAME"))
                        }
                    })
                    true
                }
                R.id.nav_admin -> {
                    loadFragment(AdminFragment())
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

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun showFeatureComingSoon(feature: String) {
        // Placeholder for future navigation
    }
}