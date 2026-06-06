package com.widyatama.siakad.ui.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.widyatama.siakad.data.model.AcademicResult
import com.widyatama.siakad.data.model.Student
import com.widyatama.siakad.data.remote.FirestoreManager

class ResultsViewModel : ViewModel() {

    private val firestoreManager = FirestoreManager.getInstance()

    private val _student = MutableLiveData<Student?>()
    val student: LiveData<Student?> = _student

    private val _academicResults = MutableLiveData<Map<Int, List<AcademicResult>>>(emptyMap())
    val academicResults: LiveData<Map<Int, List<AcademicResult>>> = _academicResults

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadResults(npm: String) {
        if (npm.isEmpty()) {
            _errorMessage.value = "NPM tidak ditemukan"
            return
        }
        _isLoading.value = true
        completedCount = 0

        // Load student data
        firestoreManager.getMahasiswa(npm) { s, _ ->
            _student.postValue(s)
            checkLoadingComplete()
        }

        // Load all academic results
        firestoreManager.getAllAcademicResults(npm) { results, error ->
            if (error != null) {
                _errorMessage.postValue(error)
                _academicResults.postValue(emptyMap())
            } else {
                val grouped = results?.groupBy { it.semester }?.toSortedMap(reverseOrder()) ?: emptyMap()
                _academicResults.postValue(grouped)
            }
            checkLoadingComplete()
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    @Volatile
    private var completedCount = 0
    private val totalRequests = 2

    private fun checkLoadingComplete() {
        completedCount++
        if (completedCount >= totalRequests) {
            _isLoading.postValue(false)
            completedCount = 0
        }
    }
}
