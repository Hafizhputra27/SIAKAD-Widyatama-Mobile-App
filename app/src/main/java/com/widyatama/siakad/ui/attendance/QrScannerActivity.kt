package com.widyatama.siakad.ui.attendance

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.widyatama.siakad.data.local.SharedPrefManager
import com.widyatama.siakad.data.model.ScanPresensiRequest
import com.widyatama.siakad.data.remote.FirestoreManager
import com.widyatama.siakad.data.remote.RetrofitClient
import com.widyatama.siakad.databinding.ActivityQrScannerBinding
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@OptIn(androidx.camera.core.ExperimentalGetImage::class)
class QrScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrScannerBinding

    private val sharedPref by lazy { SharedPrefManager.getInstance(this) }
    private val firestoreManager = FirestoreManager.getInstance()
    private lateinit var cameraExecutor: ExecutorService
    private var isProcessing = false
    private var camera: Camera? = null
    private var currentZoomRatio = 1.0f

    private val TAG = "QrScannerActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        setupClickListeners()
        setupOverlayButtons()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }
    }

    private fun setupClickListeners() {
        binding.btnCancel.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }

        binding.btnZoomIn.setOnClickListener { zoomIn() }
        binding.btnZoomOut.setOnClickListener { zoomOut() }
    }

    private fun setupOverlayButtons() {
        // Success overlay
        binding.layoutSuccess.btnDone.setOnClickListener { finishWithSuccess() }
        binding.layoutSuccess.btnFinish.setOnClickListener { finishWithSuccess() }

        // Error overlay
        binding.layoutError.btnRetry.setOnClickListener { resumeScanning() }
        binding.layoutError.btnExit.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
        binding.layoutError.btnClose.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun finishWithSuccess() {
        val resultIntent = Intent().apply {
            putExtra("status", "SUCCESS")
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }

    private fun zoomIn() {
        camera?.let { cam ->
            val zoomState = cam.cameraInfo.zoomState.value
            zoomState?.let { state ->
                val newZoom = (currentZoomRatio + 0.5f).coerceAtMost(state.maxZoomRatio)
                setZoom(newZoom)
            }
        }
    }

    private fun zoomOut() {
        camera?.let { cam ->
            val zoomState = cam.cameraInfo.zoomState.value
            zoomState?.let { state ->
                val newZoom = (currentZoomRatio - 0.5f).coerceAtLeast(state.minZoomRatio)
                setZoom(newZoom)
            }
        }
    }

    private fun setZoom(zoomRatio: Float) {
        camera?.let { cam ->
            val zoomState = cam.cameraInfo.zoomState.value
            zoomState?.let { state ->
                currentZoomRatio = zoomRatio.coerceIn(state.minZoomRatio, state.maxZoomRatio)
                cam.cameraControl.setZoomRatio(currentZoomRatio)
                binding.tvZoomLevel.text = String.format("%.1fx", currentZoomRatio)

                binding.btnZoomOut.isEnabled = currentZoomRatio > state.minZoomRatio
                binding.btnZoomIn.isEnabled = currentZoomRatio < state.maxZoomRatio

                binding.btnZoomOut.alpha = if (currentZoomRatio > state.minZoomRatio) 1f else 0.5f
                binding.btnZoomIn.alpha = if (currentZoomRatio < state.maxZoomRatio) 1f else 0.5f
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Izin kamera diperlukan", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.previewView.surfaceProvider
            }

            val options = BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
            val scanner = BarcodeScanning.getClient(options)

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        if (!isProcessing) {
                            processImage(scanner, imageProxy)
                        } else {
                            imageProxy.close()
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)

                camera?.cameraInfo?.zoomState?.observe(this) { state ->
                    currentZoomRatio = state.zoomRatio
                    binding.tvZoomLevel.text = String.format("%.1fx", currentZoomRatio)
                }

            } catch (e: Exception) {
                Toast.makeText(this, "Gagal memulai kamera", Toast.LENGTH_SHORT).show()
                finish()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @androidx.camera.core.ExperimentalGetImage
    private fun processImage(scanner: BarcodeScanner, imageProxy: androidx.camera.core.ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { value ->
                            if (!isProcessing && isValidQrCode(value)) {
                                isProcessing = true
                                runOnUiThread {
                                    processQrResult(value)
                                }
                            }
                        }
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun isValidQrCode(content: String): Boolean {
        return try {
            val json = JSONObject(content)
            json.has("token") && json.has("courseId") && json.has("pertemuanId") && json.has("expiresAt")
        } catch (e: Exception) {
            false
        }
    }

    // ============================================================
    // ENTRY POINT — dipanggil oleh ML Kit saat QR berhasil di-scan
    // ============================================================
    private fun processQrResult(rawValue: String) {
        isProcessing = true
        pauseScanning()

        Log.d(TAG, "processQrResult() called")

        // Step 1: Parse JSON
        val token: String
        val courseId: String
        val pertemuanId: String
        val expiresAt: Long

        try {
            val payload = JSONObject(rawValue)
            token = payload.getString("token")
            courseId = payload.getString("courseId")
            pertemuanId = payload.getString("pertemuanId")
            // Support both Unix timestamp (Long) and ISO 8601 string (web admin format)
            expiresAt = try {
                payload.getLong("expiresAt")
            } catch (e: JSONException) {
                val isoString = payload.getString("expiresAt")
                parseIsoDateToEpochSeconds(isoString)
            }
        } catch (e: JSONException) {
            Log.e(TAG, "JSON parse error: ${e.message}")
            showErrorState(
                title = getString(com.widyatama.siakad.R.string.qr_invalid_title),
                message = "QR Code ini bukan dari SIAKAD Widyatama"
            )
            return
        }

        Log.d(TAG, "QR parsed: token=$token, courseId=$courseId, pertemuanId=$pertemuanId, expiresAt=$expiresAt")

        // Step 2: Cek expiry secara lokal
        val nowSeconds = System.currentTimeMillis() / 1000
        if (nowSeconds > expiresAt) {
            Log.w(TAG, "QR expired: now=$nowSeconds, expiresAt=$expiresAt")
            showErrorState(
                title = getString(com.widyatama.siakad.R.string.qr_expired_title),
                message = "Waktu scan sudah habis. Minta dosen generate QR baru."
            )
            return
        }

        // Step 3: Ambil NPM dari session
        val npm = sharedPref.npm
        if (npm.isEmpty()) {
            Log.e(TAG, "Session invalid: npm is empty")
            showErrorState(
                title = "Session Tidak Valid",
                message = "Silakan logout dan login ulang"
            )
            return
        }

        Log.d(TAG, "Session npm='$npm'")

        // Step 4: Validasi ke Firestore
        showLoadingState(getString(com.widyatama.siakad.R.string.qr_verifying))
        validateWithFirestore(token, courseId, pertemuanId, npm, token)
    }

    // ============================================================
    // VALIDASI FIRESTORE
    // ============================================================
    private fun validateWithFirestore(
        token: String,
        courseId: String,
        pertemuanId: String,
        npm: String,
        qrToken: String
    ) {
        val db = FirebaseFirestore.getInstance()

        db.collection("pertemuan").document(pertemuanId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    Log.e(TAG, "Pertemuan document not found: $pertemuanId")
                    showErrorState("Sesi Tidak Ditemukan", "Data pertemuan tidak ada. Hubungi dosen.")
                    return@addOnSuccessListener
                }

                // Validasi 1: QR masih aktif?
                val isQrActive = doc.getBoolean("isQrActive") ?: false
                if (!isQrActive) {
                    Log.w(TAG, "QR is not active for pertemuan: $pertemuanId")
                    showErrorState(
                        getString(com.widyatama.siakad.R.string.qr_inactive_title),
                        "Sesi QR sudah ditutup oleh dosen."
                    )
                    return@addOnSuccessListener
                }

                // Validasi 2: Token cocok?
                val storedToken = doc.getString("qrToken") ?: ""
                if (storedToken != token) {
                    Log.w(TAG, "Token mismatch: stored=$storedToken, scanned=$token")
                    showErrorState(
                        getString(com.widyatama.siakad.R.string.qr_invalid_title),
                        "QR Code ini sudah tidak berlaku."
                    )
                    return@addOnSuccessListener
                }

                // Validasi 3: Mahasiswa enrolled?
                @Suppress("UNCHECKED_CAST")
                val enrolledNpms = doc.get("enrolledNpms") as? List<String> ?: emptyList()
                if (!enrolledNpms.contains(npm)) {
                    Log.w(TAG, "NPM $npm not enrolled. Enrolled: $enrolledNpms")
                    showErrorState(
                        getString(com.widyatama.siakad.R.string.qr_not_enrolled_title),
                        "Anda tidak terdaftar di mata kuliah ini."
                    )
                    return@addOnSuccessListener
                }

                Log.d(TAG, "All pertemuan validations passed")
                checkDuplicatePresensi(db, pertemuanId, courseId, npm, qrToken)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore get pertemuan failed: ${e.message}")
                showErrorState(
                    getString(com.widyatama.siakad.R.string.qr_error_network),
                    "Periksa koneksi internet Anda.\n${e.message}"
                )
            }
    }

    // ============================================================
    // CEK DUPLICATE
    // ============================================================
    private fun checkDuplicatePresensi(
        db: FirebaseFirestore,
        pertemuanId: String,
        courseId: String,
        npm: String,
        qrToken: String
    ) {
        db.collection("presensi")
            .whereEqualTo("pertemuanId", pertemuanId)
            .whereEqualTo("npm", npm)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    Log.i(TAG, "Duplicate presensi found for npm=$npm, pertemuanId=$pertemuanId")
                    showSuccessState(
                        title = getString(com.widyatama.siakad.R.string.qr_duplicate_title),
                        message = "Anda sudah tercatat hadir di pertemuan ini sebelumnya."
                    )
                    return@addOnSuccessListener
                }

                Log.d(TAG, "No duplicate found, submitting presensi via API...")
                submitPresensiViaApi(db, pertemuanId, courseId, npm, qrToken)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Check duplicate failed: ${e.message}")
                showErrorState("Gagal Cek Data", "Periksa koneksi internet Anda.\n${e.message}")
            }
    }

    // ============================================================
    // PRIMARY: SUBMIT PRESENSI VIA REST API
    // ============================================================
    private fun submitPresensiViaApi(
        db: FirebaseFirestore,
        pertemuanId: String,
        courseId: String,
        npm: String,
        qrToken: String
    ) {
        showLoadingState("Mengirim ke server...")

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.w(TAG, "No Firebase user, falling back to Firestore")
            submitPresensiViaFirestore(db, pertemuanId, courseId, npm, qrToken)
            return
        }

        user.getIdToken(false)
            .addOnSuccessListener { result ->
                val token = result.token
                if (token == null) {
                    Log.w(TAG, "Firebase token is null, falling back to Firestore")
                    submitPresensiViaFirestore(db, pertemuanId, courseId, npm, qrToken)
                    return@addOnSuccessListener
                }

                val request = ScanPresensiRequest(
                    token = qrToken,
                    courseId = courseId,
                    pertemuanId = pertemuanId,
                    npm = npm,
                    scanMethod = "QR_SCAN",
                    deviceInfo = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
                )

                val call = RetrofitClient.presensiApiService.scanPresensi(
                    authHeader = "Bearer $token",
                    request = request
                )

                call.enqueue(object : Callback<com.widyatama.siakad.data.model.ScanPresensiResponse> {
                    override fun onResponse(
                        call: Call<com.widyatama.siakad.data.model.ScanPresensiResponse>,
                        response: Response<com.widyatama.siakad.data.model.ScanPresensiResponse>
                    ) {
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null && body.success) {
                                Log.i(TAG, "API scan success: ${body.message}")
                                // Update enrolledNpms locally as well for consistency
                                db.collection("pertemuan").document(pertemuanId)
                                    .update("enrolledNpms", FieldValue.arrayUnion(npm))
                                    .addOnSuccessListener {
                                        Log.d(TAG, "EnrolledNpms updated via Firestore after API success")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(TAG, "Failed to update enrolledNpms after API success: ${e.message}")
                                    }
                                showSuccessState(
                                    title = getString(com.widyatama.siakad.R.string.qr_success_title),
                                    message = body.message ?: "Kehadiran Anda di $courseId telah tercatat.\nTerima kasih!"
                                )
                            } else {
                                val errorMsg = body?.error ?: body?.message ?: "Presensi gagal dicatat"
                                Log.w(TAG, "API returned success=false: $errorMsg")
                                handleApiError(db, errorMsg, pertemuanId, courseId, npm, qrToken)
                            }
                        } else {
                            val errorBody = response.errorBody()?.string()
                            val errorMsg = errorBody ?: "HTTP ${response.code()}"
                            Log.w(TAG, "API HTTP error: ${response.code()} - $errorMsg")

                            when (response.code()) {
                                401, 403 -> showErrorState(
                                    "Sesi Tidak Valid",
                                    "Sesi login Anda sudah habis. Silakan logout dan login ulang."
                                )
                                404 -> showErrorState(
                                    "Endpoint Tidak Ditemukan",
                                    "Server belum mendukung fitur ini. Menyimpan ke lokal..."
                                ).also {
                                    submitPresensiViaFirestore(db, pertemuanId, courseId, npm, qrToken)
                                }
                                500, 502, 503, 504 -> {
                                    showLoadingState("Server bermasalah. Menyimpan ke lokal...")
                                    submitPresensiViaFirestore(db, pertemuanId, courseId, npm, qrToken)
                                }
                                else -> {
                                    showLoadingState("Server merespons error. Menyimpan ke lokal...")
                                    submitPresensiViaFirestore(db, pertemuanId, courseId, npm, qrToken)
                                }
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<com.widyatama.siakad.data.model.ScanPresensiResponse>,
                        t: Throwable
                    ) {
                        Log.e(TAG, "API call failure: ${t.message}")
                        if (t is IOException) {
                            showLoadingState("Server tidak merespons. Menyimpan ke lokal...")
                            submitPresensiViaFirestore(db, pertemuanId, courseId, npm, qrToken)
                        } else {
                            showErrorState("Gagal Menyimpan", "Terjadi kesalahan tak terduga.\n${t.message}")
                        }
                    }
                })
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get Firebase ID token: ${e.message}")
                submitPresensiViaFirestore(db, pertemuanId, courseId, npm, qrToken)
            }
    }

    /**
     * Handle API error message yang spesifik dari server.
     * Jika pesan mengandung kata kunci tertentu, tampilkan error state.
     * Jika tidak, fallback ke Firestore direct write.
     */
    private fun handleApiError(
        db: FirebaseFirestore,
        errorMsg: String,
        pertemuanId: String,
        courseId: String,
        npm: String,
        qrToken: String
    ) {
        val lower = errorMsg.lowercase()
        when {
            lower.contains("expired") || lower.contains("kedaluwarsa") -> showErrorState(
                getString(com.widyatama.siakad.R.string.qr_expired_title),
                "Waktu scan sudah habis. Minta dosen generate QR baru."
            )
            lower.contains("not active") || lower.contains("tidak aktif") || lower.contains("ditutup") -> showErrorState(
                getString(com.widyatama.siakad.R.string.qr_inactive_title),
                "Sesi QR sudah ditutup oleh dosen."
            )
            lower.contains("not enrolled") || lower.contains("tidak terdaftar") -> showErrorState(
                getString(com.widyatama.siakad.R.string.qr_not_enrolled_title),
                "Anda tidak terdaftar di mata kuliah ini."
            )
            lower.contains("already") || lower.contains("sudah absen") || lower.contains("sudah tercatat") -> showSuccessState(
                title = getString(com.widyatama.siakad.R.string.qr_duplicate_title),
                message = "Anda sudah tercatat hadir di pertemuan ini sebelumnya."
            )
            else -> {
                // Unknown error from API — fallback to Firestore
                showLoadingState("Server merespons error. Menyimpan ke lokal...")
                submitPresensiViaFirestore(db, pertemuanId, courseId, npm, qrToken)
            }
        }
    }

    // ============================================================
    // FALLBACK: SUBMIT PRESENSI VIA FIRESTORE DIRECT WRITE
    // ============================================================
    private fun submitPresensiViaFirestore(
        db: FirebaseFirestore,
        pertemuanId: String,
        courseId: String,
        npm: String,
        qrToken: String
    ) {
        val mahasiswaName = sharedPref.name // Get name from session
        val presensiData = hashMapOf(
            "npm" to npm,
            "mataKuliahId" to courseId,
            "courseId" to courseId,
            "pertemuanId" to pertemuanId,
            "status" to "HADIR",
            "scanMethod" to "QR_SCAN",
            "timestamp" to FieldValue.serverTimestamp(),
            "mahasiswaName" to mahasiswaName,
            "deviceInfo" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
        )

        db.collection("presensi")
            .add(presensiData)
            .addOnSuccessListener { docRef ->
                Log.i(TAG, "Presensi submitted via Firestore fallback: ${docRef.id}")

                // Update enrolledNpms in pertemuan document
                db.collection("pertemuan").document(pertemuanId)
                    .update("enrolledNpms", FieldValue.arrayUnion(npm))
                    .addOnSuccessListener {
                        Log.d(TAG, "EnrolledNpms updated for pertemuan $pertemuanId")
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Failed to update enrolledNpms: ${e.message}")
                    }

                showSuccessState(
                    title = getString(com.widyatama.siakad.R.string.qr_success_title),
                    message = "Kehadiran Anda telah tercatat (tersimpan lokal).\nTerima kasih!"
                )
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore fallback submit failed: ${e.message}")
                showErrorState("Gagal Menyimpan", "Absensi tidak tersimpan. Periksa koneksi internet.\n${e.message}")
            }
    }

    // ============================================================
    // UI STATE HELPERS
    // ============================================================
    private fun showLoadingState(message: String) {
        runOnUiThread {
            binding.layoutLoading.visibility = View.VISIBLE
            binding.cardScanner.visibility = View.VISIBLE // tetap terlihat di belakang loading
            binding.layoutSuccess.root.visibility = View.GONE
            binding.layoutError.root.visibility = View.GONE
            binding.tvLoadingMessage.text = message
        }
    }

    private fun showSuccessState(title: String, message: String, courseName: String = "", courseTime: String = "") {
        runOnUiThread {
            binding.layoutLoading.visibility = View.GONE
            binding.cardScanner.visibility = View.GONE
            binding.layoutSuccess.root.visibility = View.VISIBLE
            binding.layoutError.root.visibility = View.GONE

            binding.layoutSuccess.tvCourseName.text = courseName
            binding.layoutSuccess.tvCourseTime.text = courseTime

            // Reset processing agar scanner bisa scan lagi
            isProcessing = false

            // Auto-kembali setelah 3 detik
            Handler(Looper.getMainLooper()).postDelayed({
                finishWithSuccess()
            }, 3000)
        }
    }

    private fun showErrorState(title: String, message: String) {
        runOnUiThread {
            binding.layoutLoading.visibility = View.GONE
            binding.cardScanner.visibility = View.GONE
            binding.layoutSuccess.root.visibility = View.GONE
            binding.layoutError.root.visibility = View.VISIBLE

            binding.layoutError.tvErrorTitle.text = title
            if (message.isNotEmpty()) {
                binding.layoutError.tvErrorMessage.text = message
                binding.layoutError.tvErrorMessage.visibility = View.VISIBLE
            } else {
                binding.layoutError.tvErrorMessage.visibility = View.GONE
            }

            // Reset processing agar scanner bisa scan lagi setelah error
            isProcessing = false
        }
    }

    private fun pauseScanning() {
        isProcessing = true
    }

    private fun resumeScanning() {
        isProcessing = false
        runOnUiThread {
            binding.layoutSuccess.root.visibility = View.GONE
            binding.layoutError.root.visibility = View.GONE
            binding.layoutLoading.visibility = View.GONE
            binding.cardScanner.visibility = View.VISIBLE
        }
    }

    /**
     * Parse ISO 8601 date string (e.g. "2026-06-04T10:30:00Z") to Unix epoch seconds.
     * Supports both with and without milliseconds.
     */
    private fun parseIsoDateToEpochSeconds(isoString: String): Long {
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val date = sdf.parse(isoString)
            date?.time?.div(1000) ?: 0L
        } catch (e: Exception) {
            try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                val date = sdf.parse(isoString)
                date?.time?.div(1000) ?: 0L
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to parse ISO date: $isoString")
                0L
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
