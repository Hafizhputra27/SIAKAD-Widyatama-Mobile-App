package com.widyatama.siakad.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.widyatama.siakad.R
import com.widyatama.siakad.databinding.ItemCourseCardBinding
import com.widyatama.siakad.data.model.Course

class CourseAdapter(private var courses: List<Course>) :
    RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courses[position])
    }

    override fun getItemCount(): Int = courses.size

    fun updateData(newCourses: List<Course>) {
        courses = newCourses
        notifyDataSetChanged()
    }

    class CourseViewHolder(private val binding: ItemCourseCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(course: Course) {
            binding.tvCourseCode.text = course.code
            binding.tvCourseName.text = course.name
            binding.tvCourseType.text = course.type
            binding.tvCourseTime.text = course.jamDisplay
            binding.tvCourseRoom.text = course.room
            binding.tvCourseLecturer.text = course.lecturer
            binding.tvAttendanceCount.text = "${course.attendance}/${course.totalAttendance}"

            if (course.type == "WAJIB") {
                binding.tvCourseType.setBackgroundResource(R.drawable.bg_tag_wajib)
                binding.tvCourseType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.success))
            } else {
                binding.tvCourseType.setBackgroundResource(R.drawable.bg_tag_pilihan)
                binding.tvCourseType.setTextColor(ContextCompat.getColor(binding.root.context, R.color.info))
            }

            // Progress kehadiran
            val attendance = course.attendance
            val totalAttendance = course.totalAttendance.coerceAtLeast(1)
            val persentase = ((attendance.toFloat() / totalAttendance.toFloat()) * 100).toInt().coerceIn(0, 100)

            binding.progressKehadiran.progress = persentase
            binding.tvPersentaseKehadiran.text = "$persentase%"
            binding.tvDetailKehadiran.text = "$attendance dari $totalAttendance pertemuan"

            // Warna berdasarkan persentase
            val (progressColor, textColor) = when {
                persentase >= 80 -> Pair(0xFF22C55E.toInt(), 0xFF15803D.toInt())
                persentase >= 75 -> Pair(0xFFF59E0B.toInt(), 0xFFB45309.toInt())
                else -> Pair(0xFFEF4444.toInt(), 0xFFB91C1C.toInt())
            }

            // Update warna progress bar
            val progressDrawable = binding.progressKehadiran.progressDrawable?.mutate()
            if (progressDrawable is android.graphics.drawable.LayerDrawable) {
                val progressLayer = progressDrawable.findDrawableByLayerId(android.R.id.progress)
                progressLayer?.setColorFilter(progressColor, android.graphics.PorterDuff.Mode.SRC_IN)
            }

            // Update warna text persentase
            binding.tvPersentaseKehadiran.setTextColor(textColor)

            // Warning text jika < 75%
            if (persentase < 75 && totalAttendance > 0) {
                binding.tvDetailKehadiran.text = "$attendance dari $totalAttendance pertemuan \u26A0\uFE0F Perlu ditingkatkan"
                binding.tvDetailKehadiran.setTextColor(0xFFB91C1C.toInt())
            } else {
                binding.tvDetailKehadiran.setTextColor(
                    binding.root.context.getColor(android.R.color.darker_gray)
                )
            }
        }
    }
}
