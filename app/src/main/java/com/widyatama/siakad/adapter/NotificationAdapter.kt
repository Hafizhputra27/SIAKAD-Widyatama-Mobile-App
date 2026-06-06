package com.widyatama.siakad.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.widyatama.siakad.R
import com.widyatama.siakad.data.model.NotificationItem
import com.widyatama.siakad.data.model.NotificationType
import com.widyatama.siakad.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private val items: List<NotificationItem>,
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class NotificationViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationItem) {
            binding.tvNotificationTitle.text = item.title
            binding.tvNotificationSubtitle.text = item.subtitle
            binding.tvNotificationTime.text = formatRelativeTime(item.timestamp)

            when (item.type) {
                NotificationType.PENGUMUMAN -> {
                    val pengumuman = item as NotificationItem.PengumumanItem
                    binding.viewCategoryBar.setBackgroundResource(R.color.info)
                    binding.ivNotificationIcon.setImageResource(R.drawable.ic_megaphone)
                    binding.ivNotificationIcon.setColorFilter(itemView.context.getColor(R.color.info))
                }
                NotificationType.TAGIHAN -> {
                    val tagihan = item as NotificationItem.TagihanItem
                    binding.viewCategoryBar.setBackgroundResource(R.color.error)
                    binding.ivNotificationIcon.setImageResource(R.drawable.ic_notification)
                    binding.ivNotificationIcon.setColorFilter(itemView.context.getColor(R.color.error))
                }
                NotificationType.PRESENSI -> {
                    val presensi = item as NotificationItem.PresensiItem
                    binding.viewCategoryBar.setBackgroundResource(R.color.success)
                    binding.ivNotificationIcon.setImageResource(R.drawable.ic_calendar)
                    binding.ivNotificationIcon.setColorFilter(itemView.context.getColor(R.color.success))
                }
            }

            itemView.setOnClickListener { onItemClick(item) }
        }

        private fun formatRelativeTime(date: Date?): String {
            if (date == null) return ""
            val diff = System.currentTimeMillis() - date.time
            return when {
                diff < 60_000 -> "Baru saja"
                diff < 3_600_000 -> "${diff / 60_000} menit lalu"
                diff < 86_400_000 -> "${diff / 3_600_000} jam lalu"
                else -> SimpleDateFormat("dd MMM yyyy", Locale("id", "ID")).format(date)
            }
        }
    }
}