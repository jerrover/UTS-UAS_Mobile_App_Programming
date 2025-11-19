package com.example.dermamindapp.ui.fragment

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

class CameraFragment : Fragment() {

    // Coroutine
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var skinAnalyzer: SkinAnalyzer

    private lateinit var previewView: PreviewView

    // Launcher untuk meminta izin kamera
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

    // Launcher untuk memilih gambar dari galeri
    private val pickMediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                Log.d(TAG, "Media dipilih: $uri")
                analyzeImage(uri)
            } else {
                Log.d(TAG, "Tidak ada media dipilih")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        previewView = view.findViewById(R.id.cameraPreview)
        val takePhotoButton: Button = view.findViewById(R.id.takePhotoButton)
        val uploadButton: Button = view.findViewById(R.id.uploadButton)

        takePhotoButton.setOnClickListener { takePhoto() }
        uploadButton.setOnClickListener { selectImageFromGallery() }

        // Minta izin kamera
        if (!allPermissionsGranted()) {
            requestPermissions()
        } else {
            startCamera()
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        skinAnalyzer = SkinAnalyzer(requireContext())

        return view
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
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA // Default ke kamera depan

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Gagal binding CameraX", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/DermaMind")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Gagal mengambil foto: ${exc.message}", exc)
                    Toast.makeText(requireContext(), "Gagal mengambil foto.", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Foto berhasil diambil: ${output.savedUri}"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                    output.savedUri?.let { analyzeImage(it) }
                }
            }
        )
    }

    private fun selectImageFromGallery() {
        pickMediaLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }



    private fun analyzeImage(uri: Uri) {
        scope.launch {
            try {
                // 1. Ubah URI ke Bitmap
                val bitmap = withContext(Dispatchers.IO) {
                    getBitmapFromUri(requireContext(), uri)
                }
                if (bitmap == null) {
                    Toast.makeText(requireContext(), "Gagal memuat gambar", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 2. Jalankan Analisis (di thread IO)
                val (probabilities, diagnosis) = withContext(Dispatchers.IO) {
                    skinAnalyzer.analyze(bitmap)
                }

                // 3. Format hasil
                val detectedConditions = diagnosis.filter { it.value }.keys.joinToString(", ")
                val resultString = if (detectedConditions.isEmpty()) "Kulit Sehat" else detectedConditions

                Log.d(TAG, "Hasil Analisis: $resultString")
                Log.d(TAG, "Probabilitas: $probabilities")

                // 4. Navigasi ke AnalysisResultFragment dengan membawa data
                findNavController().navigate(
                    CameraFragmentDirections.actionCameraFragmentToAnalysisResultFragment(
                        uri.toString(),
                        resultString
                    )
                )


            } catch (e: Exception) {
                Log.e(TAG, "Analisis gagal: ${e.message}", e)
                Toast.makeText(requireContext(), "Analisis gagal.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Helper untuk mengubah Uri menjadi Bitmap
    private fun getBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val source = ImageDecoder.createSource(context.contentResolver, uri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    decoder.isMutableRequired = true
                }.copy(Bitmap.Config.ARGB_8888, true) // <— paksa ARGB_8888
            } else {
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    .copy(Bitmap.Config.ARGB_8888, true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal konversi Uri ke Bitmap", e)
            null
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        scope.cancel() // Batalkan semua coroutine saat fragment dihancurkan
        // skinAnalyzer.close() // Panggil close di onDestroy dari Activity/VM
    }

    companion object {
        private const val TAG = "CameraFragment"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = mutableListOf(Manifest.permission.CAMERA).toTypedArray()
    }
}