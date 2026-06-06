package com.widyatama.siakad.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.widyatama.siakad.databinding.ItemSemesterBinding

class SemesterAdapter(
    private val semesters: List<Int>,
    private val selectedSemester: Int,
    private val onSemesterSelected: (Int) -> Unit
) : RecyclerView.Adapter<SemesterAdapter.SemesterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SemesterViewHolder {
        val binding = ItemSemesterBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return SemesterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SemesterViewHolder, position: Int) {
        holder.bind(semesters[position])
    }

    override fun getItemCount(): Int = semesters.size

    inner class SemesterViewHolder(private val binding: ItemSemesterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(semester: Int) {
            binding.tvSemesterLabel.text = "Semester $semester"
            binding.ivCheck.visibility = if (semester == selectedSemester) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onSemesterSelected(semester)
            }
        }
    }
}