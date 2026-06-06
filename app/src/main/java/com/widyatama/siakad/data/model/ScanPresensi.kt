package com.widyatama.siakad.data.model

import com.google.gson.annotations.SerializedName

/**
 * Request body untuk POST /api/presensi/scan
 */
data class ScanPresensiRequest(
    @SerializedName("token")
    val token: String,

    @SerializedName("courseId")
    val courseId: String,

    @SerializedName("pertemuanId")
    val pertemuanId: String,

    @SerializedName("npm")
    val npm: String,

    @SerializedName("scanMethod")
    val scanMethod: String = "QR_SCAN",

    @SerializedName("deviceInfo")
    val deviceInfo: String = ""
)

/**
 * Response dari POST /api/presensi/scan
 */
data class ScanPresensiResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String? = null,

    @SerializedName("error")
    val error: String? = null,

    @SerializedName("presensiId")
    val presensiId: String? = null
)
