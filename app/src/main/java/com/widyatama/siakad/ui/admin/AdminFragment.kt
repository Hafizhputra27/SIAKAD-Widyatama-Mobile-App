package com.widyatama.siakad.ui.admin

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.widyatama.siakad.adapter.PaymentDetailAdapter
import com.widyatama.siakad.data.remote.FirestoreManager
import com.widyatama.siakad.databinding.FragmentAdminBinding
import com.widyatama.siakad.R
import com.widyatama.siakad.data.model.Payment
import com.widyatama.siakad.data.model.PaymentDetail
import java.text.NumberFormat
import java.util.Locale

class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    private val firestoreManager = FirestoreManager.getInstance()
    private val prefs by lazy {
        requireActivity().getSharedPreferences("SIAKAD_PREFS", Context.MODE_PRIVATE)
    }

    private var activeTagihan: List<Payment> = emptyList()
    private var historyTagihan: List<Payment> = emptyList()

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
        loadDataFromFirestore()
        
        binding.chipActiveBills.performClick()
    }

    private fun setupRecyclerViews(details: List<PaymentDetail>) {
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
            updateActiveTab(it.id)
            val details = activeTagihan.map { it.toPaymentDetail() }
            updateUIForTab(
                "Tagihan Aktif",
                "Daftar tagihan yang harus\nsegera dibayarkan",
                "Total Tagihan Aktif",
                calculateTotal(details),
                details
            )
        }

        binding.chipHistoryPayment.setOnClickListener {
            updateActiveTab(it.id)
            val details = historyTagihan.map { it.toPaymentDetail() }
            updateUIForTab(
                "History Pembayaran",
                "Riwayat transaksi pembayaran\numum mahasiswa",
                "Total Pembayaran",
                calculateTotal(details),
                details
            )
        }
    }

    private fun loadDataFromFirestore() {
        val npm = prefs.getString("NPM", "") ?: ""
        if (npm.isEmpty()) return

        firestoreManager.getTagihanAktif(npm) { tagihan, _ ->
            activeTagihan = tagihan
        }

        firestoreManager.getHistoryPembayaran(npm) { tagihan, _ ->
            historyTagihan = tagihan
        }
    }

    private fun calculateTotal(details: List<PaymentDetail>): String {
        val total = details.sumOf { parseNominal(it.nominal) }
        return "Rp " + NumberFormat.getIntegerInstance(Locale("id", "ID")).format(total)
    }

    private fun parseNominal(nominal: String): Long {
        val cleanAmount = nominal.replace("Rp ", "").replace(".", "").replace(",", "")
        return cleanAmount.toLongOrNull() ?: 0L
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

    private fun updateUIForTab(title: String, subtitle: String, summaryLabel: String, amount: String, data: List<PaymentDetail>) {
        binding.tvMainTitle.text = title
        binding.tvMainSubtitle.text = subtitle
        binding.tvSummaryLabel.text = summaryLabel
        binding.tvSummaryAmount.text = amount
        setupRecyclerViews(data)
        
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

    private fun Payment.toPaymentDetail(): PaymentDetail {
        val formatter = NumberFormat.getIntegerInstance(Locale("id", "ID"))
        val nominalFormatted = "Rp " + formatter.format(this.nominal)
        val diskonFormatted = "Rp " + formatter.format(this.diskon)
        
        val batasWaktuFormatted = this.batasWaktu?.let {
            val sdf = java.text.SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            sdf.format(it.toDate())
        } ?: "-"

        return PaymentDetail(
            academicYear = this.tahunAjaran,
            installmentName = this.judul,
            deadline = batasWaktuFormatted,
            nominal = nominalFormatted,
            status = this.status,
            discount = diskonFormatted
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
