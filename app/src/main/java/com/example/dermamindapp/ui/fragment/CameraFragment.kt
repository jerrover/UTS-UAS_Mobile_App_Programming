package com.example.dermamindapp.ui.fragment

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.dermamindapp.R
import com.example.dermamindapp.ml.SkinAnalyzer
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment(), SensorEventListener {

    // Scope untuk coroutine
    private val scope = CoroutineScope(Job() + Dispatchers.Main)

    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var skinAnalyzer: SkinAnalyzer
    private lateinit var previewView: PreviewView

    // Variabel Sensor
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null
    private lateinit var tvLightWarning: TextView

    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value) {
                    permissionGranted = false
                }
            }
            if (!permissionGranted) {
                Toast.makeText(requireContext(), "Izin kamera ditolak.", Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    private val pickMediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                analyzeImage(uri)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        previewView = view.findViewById(R.id.cameraPreview)
        tvLightWarning = view.findViewById(R.id.tvLightWarning)

        val takePhotoButton: Button = view.findViewById(R.id.takePhotoButton)
        val uploadButton: Button = view.findViewById(R.id.uploadButton)

        takePhotoButton.setOnClickListener { takePhoto() }
        uploadButton.setOnClickListener { selectImageFromGallery() }

        if (!allPermissionsGranted()) {
            requestPermissions()
        } else {
            startCamera()
        }

        // Inisialisasi Executor dan Analyzer di sini
        cameraExecutor = Executors.newSingleThreadExecutor()
        skinAnalyzer = SkinAnalyzer(requireContext())

        // Setup Sensor Cahaya
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        return view
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values[0]
            if (lux < 10) {
                tvLightWarning.visibility = View.VISIBLE
            } else {
                tvLightWarning.visibility = View.GONE
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Tidak dipakai
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Gagal binding CameraX", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DermaMind")
            }
        }
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            requireContext().contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(requireContext(), "Gagal foto.", Toast.LENGTH_SHORT).show()
                }
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { analyzeImage(it) }
                }
            }
        )
    }

    private fun selectImageFromGallery() {
        pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private fun analyzeImage(uri: Uri) {
        scope.launch {
            try {
                val bitmap = withContext(Dispatchers.IO) { getBitmapFromUri(requireContext(), uri) }
                if (bitmap == null) return@launch

                val (probabilities, diagnosis) = withContext(Dispatchers.IO) { skinAnalyzer.analyze(bitmap) }
                val detectedConditions = diagnosis.filter { it.value }.keys.joinToString(", ")
                val resultString = if (detectedConditions.isEmpty()) "Kulit Sehat" else detectedConditions

                findNavController().navigate(
                    CameraFragmentDirections.actionCameraFragmentToAnalysisResultFragment(
                        uri.toString(), resultString
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Analisis gagal", e)
                Toast.makeText(requireContext(), "Analisis gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }.copy(Bitmap.Config.ARGB_8888, true)
            } else {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }?.copy(Bitmap.Config.ARGB_8888, true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memuat bitmap", e)
            null
        }
    }

    // --- PERBAIKAN: Gunakan onDestroyView untuk membersihkan Resource View ---
    override fun onDestroyView() {
        super.onDestroyView()

        // 1. Tutup SkinAnalyzer (PENTING untuk mencegah Memory Leak Native)
        if (::skinAnalyzer.isInitialized) {
            skinAnalyzer.close()
        }

        // 2. Matikan Executor Kamera
        if (::cameraExecutor.isInitialized) {
            cameraExecutor.shutdown()
        }
    }

    // --- PERBAIKAN: Gunakan onDestroy untuk membersihkan Scope Fragment ---
    override fun onDestroy() {
        super.onDestroy()
        // Batalkan semua coroutine yang berjalan
        scope.cancel()
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf(Manifest.permission.CAMERA).toTypedArray()
    }
}