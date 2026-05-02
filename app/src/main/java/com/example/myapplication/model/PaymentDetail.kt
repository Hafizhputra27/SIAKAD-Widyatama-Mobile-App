package com.example.myapplication.model

data class PaymentDetail(
    val installmentNumber: String,
    val deadline: String,
    val nominal: String,
    val status: String
)