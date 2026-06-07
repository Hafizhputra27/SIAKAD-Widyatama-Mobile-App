package com.widyatama.siakad.data.model

sealed class SemesterResultItem {
    data class Header(
        val semester: Int,
        val ipkSemester: Double,
        val sksSemester: Int
    ) : SemesterResultItem()

    data class Course(val result: AcademicResult) : SemesterResultItem()
}
