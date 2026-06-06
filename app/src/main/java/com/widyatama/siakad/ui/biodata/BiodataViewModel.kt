package com.widyatama.siakad.ui.biodata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.widyatama.siakad.data.model.Student
import com.widyatama.siakad.data.remote.FirestoreManager

class BiodataViewModel : ViewModel() {

    private val firestoreManager = FirestoreManager.getInstance()

    private val _studentData = MutableLiveData<Student?>()
    val studentData: LiveData<Student?> = _studentData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    fun loadBiodata(npm: String) {
        if (npm.isEmpty()) {
            _errorMessage.value = "NPM tidak ditemukan"
            return
        }
        _isLoading.value = true
        firestoreManager.getMahasiswa(npm) { student, error ->
            _isLoading.postValue(false)
            if (error != null) {
                _errorMessage.postValue(error)
            } else {
                _studentData.postValue(student)
                _errorMessage.postValue(null)
            }
        }
    }

    fun saveBiodata(student: Student) {
        _isLoading.value = true
        firestoreManager.updateBiodata(student) { success, error ->
            _isLoading.postValue(false)
            if (success) {
                _saveSuccess.postValue(true)
                _studentData.postValue(student)
            } else {
                _errorMessage.postValue(error ?: "Gagal menyimpan biodata")
                _saveSuccess.postValue(false)
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
