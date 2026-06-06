package com.widyatama.siakad.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.widyatama.siakad.R
import com.widyatama.siakad.data.model.Pengumuman
import com.widyatama.siakad.databinding.ItemPengumumanCardBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PengumumanAdapter(
    private val items: List<Pengumuman>
) : RecyclerView.Adapter<PengumumanAdapter.PengumumanViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PengumumanViewHolder {
        val binding = ItemPengumumanCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PengumumanViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PengumumanViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class PengumumanViewHolder(private val binding: ItemPengumumanCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pengumuman: Pengumuman) {
            binding.tvPengumumanTitle.text = pengumuman.title
            binding.tvPengumumanContent.text = pengumuman.content

            // Priority color
            val dotColor = when (pengumuman.priority.uppercase()) {
                "HIGH" -> R.color.error
                "NORMAL" -> R.color.info
                "LOW" -> R.color.success
                else -> R.color.text_secondary
            }
            binding.viewPriorityDot.setBackgroundResource(dotColor)
        }
    }
}