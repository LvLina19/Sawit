package com.example.sawit.IsiDashboard

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.sawit.R
import com.example.sawit.ml.OnnxModelHelper
import com.example.sawit.ml.PredictionResult
import kotlinx.coroutines.launch
import java.io.InputStream

class DeteksiFragment : Fragment() {

    private lateinit var ivPreview: ImageView
    private lateinit var btnPilihFoto: Button
    private lateinit var btnCekKematangan: Button
    private lateinit var btnBack: ImageView

    // Result views
    private lateinit var resultContainer: LinearLayout
    private lateinit var tvResultLabel: TextView
    private lateinit var tvConfidence: TextView
    private lateinit var progressConfidence: ProgressBar
    private lateinit var tvLocation: TextView
    private lateinit var btnScanLagi: Button
    private lateinit var btnRiwayatDeteksi: Button

    private var selectedImageBitmap: Bitmap? = null
    private var onnxHelper: OnnxModelHelper? = null

    companion object {
        private const val TAG = "DeteksiFragment"
    }

    // Activity Result Launcher untuk memilih gambar
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImageSelection(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_deteksi_sawit, container, false)

        initViews(view)
        setupListeners()
        initializeModel()

        return view
    }

    private fun initViews(view: View) {
        // Input views
        ivPreview = view.findViewById(R.id.ivPreview)
        btnPilihFoto = view.findViewById(R.id.btnPilihFoto)
        btnCekKematangan = view.findViewById(R.id.btnCekKematangan)
        btnBack = view.findViewById(R.id.btnBack)

        // Result views
        resultContainer = view.findViewById(R.id.resultContainer)
        tvResultLabel = view.findViewById(R.id.tvResultLabel)
        tvConfidence = view.findViewById(R.id.tvConfidence)
        progressConfidence = view.findViewById(R.id.progressConfidence)
        tvLocation = view.findViewById(R.id.tvLocation)
        btnScanLagi = view.findViewById(R.id.btnScanLagi)
        btnRiwayatDeteksi = view.findViewById(R.id.btnRiwayatDeteksi)

        // Disable button cek kematangan awalnya
        btnCekKematangan.isEnabled = false
        btnCekKematangan.alpha = 0.5f

        // Set default location
        tvLocation.text = "Riau, Indonesia"
    }

    private fun setupListeners() {
        btnPilihFoto.setOnClickListener {
            openGallery()
        }

        btnCekKematangan.setOnClickListener {
            selectedImageBitmap?.let { bitmap ->
                analyzeImage(bitmap)
            } ?: run {
                Toast.makeText(requireContext(), "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnScanLagi.setOnClickListener {
            resetDetection()
            openGallery()
        }

        btnRiwayatDeteksi.setOnClickListener {
            // TODO: Navigate to history
            Toast.makeText(requireContext(), "Fitur riwayat akan segera hadir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initializeModel() {
        try {
            onnxHelper = OnnxModelHelper(requireContext())
            Log.d(TAG, "Model berhasil dimuat")
            Toast.makeText(requireContext(), "Model siap digunakan", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memuat model", e)
            Toast.makeText(requireContext(), "Gagal memuat model: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    private fun handleImageSelection(uri: Uri) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            selectedImageBitmap = BitmapFactory.decodeStream(inputStream)

            if (selectedImageBitmap != null) {
                // Tampilkan preview
                ivPreview.setImageBitmap(selectedImageBitmap)

                // Enable button cek kematangan
                btnCekKematangan.isEnabled = true
                btnCekKematangan.alpha = 1.0f

                // Hide result if showing
                resultContainer.visibility = View.GONE

                Log.d(TAG, "Gambar berhasil dipilih: ${selectedImageBitmap?.width}x${selectedImageBitmap?.height}")
                Toast.makeText(requireContext(), "Gambar berhasil dipilih", Toast.LENGTH_SHORT).show()
            } else {
                throw Exception("Bitmap null setelah decode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memuat gambar", e)
            Toast.makeText(requireContext(), "Gagal memuat gambar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun analyzeImage(bitmap: Bitmap) {
        // Tampilkan loading
        btnCekKematangan.isEnabled = false
        btnCekKematangan.text = "Memproses..."

        Log.d(TAG, "=== Mulai Analisis ===")
        Log.d(TAG, "Ukuran bitmap: ${bitmap.width}x${bitmap.height}")

        lifecycleScope.launch {
            try {
                // Prediksi
                val result = onnxHelper?.predictMaturity(bitmap)

                // DEBUG: Log hasil prediksi
                Log.d(TAG, "=== Hasil Prediksi ===")
                Log.d(TAG, "Label: ${result?.label}")
                Log.d(TAG, "Predicted Class: ${result?.predictedClass}")
                Log.d(TAG, "Confidence: ${result?.confidence}%")
                Log.d(TAG, "Error: ${result?.error}")

                // Kembalikan state button
                btnCekKematangan.isEnabled = true
                btnCekKematangan.text = "Cek Kematangan"

                if (result != null && result.error == null) {
                    showResult(result)
                } else {
                    val errorMsg = result?.error ?: "Unknown error"
                    Log.e(TAG, "Error prediksi: $errorMsg")
                    Toast.makeText(
                        requireContext(),
                        "Error: $errorMsg",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception saat analisis", e)
                btnCekKematangan.isEnabled = true
                btnCekKematangan.text = "Cek Kematangan"
                Toast.makeText(
                    requireContext(),
                    "Gagal menganalisis: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showResult(result: PredictionResult) {
        Log.d(TAG, "=== Menampilkan Hasil ===")
        Log.d(TAG, "Label: ${result.label}")
        Log.d(TAG, "Confidence: ${result.confidence}%")

        // Update result label dengan styling
        tvResultLabel.text = result.label

        // Set warna berdasarkan label (DISESUAIKAN untuk 3 label)
        val labelColor = when (result.label) {
            "Mentah" -> android.graphics.Color.parseColor("#E53935") // Merah
            "Matang" -> android.graphics.Color.parseColor("#43A047") // Hijau
            "Kelewat Matang" -> android.graphics.Color.parseColor("#FB8C00") // Orange
            else -> android.graphics.Color.parseColor("#757575") // Abu-abu
        }
        tvResultLabel.setTextColor(labelColor)

        // Update confidence (sudah dalam bentuk persen dari model)
        val confidenceInt = result.confidence.toInt().coerceIn(0, 100)
        tvConfidence.text = "$confidenceInt%"
        progressConfidence.progress = confidenceInt

        Log.d(TAG, "Progress bar set to: $confidenceInt")

        // Show result container with animation
        resultContainer.visibility = View.VISIBLE
        resultContainer.alpha = 0f
        resultContainer.animate()
            .alpha(1f)
            .setDuration(300)
            .start()

        Log.d(TAG, "Result container ditampilkan")
    }

    private fun resetDetection() {
        Log.d(TAG, "Reset detection")
        resultContainer.visibility = View.GONE
        btnCekKematangan.isEnabled = false
        btnCekKematangan.alpha = 0.5f
        btnCekKematangan.text = "Cek Kematangan"

        selectedImageBitmap?.recycle()
        selectedImageBitmap = null

        ivPreview.setImageResource(R.drawable.ic_launcher_background)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")

        onnxHelper?.close()
        onnxHelper = null

        selectedImageBitmap?.recycle()
        selectedImageBitmap = null
    }
}