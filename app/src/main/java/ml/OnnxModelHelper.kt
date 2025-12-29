package com.example.sawit.ml

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import ai.onnxruntime.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.io.InputStream
import java.nio.FloatBuffer
import kotlin.math.sqrt

class OnnxModelHelper(private val context: Context) {

    private var modelSession: OrtSession? = null
    private var ortEnvironment: OrtEnvironment? = null

    // Scaler parameters (dari scaler_sawit.pkl)
    // TODO: Ganti dengan nilai AKTUAL dari scaler Anda!
    private val scalerMean = floatArrayOf(
        38.7709f,
        59.3592f,
        130.0042f,
        1103.9981f,
        0.1342f,
        0.1950f
    )

    private val scalerStd = floatArrayOf(
        21.5241f,
        23.0539f,
        35.8990f,
        387.4233f,
        0.1815f,
        0.1739f
    )

    // Labels kematangan (3 kategori)
    private val labels = arrayOf(
        "Mentah",           // 0
        "Matang",           // 1
        "Kelewat Matang"    // 2
    )

    init {
        try {
            ortEnvironment = OrtEnvironment.getEnvironment()
            loadModel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadModel() {
        try {
            val sessionOptions = OrtSession.SessionOptions()

            // Load ONNX Model (hanya 1 file)
            val modelBytes = readModelFromAssets("model_sawit_rf.onnx")
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
            // 1. Extract features dari gambar (SAMA dengan Python)
            val features = extractFeatures(bitmap)

            // 2. Apply Scaler (manual, tanpa ONNX scaler)
            val scaledFeatures = applyManualScaler(features)

            // 3. Predict dengan ONNX model
            val prediction = predictWithModel(scaledFeatures)

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

    /**
     * Ekstraksi fitur IDENTIK dengan Python
     * Mengembalikan: [h_mean, s_mean, v_mean, contrast, energy, homogeneity]
     */
    private fun extractFeatures(bitmap: Bitmap): FloatArray {
        // Resize ke 200x200 (SAMA dengan Python)
        val resized = Bitmap.createScaledBitmap(bitmap, 200, 200, true)

        // Convert Bitmap ke OpenCV Mat
        val mat = Mat()
        Utils.bitmapToMat(resized, mat)

        // Convert RGBA ke BGR (OpenCV default)
        val bgr = Mat()
        Imgproc.cvtColor(mat, bgr, Imgproc.COLOR_RGBA2BGR)

        // ---- 1. WARNA (HSV) ----
        val hsv = Mat()
        Imgproc.cvtColor(bgr, hsv, Imgproc.COLOR_BGR2HSV)

        val hsvChannels = mutableListOf<Mat>()
        Core.split(hsv, hsvChannels)

        val hMean = Core.mean(hsvChannels[0]).`val`[0].toFloat()
        val sMean = Core.mean(hsvChannels[1]).`val`[0].toFloat()
        val vMean = Core.mean(hsvChannels[2]).`val`[0].toFloat()

        // ---- 2. TEKSTUR (GLCM) ----
        val gray = Mat()
        Imgproc.cvtColor(bgr, gray, Imgproc.COLOR_BGR2GRAY)

        // Hitung GLCM features
        val glcmFeatures = calculateGLCM(gray)
        val contrast = glcmFeatures[0]
        val energy = glcmFeatures[1]
        val homogeneity = glcmFeatures[2]

        // Cleanup
        mat.release()
        bgr.release()
        hsv.release()
        gray.release()
        hsvChannels.forEach { it.release() }

        return floatArrayOf(hMean, sMean, vMean, contrast, energy, homogeneity)
    }

    /**
     * Hitung GLCM features (simplified version)
     * Untuk hasil PERSIS sama, gunakan library seperti scikit-image
     */
    private fun calculateGLCM(grayMat: Mat): FloatArray {
        val width = grayMat.cols()
        val height = grayMat.rows()

        // GLCM matrix (256x256 untuk grayscale)
        val glcm = Array(256) { FloatArray(256) }

        // Hitung co-occurrence (distance=1, angle=0°)
        for (y in 0 until height) {
            for (x in 0 until width - 1) {
                val pixel1 = grayMat.get(y, x)[0].toInt()
                val pixel2 = grayMat.get(y, x + 1)[0].toInt()
                glcm[pixel1][pixel2]++
            }
        }

        // Normalize GLCM
        var total = 0f
        for (i in 0 until 256) {
            for (j in 0 until 256) {
                total += glcm[i][j]
            }
        }

        if (total > 0) {
            for (i in 0 until 256) {
                for (j in 0 until 256) {
                    glcm[i][j] /= total
                }
            }
        }

        // Calculate properties
        var contrast = 0f
        var energy = 0f
        var homogeneity = 0f

        for (i in 0 until 256) {
            for (j in 0 until 256) {
                val value = glcm[i][j]
                contrast += (i - j) * (i - j) * value
                energy += value * value
                homogeneity += value / (1 + kotlin.math.abs(i - j))
            }
        }

        return floatArrayOf(contrast, energy, homogeneity)
    }

    /**
     * Manual scaling: (x - mean) / std
     * Sama dengan StandardScaler.transform() di Python
     */
    private fun applyManualScaler(features: FloatArray): FloatArray {
        val scaled = FloatArray(features.size)
        for (i in features.indices) {
            scaled[i] = (features[i] - scalerMean[i]) / scalerStd[i]
        }
        return scaled
    }

    /**
     * Prediksi menggunakan ONNX model
     */
    private fun predictWithModel(features: FloatArray): PredictionResult {
        val inputName = modelSession?.inputNames?.iterator()?.next()
        val shape = longArrayOf(1, features.size.toLong())

        val inputTensor = OnnxTensor.createTensor(
            ortEnvironment,
            FloatBuffer.wrap(features),
            shape
        )

        val results = modelSession?.run(mapOf(inputName to inputTensor))

        // RandomForest ONNX output biasanya langsung class index
        val output = results?.get(0)?.value

        val predictedClass = when (output) {
            is LongArray -> output[0].toInt()
            is Array<*> -> (output[0] as? LongArray)?.get(0)?.toInt() ?: 0
            else -> 0
        }

        inputTensor.close()
        results?.close()

        // Confidence bisa diambil dari output kedua jika ada (probabilities)
        // Untuk sementara gunakan confidence dummy
        val confidence = 85f // Atau ambil dari output[1] jika tersedia

        return PredictionResult(
            label = labels.getOrElse(predictedClass) { "Unknown" },
            confidence = confidence,
            predictedClass = predictedClass
        )
    }

    fun close() {
        modelSession?.close()
        ortEnvironment?.close()
    }
}

data class PredictionResult(
    val label: String,
    val confidence: Float,
    val predictedClass: Int = -1,
    val error: String? = null
)