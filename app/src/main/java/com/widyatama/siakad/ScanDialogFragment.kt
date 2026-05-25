package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.myapplication.databinding.DialogScanErrorBinding
import com.example.myapplication.databinding.DialogScanQrBinding
import com.example.myapplication.databinding.DialogScanSuccessBinding
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class ScanDialogFragment : DialogFragment() {

    private var _binding: DialogScanQrBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var cameraExecutor: ExecutorService
    private var isScanning = true
    private val scanner = BarcodeScanning.getClient()
    
    private var camera: Camera? = null
    private var currentZoomRatio = 1f

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "Izin kamera diperlukan untuk scan", Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBlurDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogScanQrBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        cameraExecutor = Executors.newSingleThreadExecutor()

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnZoomIn.setOnClickListener {
            camera?.cameraControl?.let { control ->
                val maxZoom = camera?.cameraInfo?.zoomState?.value?.maxZoomRatio ?: 1f
                if (currentZoomRatio < maxZoom) {
                    currentZoomRatio += 0.5f
                    if (currentZoomRatio > maxZoom) currentZoomRatio = maxZoom
                    control.setZoomRatio(currentZoomRatio)
                    updateZoomText()
                }
            }
        }

        binding.btnZoomOut.setOnClickListener {
            camera?.cameraControl?.let { control ->
                val minZoom = camera?.cameraInfo?.zoomState?.value?.minZoomRatio ?: 1f
                if (currentZoomRatio > minZoom) {
                    currentZoomRatio -= 0.5f
                    if (currentZoomRatio < minZoom) currentZoomRatio = minZoom
                    control.setZoomRatio(currentZoomRatio)
                    updateZoomText()
                }
            }
        }

        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun allPermissionsGranted() = ContextCompat.checkSelfPermission(
        requireContext(), Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, cameraSelector, preview, imageAnalyzer
                )
                
                // Initialize zoom text
                updateZoomText()

            } catch (exc: Exception) {
                Log.e("ScanDialogFragment", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty() && isScanning) {
                        val barcode = barcodes[0]
                        if (barcode.valueType == Barcode.TYPE_TEXT || barcode.valueType == Barcode.TYPE_URL) {
                            // Check if barcode content is valid (production logic)
                            val qrContent = barcode.displayValue
                            if (isValidQr(qrContent)) {
                                isScanning = false
                                if (isAdded) {
                                    showSuccessDialog()
                                }
                            } else {
                                // If QR is scanned but content doesn't match expected format
                                isScanning = false
                                if (isAdded) {
                                    showErrorDialog()
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("ScanDialogFragment", "Barcode scanning failed", it)
                    isScanning = false
                    if (isAdded) {
                        showErrorDialog()
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun isValidQr(content: String?): Boolean {
        if (content == null) return false
        // Production logic: QR must contain Widyatama domain or follow specific attendance format
        // Example: https://presensi.widyatama.ac.id/scan/abc123
        return content.contains("widyatama.ac.id", ignoreCase = true) || 
               content.contains("WIDYATAMA-PRESENSI", ignoreCase = true)
    }

    private fun updateZoomText() {
        val displayZoom = String.format(Locale.US, "%.0fX", currentZoomRatio)
        binding.tvZoomLevel.text = displayZoom
    }

    private fun setupDialogWindow(window: android.view.Window?) {
        window?.let {
            it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            
            // Menggunakan MATCH_PARENT untuk memastikan layout root bisa mengisi seluruh layar
            // sehingga margin/padding di XML bekerja dengan benar tanpa terkompresi.
            it.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )

            // Fallback dimAmount 0.7f untuk semua versi agar fokus
            it.setDimAmount(0.7f)
            
            // Pengaturan Blur untuk Android 12+ (API 31)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                it.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                it.attributes.blurBehindRadius = 30
            }
        }
    }

    private fun showSuccessDialog() {
        val successBinding = DialogScanSuccessBinding.inflate(layoutInflater)
        val successDialog = androidx.appcompat.app.AppCompatDialog(requireContext(), R.style.CustomBlurDialog)
        successDialog.setContentView(successBinding.root)
        successDialog.setCancelable(false)
        
        // Tombol Silang (X)
        successBinding.btnDone.setOnClickListener {
            successDialog.dismiss()
            this.dismiss()
        }

        // Tombol "Selesai"
        successBinding.btnFinish.setOnClickListener {
            successDialog.dismiss()
            this.dismiss()
        }

        successDialog.show()
        setupDialogWindow(successDialog.window)
    }

    private fun showErrorDialog() {
        val errorBinding = DialogScanErrorBinding.inflate(layoutInflater)
        val errorDialog = androidx.appcompat.app.AppCompatDialog(requireContext(), R.style.CustomBlurDialog)
        errorDialog.setContentView(errorBinding.root)
        errorDialog.setCancelable(false)

        errorBinding.btnRetry.setOnClickListener {
            errorDialog.dismiss()
            isScanning = true 
        }

        errorBinding.btnExit.setOnClickListener {
            errorDialog.dismiss()
            this.dismiss()
        }

        errorBinding.btnClose.setOnClickListener {
            errorDialog.dismiss()
            this.dismiss()
        }

        errorDialog.show()
        setupDialogWindow(errorDialog.window)
    }

    override fun onStart() {
        super.onStart()
        setupDialogWindow(dialog?.window)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        scanner.close()
        _binding = null
    }

    companion object {
        const val TAG = "ScanDialogFragment"
        fun newInstance() = ScanDialogFragment()
    }
}