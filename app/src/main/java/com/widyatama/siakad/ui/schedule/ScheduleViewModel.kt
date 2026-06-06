package com.widyatama.siakad.ui.schedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.widyatama.siakad.data.model.Course
import com.widyatama.siakad.data.remote.FirestoreManager

class ScheduleViewModel : ViewModel() {

    private val firestoreManager = FirestoreManager.getInstance()

    private val _courses = MutableLiveData<List<Course>>(emptyList())
    val courses: LiveData<List<Course>> = _courses

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadCourses(npm: String, semester: Int) {
        _isLoading.value = true
        firestoreManager.getCoursesForStudent(npm, semester) { list, error ->
            _isLoading.postValue(false)
            if (error != null) {
                _errorMessage.postValue(error)
                _courses.postValue(emptyList())
            } else {
                _courses.postValue(list ?: emptyList())
                _errorMessage.postValue(null)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
