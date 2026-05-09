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

    fun getPaymentHistory(): ArrayList<PaymentDetail> {
        return arrayListOf(
            PaymentDetail(
                academicYear = "Tahun 2024/2025",
                installmentName = "Biaya Wisuda",
                deadline = "10 Januari 2025",
                nominal = "Rp 2.500.000",
                status = "LUNAS",
                discount = "Rp 0"
            ),
            PaymentDetail(
                academicYear = "Tahun 2024/2025",
                installmentName = "Jaket Almamater",
                deadline = "15 Agustus 2024",
                nominal = "Rp 350.000",
                status = "LUNAS",
                discount = "Rp 0"
            )
        )
    }

    fun getActiveBills(): ArrayList<PaymentDetail> {
        return arrayListOf(
            PaymentDetail(
                academicYear = "Tahun 2024/2025",
                installmentName = "UKT Semester 3",
                deadline = "15 Agustus 2024",
                nominal = "Rp 5.500.000",
                status = "TAGIHAN",
                discount = "Rp 0"
            ),
            PaymentDetail(
                academicYear = "Tahun 2024/2025",
                installmentName = "Iuran Kemahasiswaan",
                deadline = "15 Agustus 2024",
                nominal = "Rp 250.000",
                status = "TAGIHAN",
                discount = "Rp 0"
            )
        )
    }
}