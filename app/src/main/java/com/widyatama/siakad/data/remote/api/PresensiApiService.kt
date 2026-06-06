package com.widyatama.siakad.data.remote.api

import com.widyatama.siakad.data.model.ScanPresensiRequest
import com.widyatama.siakad.data.model.ScanPresensiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Retrofit API Service untuk endpoint presensi.
 */
interface PresensiApiService {

    /**
     * Primary flow: kirim data scan QR ke web admin API.
     * Header Authorization: Bearer <Firebase ID Token>
     */
    @POST("presensi/scan")
    fun scanPresensi(
        @Header("Authorization") authHeader: String,
        @Body request: ScanPresensiRequest
    ): Call<ScanPresensiResponse>
}
