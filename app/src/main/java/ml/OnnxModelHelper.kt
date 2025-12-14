package com.example.sawit.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import ai.onnxruntime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.nio.FloatBuffer

class OnnxModelHelper(private val context: Context) {

    private var scalerSession: OrtSession? = null
    private var pcaSession: OrtSession? = null
    private var modelSession: OrtSession? = null
    private var ortEnvironment: OrtEnvironment? = null

    // Labels kematangan
    private val labels = arrayOf("Mentah", "Matang", "Terlalu Matang")

    init {
        try {
            ortEnvironment = OrtEnvironment.getEnvironment()
            loadModels()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModels() {
        try {
            val sessionOptions = OrtSession.SessionOptions()

            // Load Scaler
            val scalerBytes = readModelFromAssets("scaler.onnx")
            scalerSession = ortEnvironment?.createSession(scalerBytes, sessionOptions)

            // Load PCA
            val pcaBytes = readModelFromAssets("pca.onnx")
            pcaSession = ortEnvironment?.createSession(pcaBytes, sessionOptions)

            // Load Main Model
            val modelBytes = readModelFromAssets("model.onnx")
            modelSession = ortEnvironment?.createSession(modelBytes, sessionOptions)

        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Gagal memuat model: ${e.message}")
        }
    }

    private fun readModelFromAssets(fileName: String): ByteArray {
        val inputStream: InputStream = context.assets.open(fileName)
        return inputStream.readBytes()
    }

    suspend fun predictMaturity(bitmap: Bitmap): PredictionResult = withContext(Dispatchers.Default) {
        try {
            // 1. Extract features dari gambar
            val features = extractFeaturesFromBitmap(bitmap)

            // 2. Apply Scaler
            val scaledFeatures = applyScaler(features)

            // 3. Apply PCA
            val pcaFeatures = applyPCA(scaledFeatures)

            // 4. Predict dengan model utama
            val prediction = predictWithModel(pcaFeatures)

            prediction
        } catch (e: Exception) {
            e.printStackTrace()
            PredictionResult(
                label = "Error",
                confidence = 0f,
                error = e.message ?: "Unknown error"
            )
        }
    }

    private fun extractFeaturesFromBitmap(bitmap: Bitmap): FloatArray {
        // Resize bitmap ke ukuran yang dibutuhkan (misal 224x224)
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

        val features = FloatArray(224 * 224 * 3) // RGB channels
        var index = 0

        for (y in 0 until resizedBitmap.height) {
            for (x in 0 until resizedBitmap.width) {
                val pixel = resizedBitmap.getPixel(x, y)

                // Normalize RGB values to [0, 1]
                features[index++] = Color.red(pixel) / 255f
                features[index++] = Color.green(pixel) / 255f
                features[index++] = Color.blue(pixel) / 255f
            }
        }

        return features
    }

    private fun applyScaler(features: FloatArray): FloatArray {
        val inputName = scalerSession?.inputNames?.iterator()?.next()
        val shape = longArrayOf(1, features.size.toLong())

        val inputTensor = OnnxTensor.createTensor(
            ortEnvironment,
            FloatBuffer.wrap(features),
            shape
        )

        val results = scalerSession?.run(mapOf(inputName to inputTensor))
        val output = results?.get(0)?.value

        val outputArray = when (output) {
            is Array<*> -> {
                // Handle 2D array output
                (output[0] as? FloatArray) ?: FloatArray(0)
            }
            is FloatArray -> output
            else -> FloatArray(0)
        }

        inputTensor.close()
        results?.close()

        return outputArray
    }

    private fun applyPCA(features: FloatArray): FloatArray {
        val inputName = pcaSession?.inputNames?.iterator()?.next()
        val shape = longArrayOf(1, features.size.toLong())

        val inputTensor = OnnxTensor.createTensor(
            ortEnvironment,
            FloatBuffer.wrap(features),
            shape
        )

        val results = pcaSession?.run(mapOf(inputName to inputTensor))
        val output = results?.get(0)?.value

        val outputArray = when (output) {
            is Array<*> -> {
                // Handle 2D array output
                (output[0] as? FloatArray) ?: FloatArray(0)
            }
            is FloatArray -> output
            else -> FloatArray(0)
        }

        inputTensor.close()
        results?.close()

        return outputArray
    }

    private fun predictWithModel(features: FloatArray): PredictionResult {
        val inputName = modelSession?.inputNames?.iterator()?.next()
        val shape = longArrayOf(1, features.size.toLong())

        val inputTensor = OnnxTensor.createTensor(
            ortEnvironment,
            FloatBuffer.wrap(features),
            shape
        )

        val results = modelSession?.run(mapOf(inputName to inputTensor))
        val output = results?.get(0)?.value

        val probabilities = when (output) {
            is Array<*> -> {
                // Handle 2D array output
                (output[0] as? FloatArray) ?: FloatArray(labels.size)
            }
            is FloatArray -> output
            else -> FloatArray(labels.size)
        }

        // Find max probability
        var maxIndex = 0
        var maxProb = probabilities[0]

        for (i in probabilities.indices) {
            if (probabilities[i] > maxProb) {
                maxProb = probabilities[i]
                maxIndex = i
            }
        }

        inputTensor.close()
        results?.close()

        return PredictionResult(
            label = labels[maxIndex],
            confidence = maxProb * 100,
            allProbabilities = probabilities.map { it * 100 }
        )
    }


    // TAMBAHKAN METHOD BARU INI 👇
    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f
        val exps = logits.map { kotlin.math.exp((it - maxLogit).toDouble()).toFloat() }
        val sumExps = exps.sum()
        return exps.map { it / sumExps }.toFloatArray()
    }
    fun close() {
        scalerSession?.close()
        pcaSession?.close()
        modelSession?.close()
        ortEnvironment?.close()
    }
}

data class PredictionResult(
    val label: String,
    val confidence: Float,
    val allProbabilities: List<Float> = emptyList(),
    val error: String? = null
)