package com.widyatama.siakad.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
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
            binding.tvCourseName.text = course.name
            binding.tvCourseType.text = course.type
            binding.tvCourseTime.text = course.jamMulai
            binding.tvCourseRoom.text = course.room
            binding.tvCourseLecturer.text = course.lecturer
            binding.tvAttendanceCount.text = "${course.attendance}/${course.totalAttendance}"

            if (course.type == "WAJIB") {
                binding.tvCourseType.setBackgroundResource(R.drawable.bg_tag_wajib)
                binding.tvCourseType.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            } else {
                binding.tvCourseType.setBackgroundResource(R.drawable.bg_tag_pilihan)
                binding.tvCourseType.setTextColor(android.graphics.Color.parseColor("#1565C0"))
            }
        }
    }
}
