package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemCourseCardBinding
import com.example.myapplication.model.Course

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
            binding.tvTypeTag.text = course.type
            binding.tvTime.text = course.time
            binding.tvRoom.text = course.room
            binding.tvLecturer.text = course.lecturer
            binding.tvEnrolled.text = "${course.enrolledCount} Students Enrolled"

            // Adjust tag color based on type
            if (course.type == "WAJIB") {
                binding.tvTypeTag.setBackgroundResource(R.drawable.bg_tag_wajib)
                binding.tvTypeTag.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            } else {
                binding.tvTypeTag.setBackgroundResource(R.drawable.bg_tag_pilihan)
                binding.tvTypeTag.setTextColor(android.graphics.Color.parseColor("#1565C0"))
            }
        }
    }
}