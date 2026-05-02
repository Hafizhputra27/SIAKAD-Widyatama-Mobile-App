package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemPaymentDetailBinding
import com.example.myapplication.model.PaymentDetail

class PaymentDetailAdapter(private val details: List<PaymentDetail>) :
    RecyclerView.Adapter<PaymentDetailAdapter.PaymentDetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentDetailViewHolder {
        val binding = ItemPaymentDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PaymentDetailViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentDetailViewHolder, position: Int) {
        holder.bind(details[position])
    }

    override fun getItemCount(): Int = details.size

    class PaymentDetailViewHolder(private val binding: ItemPaymentDetailBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(paymentDetail: PaymentDetail) {
            binding.tvInstallment.text = paymentDetail.installmentNumber
            binding.tvDeadline.text = paymentDetail.deadline
            binding.tvNominal.text = paymentDetail.nominal
            binding.tvStatus.text = paymentDetail.status

            val statusBackground = when (paymentDetail.status) {
                "LUNAS" -> R.drawable.bg_status_lunas
                "PENDING" -> R.drawable.bg_status_pending
                else -> R.drawable.bg_status_tagihan
            }
            binding.tvStatus.setBackgroundResource(statusBackground)
        }
    }
}