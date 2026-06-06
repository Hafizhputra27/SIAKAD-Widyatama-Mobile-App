package com.widyatama.siakad.data.model

/**
 * Lightweight transcript summary generated from academic_results.
 * Not stored in Firestore — computed on-the-fly by the mobile app.
 */
data class TranscriptSummary(
    val semester: Int = 0,
    val ipkSemester: Double = 0.0,
    val sksSemester: Int = 0,
    val courses: List<AcademicResult> = emptyList()
)