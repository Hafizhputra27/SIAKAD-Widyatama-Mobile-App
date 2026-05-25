package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemResultCourseBinding
import com.example.myapplication.model.CourseResult

class ResultAdapter(private val results: List<CourseResult>) :
    RecyclerView.Adapter<ResultAdapter.ResultViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val binding = ItemResultCourseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ResultViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        holder.bind(results[position])
    }

    override fun getItemCount(): Int = results.size

    class ResultViewHolder(private val binding: ItemResultCourseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(result: CourseResult) {
            binding.tvCourseName.text = result.name
            binding.tvSks.text = "${result.sks} SKS"
            binding.tvMutu.text = "Mutu: ${result.mutu}"
            binding.tvGrade.text = result.grade

            // Set color based on grade
            val colorRes = when (result.grade) {
                "A" -> R.color.green_success
                "B+" -> R.color.navy_blue
                "B" -> R.color.blue_light
                else -> R.color.text_secondary
            }
            binding.tvGrade.setTextColor(ContextCompat.getColor(binding.root.context, colorRes))
            
            // Set icon if available
            result.iconRes?.let {
                binding.ivCourseIcon.setImageResource(it)
            }
        }
    }
}