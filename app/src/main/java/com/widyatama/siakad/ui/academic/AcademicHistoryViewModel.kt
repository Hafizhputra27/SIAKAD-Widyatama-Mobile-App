package com.widyatama.siakad.ui.academic

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.widyatama.siakad.data.model.AcademicResult
import com.widyatama.siakad.data.model.TranscriptSummary
import com.widyatama.siakad.data.remote.FirestoreManager

class AcademicHistoryViewModel : ViewModel() {

    private val firestoreManager = FirestoreManager.getInstance()

    private val _utsResults = MutableLiveData<List<AcademicResult>>(emptyList())
    val utsResults: LiveData<List<AcademicResult>> = _utsResults

    private val _uasResults = MutableLiveData<List<AcademicResult>>(emptyList())
    val uasResults: LiveData<List<AcademicResult>> = _uasResults

    private val _transcriptSummaries = MutableLiveData<List<TranscriptSummary>>(emptyList())
    val transcriptSummaries: LiveData<List<TranscriptSummary>> = _transcriptSummaries

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /** All academic results loaded (used for transcript generation) */
    private var allAcademicResults: List<AcademicResult> = emptyList()

    fun loadAcademicData(npm: String, semester: Int) {
        if (npm.isEmpty()) {
            _errorMessage.value = "NPM tidak ditemukan"
            return
        }
        _isLoading.value = true
        loadAcademicResults(npm, semester)
    }

    private fun loadAcademicResults(npm: String, semester: Int) {
        firestoreManager.getAcademicResults(npm, semester) { results, error ->
            if (error != null) {
                _errorMessage.postValue(error)
                _utsResults.postValue(emptyList())
                _uasResults.postValue(emptyList())
                _transcriptSummaries.postValue(emptyList())
                _isLoading.postValue(false)
            } else {
                val list = results ?: emptyList()
                allAcademicResults = list
                // For demo: split between UTS and UAS based on some logic or show all in both tabs
                // In real implementation, AcademicResult should have a "type" field (UTS/UAS)
                _utsResults.postValue(list)
                _uasResults.postValue(list)
                _transcriptSummaries.postValue(generateTranscriptFromResults(list))
                _isLoading.postValue(false)
            }
        }
    }

    fun loadAllSemesters(npm: String) {
        _isLoading.value = true
        firestoreManager.getAllAcademicResults(npm) { results, error ->
            if (error != null) {
                _errorMessage.postValue(error)
                _utsResults.postValue(emptyList())
                _uasResults.postValue(emptyList())
                _transcriptSummaries.postValue(emptyList())
            } else {
                val list = results ?: emptyList()
                allAcademicResults = list
                _utsResults.postValue(list)
                _uasResults.postValue(list)
                _transcriptSummaries.postValue(generateTranscriptFromResults(list))
            }
            _isLoading.postValue(false)
        }
    }

    /**
     * Generate transcript summaries from academic_results.
     * Groups by semester, calculates IPK per semester.
     */
    private fun generateTranscriptFromResults(results: List<AcademicResult>): List<TranscriptSummary> {
        if (results.isEmpty()) return emptyList()

        return results
            .groupBy { it.semester }
            .map { (semester, courses) ->
                val sksSemester = courses.sumOf { it.sks }
                val totalMutu = courses.sumOf { it.mutu * it.sks }
                val ipkSemester = if (sksSemester > 0) totalMutu / sksSemester else 0.0

                TranscriptSummary(
                    semester = semester,
                    ipkSemester = ipkSemester,
                    sksSemester = sksSemester,
                    courses = courses
                )
            }
            .sortedByDescending { it.semester }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}