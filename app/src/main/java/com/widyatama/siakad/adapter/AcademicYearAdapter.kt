package com.widyatama.siakad.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.widyatama.siakad.databinding.ItemHistoryCardBinding
import com.widyatama.siakad.data.model.AcademicYear

class AcademicYearAdapter(private val years: List<AcademicYear>) :
    RecyclerView.Adapter<AcademicYearAdapter.AcademicYearViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AcademicYearViewHolder {
        val binding = ItemHistoryCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AcademicYearViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AcademicYearViewHolder, position: Int) {
        holder.bind(years[position])
    }

    override fun getItemCount(): Int = years.size

    class AcademicYearViewHolder(private val binding: ItemHistoryCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(academicYear: AcademicYear) {
            binding.tvYear.text = academicYear.year
            binding.tvTotalInstallments.text = academicYear.totalInstallments
            binding.tvNearestDeadline.text = academicYear.nearestDeadline
            binding.tvTotalPaid.text = academicYear.totalPaid
            binding.tvStatus.text = academicYear.status
            binding.progressBar.progress = academicYear.progress
            binding.tvProgress.text = "${academicYear.progress}% TERBAYAR"

            if (academicYear.status == "LUNAS") {
                binding.tvStatus.setBackgroundResource(com.widyatama.siakad.R.drawable.bg_status_lunas)
            } else {
                binding.tvStatus.setBackgroundResource(com.widyatama.siakad.R.drawable.bg_status_proses)
            }
        }
    }
}