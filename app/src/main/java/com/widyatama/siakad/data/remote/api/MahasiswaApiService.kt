package com.widyatama.siakad.data.remote.api

import com.widyatama.siakad.data.model.Student
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * API service untuk endpoint mahasiswa di web-admin.
 */
interface MahasiswaApiService {

    /**
     * GET /api/mahasiswa/{npm} -> { data }
     */
    @GET("mahasiswa/{npm}")
    fun getMahasiswa(@Path("npm") npm: String): Call<ApiResponse<Student>>
}

