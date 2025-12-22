package com.example.dermamindapp.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class SkinAnalyzer(
    private val context: Context,
    private val modelName: String = "skincare_model_v4_float32_flex.tflite"
) {

    private var interpreter: Interpreter
    private val imageProcessor: ImageProcessor

    companion object {
        private const val INPUT_SIZE = 224

        // Pastikan list ini sesuai urutan output model Anda saat training
        private val LABELS = listOf(
            "Jerawat_Aktif", "Kulit_Berminyak", "Kemerahan", "Tekstur_Pori", "Kulit_Sehat"
        )
    }

    init {
        val options = Interpreter.Options()
        // Opsi thread untuk performa
        options.setNumThreads(4)

        try {
            interpreter = Interpreter(loadModelFile(), options)
        } catch (e: Exception) {
            Log.e("SkinAnalyzer", "Error init model: ${e.message}")
            throw e
        }

        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(INPUT_SIZE, INPUT_SIZE, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(127.5f, 127.5f)) // Normalisasi range -1 ke 1
            .build()
    }

    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    fun analyze(bitmap: Bitmap): SkinResult {
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        val processedImage = imageProcessor.process(tensorImage)

        // 1. Baca ukuran output model secara dinamis (Anti-Crash)
        val outputTensor = interpreter.getOutputTensor(0)
        val outputShape = outputTensor.shape() // misal [1, 5]
        val outputSize = outputShape[1] // ambil angka 5

        // 2. Siapkan buffer sesuai ukuran model
        val outputBuffer = Array(1) { FloatArray(outputSize) }

        // 3. Jalankan
        interpreter.run(processedImage.buffer, outputBuffer)

        return postprocess(outputBuffer[0])
    }

    private fun postprocess(outputs: FloatArray): SkinResult {
        val probabilities = mutableMapOf<String, Float>()
        val diagnosis = mutableMapOf<String, Boolean>()

        // Mapping hasil ke Label
        // Menggunakan 'zip' agar aman jika jumlah label vs output beda
        LABELS.zip(outputs.toList()).forEach { (label, score) ->
            probabilities[label] = score
            // Threshold simpel 0.5, bisa disesuaikan
            diagnosis[label] = score > 0.45f
        }

        return SkinResult(probabilities, diagnosis)
    }

    fun close() {
        interpreter.close()
    }
}