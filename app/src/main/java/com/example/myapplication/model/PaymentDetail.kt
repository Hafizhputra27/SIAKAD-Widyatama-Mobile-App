package com.example.myapplication.model

data class PaymentDetail(
    val academicYear: String,
    val installmentName: String,
    val deadline: String,
    val nominal: String,
    val status: String,
    val discount: String = "Rp 0"
)