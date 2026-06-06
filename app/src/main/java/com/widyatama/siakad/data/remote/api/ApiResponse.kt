package com.widyatama.siakad.data.remote.api

/** Generic API response wrapper used by web-admin API. */
data class ApiResponse<T>(
    val data: T? = null,
    val message: String? = null,
    val success: Boolean? = null
)

