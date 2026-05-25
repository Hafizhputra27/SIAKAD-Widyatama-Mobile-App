package com.widyatama.siakad.ui.attendance

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.widyatama.siakad.data.model.Presensi
import com.widyatama.siakad.data.remote.FirestoreManager
import com.widyatama.siakad.databinding.ActivityQrScannerBinding
import com.google.firebase.Timestamp
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import org.json.JSONObject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class QrScannerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQrScannerBinding

    private val prefs by lazy {
        getSharedPreferences("SIAKAD_PREFS", Context.MODE_PRIVATE)
    }

    private val firestoreManager = FirestoreManager.getInstance()
    private lateinit var cameraExecutor: ExecutorService
    private var isProcessing = false
    private var camera: Camera? = null
    private var currentZoomRatio = 1.0f
    private var maxZoomRatio = 1.0f
    private var minZoomRatio = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraExecutor = Executors.newSingleThreadExecutor()

        setupClickListeners()
        setupScanFrame()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener {
            finish()
        }

        binding.btnZoomIn.setOnClickListener {
            zoomIn()
        }

        binding.btnZoomOut.setOnClickListener {
            zoomOut()
        }
    }

    private fun setupScanFrame() {
        binding.scanFrameContainer.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.scanFrameContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                updateOverlay()
            }
        })
    }

    private fun updateOverlay() {
        binding.scanFrameContainer.post {
            val frameLocation = IntArray(2)
            binding.scanFrameContainer.getLocationOnScreen(frameLocation)
            val frameX = frameLocation[0]
            val frameY = frameLocation[1]
            val frameWidth = binding.scanFrameContainer.width
            val frameHeight = binding.scanFrameContainer.height
            val screenWidth = binding.root.width
            val screenHeight = binding.root.height

            Log.d("QrScanner", "Frame: x=$frameX, y=$frameY, w=$frameWidth, h=$frameHeight")
            Log.d("QrScanner", "Screen: w=$screenWidth, h=$screenHeight")

            val statusBarHeight = getStatusBarHeight()
            val adjustedY = frameY - statusBarHeight

            binding.overlayTop.layoutParams = binding.overlayTop.layoutParams.apply {
                height = adjustedY
            }

            binding.overlayBottom.layoutParams = binding.overlayBottom.layoutParams.apply {
                height = screenHeight - adjustedY - frameHeight
            }

            binding.overlayLeft.layoutParams = binding.overlayLeft.layoutParams.apply {
                width = frameX
                height = frameHeight
            }

            binding.overlayRight.layoutParams = binding.overlayRight.layoutParams.apply {
                width = screenWidth - frameX - frameWidth
                height = frameHeight
            }

            binding.overlayTop.requestLayout()
            binding.overlayBottom.requestLayout()
            binding.overlayLeft.requestLayout()
            binding.overlayRight.requestLayout()
        }
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
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
                    minZoomRatio = state.minZoomRatio
                    maxZoomRatio = state.maxZoomRatio
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
                                    processQrCode(value)
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
            json.has("mataKuliahId") && json.has("pertemuanId")
        } catch (e: Exception) {
            false
        }
    }

    private fun processQrCode(qrContent: String) {
        try {
            val json = JSONObject(qrContent)
            val mataKuliahId = json.getString("mataKuliahId")
            val pertemuanId = json.getString("pertemuanId")

            val npm = prefs.getString("NPM", "") ?: ""
            if (npm.isEmpty()) {
                val resultIntent = Intent()
                resultIntent.putExtra("status", "FAILED")
                resultIntent.putExtra("error", "NPM tidak ditemukan. Silakan login ulang.")
                setResult(RESULT_OK, resultIntent)
                finish()
                return
            }

            val presensi = Presensi(
                npm = npm,
                mataKuliahId = mataKuliahId,
                pertemuanId = pertemuanId,
                status = "HADIR",
                waktu = Timestamp.now(),
                metodeScan = "QR_CODE"
            )

            firestoreManager.recordPresensi(presensi) { success, error ->
                val resultIntent = Intent()
                if (success) {
                    resultIntent.putExtra("status", "SUCCESS")
                    resultIntent.putExtra("mataKuliah", mataKuliahId)
                } else {
                    resultIntent.putExtra("status", "FAILED")
                    resultIntent.putExtra("error", error ?: "Gagal mencatat presensi")
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        } catch (e: Exception) {
            val resultIntent = Intent()
            resultIntent.putExtra("status", "FAILED")
            resultIntent.putExtra("error", "Format QR Code tidak valid")
            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
