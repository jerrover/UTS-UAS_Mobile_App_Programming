package com.example.dermamindapp.ml

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.flex.FlexDelegate
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
        private const val INPUT_IMAGE_WIDTH = 224
        private const val INPUT_IMAGE_HEIGHT = 224
        private const val NUM_CLASSES = 5

        private val OPTIMAL_THRESHOLDS = mapOf(
            "Jerawat_Aktif" to 0.45f,
            "Kulit_Berminyak" to 0.30f,
            "Kemerahan" to 0.40f,
            "Tekstur_Pori_pori" to 0.35f,
            "Kulit_Sehat" to 0.50f
        )
    }

    init {
        val options = Interpreter.Options().apply {
            setNumThreads(4)

            // FlexDelegate: optional, but recommended for SelectOps model
            try {
                addDelegate(FlexDelegate())
            } catch (e: Exception) {
                Log.w("SkinAnalyzer", "FlexDelegate unavailable: ${e.message}")
            }
        }

        interpreter = try {
            Interpreter(loadModelFile(modelName), options)
        } catch (e: Exception) {
            Log.e("SkinAnalyzer", "Failed to load model: ${e.message}")
            throw IllegalStateException("Cannot initialize TFLite Interpreter", e)
        }

        imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(INPUT_IMAGE_HEIGHT, INPUT_IMAGE_WIDTH, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(127.5f, 127.5f))
            .build()
    }

    private fun loadModelFile(modelName: String): MappedByteBuffer {
        val fd = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fd.fileDescriptor)
        val channel = inputStream.channel
        return channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
    }

    private fun preprocess(bitmap: Bitmap): TensorImage {
        val tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        return imageProcessor.process(tensorImage)
    }

    private fun postprocess(outputProbabilities: FloatArray): SkinResult {
        val probabilities = mutableMapOf<String, Float>()
        val diagnosis = mutableMapOf<String, Boolean>()

        Log.d("SkinAnalyzer", "=== MEMULAI ANALISIS BARU ===")

        ALL_CLASSES.forEachIndexed { i, className ->
            val score = outputProbabilities[i]
            val threshold = OPTIMAL_THRESHOLDS[className] ?: 0.5f
            probabilities[className] = score
            diagnosis[className] = score > threshold

            Log.d("SkinAnalyzer", "Hasil $className: $score (Threshold: $threshold)")
        }

        return SkinResult(probabilities, diagnosis)
    }

    fun analyze(bitmap: Bitmap): SkinResult {
        if (bitmap.width <= 0 || bitmap.height <= 0) {
            throw IllegalArgumentException("Bitmap invalid: ukuran tidak boleh 0.")
        }

        return try {
            val inputImage = preprocess(bitmap)
            val output = Array(1) { FloatArray(NUM_CLASSES) }
            interpreter.run(inputImage.buffer, output)
            postprocess(output[0])
        } catch (e: Exception) {
            Log.e("SkinAnalyzer", "Inference failed: ${e.message}")
            throw RuntimeException("Gagal menjalankan analisis kulit.", e)
        }
    }

    fun close() = interpreter.close()
}
