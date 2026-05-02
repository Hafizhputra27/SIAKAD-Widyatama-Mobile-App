package com.example.myapplication.data

import com.example.myapplication.model.AcademicYear
import com.example.myapplication.model.PaymentDetail

object PaymentData {

    fun getAcademicYears(): ArrayList<AcademicYear> {
        return arrayListOf(
            AcademicYear(
                id = 1,
                year = "2024/2025",
                totalInstallments = "6 Kali",
                nearestDeadline = "30 Jan 2025",
                totalPaid = "Rp 12.500.000",
                progress = 100,
                status = "LUNAS"
            ),
            AcademicYear(
                id = 2,
                year = "2023/2024",
                totalInstallments = "6 Kali",
                nearestDeadline = "15 Feb 2024",
                totalPaid = "Rp 11.000.000",
                progress = 100,
                status = "LUNAS"
            )
        )
    }

    fun getPaymentDetails(): ArrayList<PaymentDetail> {
        return arrayListOf(
            PaymentDetail(
                installmentNumber = "Cicilan 1",
                deadline = "30 Aug 2024",
                nominal = "Rp 2.500.000",
                status = "LUNAS"
            ),
            PaymentDetail(
                installmentNumber = "Cicilan 2",
                deadline = "30 Sep 2024",
                nominal = "Rp 2.000.000",
                status = "LUNAS"
            ),
            PaymentDetail(
                installmentNumber = "Cicilan 3",
                deadline = "30 Oct 2024",
                nominal = "Rp 2.000.000",
                status = "LUNAS"
            ),
            PaymentDetail(
                installmentNumber = "Cicilan 4",
                deadline = "30 Nov 2024",
                nominal = "Rp 2.000.000",
                status = "LUNAS"
            ),
            PaymentDetail(
                installmentNumber = "Cicilan 5",
                deadline = "30 Dec 2024",
                nominal = "Rp 2.000.000",
                status = "LUNAS"
            ),
            PaymentDetail(
                installmentNumber = "Cicilan 6",
                deadline = "30 Jan 2025",
                nominal = "Rp 2.000.000",
                status = "LUNAS"
            )
        )
    }
}