package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.AcademicYearAdapter
import com.example.myapplication.adapter.PaymentDetailAdapter
import com.example.myapplication.data.PaymentData
import com.example.myapplication.databinding.FragmentAdminBinding

class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        // Set default view to Tagihan Aktif
        binding.chipActiveBills.performClick()
    }

    private fun setupRecyclerViews(details: List<com.example.myapplication.model.PaymentDetail>) {
        binding.rvPayments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PaymentDetailAdapter(details)
        }
    }

    private fun setupClickListeners() {
        binding.btnDownload.setOnClickListener {
            val currentTab = binding.tvMainTitle.text.toString()
            Toast.makeText(requireContext(), "Downloading data for $currentTab...", Toast.LENGTH_SHORT).show()
        }

        binding.chipActiveBills.setOnClickListener {
            val data = PaymentData.getActiveBills()
            updateActiveTab(it.id)
            updateUIForTab(
                "Tagihan Aktif",
                "Daftar tagihan yang harus\nsegera dibayarkan",
                "Total Tagihan Aktif",
                calculateTotal(data),
                data
            )
        }

        binding.chipHistoryPayment.setOnClickListener {
            val data = PaymentData.getPaymentHistory()
            updateActiveTab(it.id)
            updateUIForTab(
                "History Pembayaran",
                "Riwayat transaksi pembayaran\numum mahasiswa",
                "Total Pembayaran",
                calculateTotal(data),
                data
            )
        }
    }

    private fun calculateTotal(details: List<com.example.myapplication.model.PaymentDetail>): String {
        var total = 0L
        details.forEach {
            val cleanAmount = it.nominal.replace("Rp ", "").replace(".", "").replace(",", "")
            total += cleanAmount.toLongOrNull() ?: 0L
        }
        return "Rp " + java.text.NumberFormat.getIntegerInstance(java.util.Locale("id", "ID")).format(total)
    }

    private fun updateActiveTab(selectedChipId: Int) {
        val chips = listOf(binding.chipActiveBills, binding.chipHistoryPayment)
        chips.forEach { chip ->
            if (chip.id == selectedChipId) {
                chip.setChipBackgroundColorResource(R.color.navy_blue)
                chip.setTextColor(resources.getColor(R.color.white, null))
            } else {
                chip.setChipBackgroundColorResource(R.color.light_gray)
                chip.setTextColor(resources.getColor(R.color.navy_blue, null))
            }
        }
    }

    private fun updateUIForTab(title: String, subtitle: String, summaryLabel: String, amount: String, data: List<com.example.myapplication.model.PaymentDetail>) {
        binding.tvMainTitle.text = title
        binding.tvMainSubtitle.text = subtitle
        binding.tvSummaryLabel.text = summaryLabel
        binding.tvSummaryAmount.text = amount
        setupRecyclerViews(data)
        
        // Dynamic summary card behavior
        if (title == "Tagihan Aktif") {
            binding.progressPayment.progress = 0
            binding.tvProgressValue.text = "0%"
            binding.tvStatusFooter.text = "Status: BELUM LUNAS"
        } else {
            binding.progressPayment.progress = 100
            binding.tvProgressValue.text = "100%"
            binding.tvStatusFooter.text = "Status: LUNAS"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}