package com.widyatama.siakad.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.widyatama.siakad.data.model.Payment
import com.widyatama.siakad.data.remote.FirestoreManager

class AdminViewModel : ViewModel() {

    private val firestoreManager = FirestoreManager.getInstance()

    private val _activeTagihan = MutableLiveData<List<Payment>>(emptyList())
    val activeTagihan: LiveData<List<Payment>> = _activeTagihan

    private val _historyTagihan = MutableLiveData<List<Payment>>(emptyList())
    val historyTagihan: LiveData<List<Payment>> = _historyTagihan

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun loadTagihan(npm: String) {
        if (npm.isEmpty()) {
            _errorMessage.value = "NPM tidak ditemukan"
            return
        }
        _isLoading.value = true
        completedCount = 0

        firestoreManager.getTagihanAktif(npm) { list, error ->
            if (error != null) {
                _errorMessage.postValue(error)
            } else {
                _activeTagihan.postValue(list ?: emptyList())
            }
            checkLoadingComplete()
        }

        firestoreManager.getHistoryPembayaran(npm) { list, error ->
            if (error != null) {
                _errorMessage.postValue(error)
            } else {
                _historyTagihan.postValue(list ?: emptyList())
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
