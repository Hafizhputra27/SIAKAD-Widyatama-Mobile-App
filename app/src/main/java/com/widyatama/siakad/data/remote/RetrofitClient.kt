package com.widyatama.siakad.data.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.widyatama.siakad.core.constants.AppConstants
import com.widyatama.siakad.data.remote.api.PresensiApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton Retrofit client untuk komunikasi REST API ke web admin.
 * Otomatis menambahkan Firebase ID Token ke header Authorization.
 */
object RetrofitClient {

    private const val TAG = "RetrofitClient"
    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 15L

    /**
     * Interceptor yang menambahkan Firebase ID Token ke setiap request.
     * Token diambil dari FirebaseAuth.currentUser.getIdToken(false).
     * Jika token null, request tetap dilanjutkan (server akan return 401).
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val user = FirebaseAuth.getInstance().currentUser

        val token = try {
            user?.getIdToken(false)?.result?.token
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get Firebase ID token: ${e.message}")
            null
        }

        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .method(originalRequest.method, originalRequest.body)
                .build()
        } else {
            originalRequest.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .method(originalRequest.method, originalRequest.body)
                .build()
        }

        chain.proceed(newRequest)
    }

    /**
     * Logging interceptor untuk debug. Hanya aktif di DEBUG build.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (com.widyatama.siakad.BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(AppConstants.BASE_API_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val presensiApiService: PresensiApiService by lazy {
        retrofit.create(PresensiApiService::class.java)
    }

    // Mahasiswa API (web-admin) - detail mahasiswa
    val mahasiswaApiService: com.widyatama.siakad.data.remote.api.MahasiswaApiService by lazy {
        retrofit.create(com.widyatama.siakad.data.remote.api.MahasiswaApiService::class.java)
    }

    /**
     * Fungsi helper untuk membuat auth header secara synchronous.
     * Digunakan oleh QrScannerActivity sebelum memanggil API.
     * @return "Bearer <token>" atau null jika tidak ada user.
     */
    fun getBearerToken(): String? {
        return try {
            val user = FirebaseAuth.getInstance().currentUser
            val token = user?.getIdToken(false)?.result?.token
            token?.let { "Bearer $it" }
        } catch (e: Exception) {
            Log.e(TAG, "getBearerToken failed: ${e.message}")
            null
        }
    }
}
