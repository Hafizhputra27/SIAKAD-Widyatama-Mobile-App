package com.widyatama.siakad.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.widyatama.siakad.data.model.AcademicResult
import com.widyatama.siakad.data.model.SemesterResultItem
import com.widyatama.siakad.databinding.ItemResultCourseBinding
import com.widyatama.siakad.databinding.ItemSemesterResultHeaderBinding
import java.util.Locale

class SemesterResultAdapter(
    private val items: List<SemesterResultItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_COURSE = 1

        fun fromGroupedResults(grouped: Map<Int, List<AcademicResult>>): List<SemesterResultItem> {
            val flat = mutableListOf<SemesterResultItem>()
            grouped.toSortedMap(reverseOrder()).forEach { (semester, courses) ->
                val sksSemester = courses.sumOf { it.sks }
                val ipkSemester = if (sksSemester > 0) {
                    courses.sumOf { it.mutu * it.sks } / sksSemester.toDouble()
                } else 0.0
                flat.add(SemesterResultItem.Header(semester, ipkSemester, sksSemester))
                courses.forEach { flat.add(SemesterResultItem.Course(it)) }
            }
            return flat
        }
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is SemesterResultItem.Header -> TYPE_HEADER
        is SemesterResultItem.Course -> TYPE_COURSE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(
                ItemSemesterResultHeaderBinding.inflate(inflater, parent, false)
            )
            else -> CourseViewHolder(
                ItemResultCourseBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is SemesterResultItem.Header -> (holder as HeaderViewHolder).bind(item)
            is SemesterResultItem.Course -> (holder as CourseViewHolder).bind(item.result)
        }
    }

    override fun getItemCount(): Int = items.size

    class HeaderViewHolder(
        private val binding: ItemSemesterResultHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: SemesterResultItem.Header) {
            binding.tvSemesterTitle.text = "Semester ${header.semester}"
            binding.tvSemesterStats.text = String.format(
                Locale("id", "ID"),
                "IPK %.2f • %d SKS",
                header.ipkSemester,
                header.sksSemester
            )
        }
    }

    class CourseViewHolder(
        private val binding: ItemResultCourseBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(result: AcademicResult) {
            ResultAdapter.bindResult(binding, result)
        }
    }
}
