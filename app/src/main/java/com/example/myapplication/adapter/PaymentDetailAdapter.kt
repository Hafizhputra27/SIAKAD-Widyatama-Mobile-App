package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemInstallmentCardBinding
import com.example.myapplication.model.PaymentDetail

class PaymentDetailAdapter(private val details: List<PaymentDetail>) :
    RecyclerView.Adapter<PaymentDetailAdapter.PaymentDetailViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentDetailViewHolder {
        val binding = ItemInstallmentCardBinding.inflate(
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

    class PaymentDetailViewHolder(private val binding: ItemInstallmentCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(paymentDetail: PaymentDetail) {
            binding.tvYear.text = paymentDetail.academicYear
            binding.tvInstallmentName.text = paymentDetail.installmentName
            binding.tvDeadline.text = paymentDetail.deadline
            binding.tvDiscount.text = paymentDetail.discount
            binding.tvPaidAmount.text = paymentDetail.nominal
            binding.tvStatusTag.text = paymentDetail.status

            val statusBackground = when (paymentDetail.status) {
                "LUNAS" -> R.drawable.bg_status_lunas
                "PENDING" -> R.drawable.bg_status_pending
                "TAGIHAN" -> R.drawable.bg_status_tagihan
                else -> R.drawable.bg_status_tagihan
            }
            binding.tvStatusTag.setBackgroundResource(statusBackground)

            val statusTextColor = when (paymentDetail.status) {
                "LUNAS" -> android.graphics.Color.parseColor("#00695C")
                "TAGIHAN" -> android.graphics.Color.parseColor("#E65100")
                else -> android.graphics.Color.parseColor("#757575")
            }
            binding.tvStatusTag.setTextColor(statusTextColor)

            // Update label based on status
            binding.tvPaidAmountLabel.text = if (paymentDetail.status == "TAGIHAN") {
                "Nominal Tagihan"
            } else {
                "Sudah Dibayar"
            }
        }
    }
}