package com.widyatama.siakad.ui.admin

import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.widyatama.siakad.adapter.PaymentDetailAdapter
import com.widyatama.siakad.data.local.SharedPrefManager
import com.widyatama.siakad.data.model.Payment
import com.widyatama.siakad.data.model.PaymentDetail
import com.widyatama.siakad.databinding.FragmentAdminBinding
import com.widyatama.siakad.R
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminFragment : Fragment() {

    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminViewModel by viewModels()
    private val sharedPref by lazy { SharedPrefManager.getInstance(requireContext()) }

    private var activeTagihan: List<Payment> = emptyList()
    private var historyTagihan: List<Payment> = emptyList()
    private var currentTab: String = "aktif"

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
        setupObservers()
        loadData()

        binding.chipActiveBills.performClick()
    }

    private fun setupObservers() {
        viewModel.activeTagihan.observe(viewLifecycleOwner) { tagihan ->
            activeTagihan = tagihan
            if (currentTab == "aktif") {
                refreshCurrentTab()
            }
        }

        viewModel.historyTagihan.observe(viewLifecycleOwner) { tagihan ->
            historyTagihan = tagihan
            if (currentTab == "history") {
                refreshCurrentTab()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun refreshCurrentTab() {
        if (currentTab == "aktif") {
            binding.chipActiveBills.performClick()
        } else {
            binding.chipHistoryPayment.performClick()
        }
    }

    private fun setupRecyclerViews(details: List<PaymentDetail>) {
        binding.rvPayments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PaymentDetailAdapter(details)
        }
    }

    private fun setupClickListeners() {
        binding.btnDownload.setOnClickListener {
            generateAndDownloadTagihanPdf()
        }

        binding.chipActiveBills.setOnClickListener {
            currentTab = "aktif"
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
            currentTab = "history"
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

    private fun loadData() {
        val npm = sharedPref.npm
        if (npm.isEmpty()) return
        viewModel.loadTagihan(npm)
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

        val totalNominal = data.sumOf { parseNominal(it.nominal) }
        val paidNominal = data.filter { it.status == "LUNAS" }.sumOf { parseNominal(it.nominal) }
        val progress = if (totalNominal > 0) {
            ((paidNominal.toDouble() / totalNominal.toDouble()) * 100).toInt()
        } else {
            if (title == "History Pembayaran") 100 else 0
        }

        binding.progressPayment.progress = progress
        binding.tvProgressValue.text = "$progress%"

        val statusText = when {
            progress >= 100 -> "Status: LUNAS"
            progress > 0 -> "Status: DALAM PROSES"
            else -> "Status: BELUM LUNAS"
        }
        binding.tvStatusFooter.text = statusText
    }

    private fun Payment.toPaymentDetail(): PaymentDetail {
        val formatter = NumberFormat.getIntegerInstance(Locale("id", "ID"))
        val totalFormatted = "Rp " + formatter.format(this.total)
        val diskonFormatted = "Rp " + formatter.format(this.discount)

        val dueDateFormatted = this.dueDate?.let {
            val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
            sdf.format(it)
        } ?: "-"

        return PaymentDetail(
            academicYear = this.academicYear,
            installmentName = this.title,
            deadline = dueDateFormatted,
            nominal = totalFormatted,
            status = this.status,
            discount = diskonFormatted
        )
    }

    // ============================================================
    // PDF GENERATION
    // ============================================================
    private fun generateAndDownloadTagihanPdf() {
        Toast.makeText(requireContext(), "Menyiapkan dokumen...", Toast.LENGTH_SHORT).show()

        try {
            val nama = sharedPref.name
            val npm = sharedPref.npm
            val prodi = sharedPref.prodi

            val currentTabTitle = binding.tvMainTitle.text.toString()
            val data = if (currentTabTitle.contains("History", ignoreCase = true)) {
                historyTagihan
            } else {
                activeTagihan
            }

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            fun drawText(text: String, x: Float, y: Float, size: Float = 12f, bold: Boolean = false) {
                paint.textSize = size
                paint.typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                paint.color = Color.BLACK
                canvas.drawText(text, x, y, paint)
            }

            fun drawLine(y: Float) {
                paint.color = Color.LTGRAY
                paint.strokeWidth = 1f
                canvas.drawLine(40f, y, 555f, y, paint)
                paint.color = Color.BLACK
            }

            var yPos = 60f

            drawText("UNIVERSITAS WIDYATAMA", 140f, yPos, 18f, true)
            yPos += 22f
            drawText("Sistem Informasi Akademik (SIAKAD)", 155f, yPos, 12f)
            yPos += 30f
            drawLine(yPos)
            yPos += 20f

            drawText("KWITANSI / BUKTI TAGIHAN", 175f, yPos, 16f, true)
            yPos += 35f

            drawText("Nama          : $nama", 40f, yPos, 12f)
            yPos += 20f
            drawText("NPM           : $npm", 40f, yPos, 12f)
            yPos += 20f
            drawText("Program Studi : $prodi", 40f, yPos, 12f)
            yPos += 20f
            drawText("Tanggal Cetak : ${SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())}", 40f, yPos, 12f)
            yPos += 30f
            drawLine(yPos)
            yPos += 20f

            drawText("Semester", 40f, yPos, 11f, true)
            drawText("Keterangan", 130f, yPos, 11f, true)
            drawText("Total", 400f, yPos, 11f, true)
            drawText("Status", 480f, yPos, 11f, true)
            yPos += 16f
            drawLine(yPos)
            yPos += 14f

            if (data.isEmpty()) {
                drawText("Tidak ada data tagihan.", 40f, yPos, 10f)
                yPos += 20f
            } else {
                data.forEach { payment ->
                    val formatter = NumberFormat.getIntegerInstance(Locale("id", "ID"))
                    val totalStr = "Rp " + formatter.format(payment.total)
                    drawText(payment.academicYear, 40f, yPos, 10f)
                    drawText(payment.title, 130f, yPos, 10f)
                    drawText(totalStr, 390f, yPos, 10f)
                    drawText(payment.status, 480f, yPos, 10f)
                    yPos += 20f
                }
            }

            yPos += 10f
            drawLine(yPos)
            yPos += 30f

            drawText("Dokumen ini dicetak secara digital dari aplikasi SIAKAD Widyatama.", 40f, yPos, 10f)
            yPos += 16f
            drawText("Untuk informasi lebih lanjut, hubungi Bagian Keuangan Universitas Widyatama.", 40f, yPos, 10f)

            pdfDocument.finishPage(page)

            val downloadsDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                ?: requireContext().filesDir
            val fileName = "Tagihan_${npm}_${System.currentTimeMillis()}.pdf"
            val file = File(downloadsDir, fileName)

            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            Toast.makeText(requireContext(), "PDF tersimpan", Toast.LENGTH_LONG).show()

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "PDF viewer tidak ditemukan. File tersimpan di app storage.", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal membuat PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
