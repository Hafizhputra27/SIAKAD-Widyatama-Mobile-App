package com.example.myapplication.model

data class Course(
    val id: Int,
    val name: String,
    val type: String, // WAJIB, PILIHAN
    val time: String,
    val room: String,
    val lecturer: String,
    val enrolledCount: Int,
    val day: String, // Senin, Selasa, etc.
    val sks: Int
)