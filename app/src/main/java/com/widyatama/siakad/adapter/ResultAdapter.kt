package com.widyatama.siakad.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.widyatama.siakad.R
import com.widyatama.siakad.databinding.ItemResultCourseBinding
import com.widyatama.siakad.data.model.AcademicResult

class ResultAdapter(private val results: List<AcademicResult>) :
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

        fun bind(result: AcademicResult) {
            bindResult(binding, result)
        }
    }

    companion object {
        fun bindResult(binding: ItemResultCourseBinding, result: AcademicResult) {
            val code = result.courseCode.trim()
            val name = result.courseName.trim()
            binding.tvCourseName.text = when {
                code.isNotEmpty() && name.isNotEmpty() -> "$code - $name"
                name.isNotEmpty() -> name
                code.isNotEmpty() -> code
                else -> "Mata Kuliah"
            }
            binding.tvSks.text = "${result.sks} SKS"
            binding.tvMutu.text = "Mutu: ${result.mutu}"

            val grade = result.grade.trim().ifEmpty { deriveGradeFromMutu(result.mutu) }
            binding.tvGrade.text = grade

            val (textColor, bgColor) = when (grade.uppercase()) {
                "A" -> Pair(ContextCompat.getColor(binding.root.context, R.color.success), 0xFFDCFCE7.toInt())
                "B+", "B" -> Pair(ContextCompat.getColor(binding.root.context, R.color.info), 0xFFDBEAFE.toInt())
                "C" -> Pair(0xFFEAB308.toInt(), 0xFFFEF9C3.toInt())
                "D" -> Pair(ContextCompat.getColor(binding.root.context, R.color.secondary), 0xFFFFEDD5.toInt())
                "E" -> Pair(ContextCompat.getColor(binding.root.context, R.color.error), 0xFFFEE2E2.toInt())
                else -> Pair(ContextCompat.getColor(binding.root.context, R.color.text_secondary), 0xFFF3F4F6.toInt())
            }
            binding.tvGrade.setTextColor(textColor)
            binding.cvGradeBadge.setCardBackgroundColor(bgColor)
        }

        fun deriveGradeFromMutu(mutu: Double): String = when {
            mutu >= 4.0 -> "A"
            mutu >= 3.5 -> "B+"
            mutu >= 3.0 -> "B"
            mutu >= 2.5 -> "C"
            mutu >= 2.0 -> "D"
            mutu > 0.0 -> "E"
            else -> "-"
        }
    }
}
