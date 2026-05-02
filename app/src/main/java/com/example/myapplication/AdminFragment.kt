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
        setupRecyclerViews()
        setupClickListeners()
    }

    private fun setupRecyclerViews() {
        val academicYears = PaymentData.getAcademicYears()
        binding.rvAcademicYear.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = AcademicYearAdapter(academicYears)
        }

        val paymentDetails = PaymentData.getPaymentDetails()
        binding.rvPaymentDetails.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = PaymentDetailAdapter(paymentDetails)
        }
    }

    private fun setupClickListeners() {
        binding.btnDownload.setOnClickListener {
            Toast.makeText(requireContext(), "Download functionality coming soon!", Toast.LENGTH_SHORT).show()
        }

        binding.chipHistoryPayment.setOnClickListener {
            Toast.makeText(requireContext(), "History Pembayaran selected", Toast.LENGTH_SHORT).show()
        }

        binding.chipHistoryUsp.setOnClickListener {
            Toast.makeText(requireContext(), "History USP selected", Toast.LENGTH_SHORT).show()
        }

        binding.chipHistorySpp.setOnClickListener {
            Toast.makeText(requireContext(), "History SPP selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}