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
import com.example.sawit.model.RiwayatDeteksiRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
    private lateinit var repository: RiwayatDeteksiRepository

    // Menyimpan hasil deteksi terakhir
    private var lastPredictionResult: PredictionResult? = null

    companion object {
        private const val TAG = "DeteksiFragment"
    }

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
        initializeRepository()

        return view
    }

    private fun initViews(view: View) {
        ivPreview = view.findViewById(R.id.ivPreview)
        btnPilihFoto = view.findViewById(R.id.btnPilihFoto)
        btnCekKematangan = view.findViewById(R.id.btnCekKematangan)
        btnBack = view.findViewById(R.id.btnBack)

        resultContainer = view.findViewById(R.id.resultContainer)
        tvResultLabel = view.findViewById(R.id.tvResultLabel)
        tvConfidence = view.findViewById(R.id.tvConfidence)
        progressConfidence = view.findViewById(R.id.progressConfidence)
        tvLocation = view.findViewById(R.id.tvLocation)
        btnScanLagi = view.findViewById(R.id.btnScanLagi)
        btnRiwayatDeteksi = view.findViewById(R.id.btnRiwayatDeteksi)

        btnCekKematangan.isEnabled = false
        btnCekKematangan.alpha = 0.5f

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
            navigateToRiwayat()
        }
    }

    private fun initializeModel() {
        try {
            onnxHelper = OnnxModelHelper(requireContext())
            Log.d(TAG, "Model berhasil dimuat")
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memuat model", e)
            Toast.makeText(requireContext(), "Gagal memuat model: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun initializeRepository() {
        repository = RiwayatDeteksiRepository(requireContext())
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
                ivPreview.setImageBitmap(selectedImageBitmap)
                btnCekKematangan.isEnabled = true
                btnCekKematangan.alpha = 1.0f
                resultContainer.visibility = View.GONE

                Log.d(TAG, "Gambar berhasil dipilih: ${selectedImageBitmap?.width}x${selectedImageBitmap?.height}")
            } else {
                throw Exception("Bitmap null setelah decode")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gagal memuat gambar", e)
            Toast.makeText(requireContext(), "Gagal memuat gambar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun analyzeImage(bitmap: Bitmap) {
        btnCekKematangan.isEnabled = false
        btnCekKematangan.text = "Memproses..."

        Log.d(TAG, "=== Mulai Analisis ===")

        lifecycleScope.launch {
            try {
                val result = onnxHelper?.predictMaturity(bitmap)

                Log.d(TAG, "=== Hasil Prediksi ===")
                Log.d(TAG, "Label: ${result?.label}")
                Log.d(TAG, "Confidence: ${result?.confidence}%")

                btnCekKematangan.isEnabled = true
                btnCekKematangan.text = "Cek Kematangan"

                if (result != null && result.error == null) {
                    lastPredictionResult = result
                    showResult(result)

                    // Simpan ke Firebase
                    saveToFirebase(bitmap, result)
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

    private fun saveToFirebase(bitmap: Bitmap, result: PredictionResult) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Menyimpan hasil deteksi...")

                // Show loading
                Toast.makeText(requireContext(), "Mengupload gambar...", Toast.LENGTH_SHORT).show()

                val saveResult = repository.saveDeteksi(
                    bitmap = bitmap,
                    jenisBuah = result.label,
                    lokasi = tvLocation.text.toString(),
                    kepercayaan = result.confidence.toInt(),
                    area = 0.0
                )

                saveResult.fold(
                    onSuccess = { documentId ->
                        Log.d(TAG, "Berhasil disimpan dengan ID: $documentId")
                        Toast.makeText(
                            requireContext(),
                            "✅ Hasil deteksi berhasil disimpan",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Gagal menyimpan", error)

                        val errorMsg = when {
                            error.message?.contains("upload gambar", true) == true ->
                                "Gagal upload gambar ke server"
                            error.message?.contains("network", true) == true ->
                                "Tidak ada koneksi internet"
                            error.message?.contains("timeout", true) == true ->
                                "Upload timeout, coba lagi"
                            else -> error.message ?: "Error tidak diketahui"
                        }

                        Toast.makeText(
                            requireContext(),
                            "❌ Gagal: $errorMsg",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception saat menyimpan", e)
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    private fun showResult(result: PredictionResult) {
        Log.d(TAG, "=== Menampilkan Hasil ===")

        tvResultLabel.text = result.label

        val labelColor = when (result.label) {
            "Mentah" -> android.graphics.Color.parseColor("#E53935")
            "Matang" -> android.graphics.Color.parseColor("#43A047")
            "Kelewat Matang" -> android.graphics.Color.parseColor("#FB8C00")
            else -> android.graphics.Color.parseColor("#757575")
        }
        tvResultLabel.setTextColor(labelColor)

        val confidenceInt = result.confidence.toInt().coerceIn(0, 100)
        tvConfidence.text = "$confidenceInt%"
        progressConfidence.progress = confidenceInt

        resultContainer.visibility = View.VISIBLE
        resultContainer.alpha = 0f
        resultContainer.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }

    private fun resetDetection() {
        Log.d(TAG, "Reset detection")
        resultContainer.visibility = View.GONE
        btnCekKematangan.isEnabled = false
        btnCekKematangan.alpha = 0.5f
        btnCekKematangan.text = "Cek Kematangan"

        selectedImageBitmap?.recycle()
        selectedImageBitmap = null
        lastPredictionResult = null

        ivPreview.setImageResource(R.drawable.ic_launcher_background)
    }

    private fun navigateToRiwayat() {
        val fragment = RiwayatDeteksiFragment()

        // Cara 1: Dapatkan container ID dari parent view fragment saat ini
        val containerId = (view?.parent as? View)?.id

        if (containerId != null && containerId != View.NO_ID) {
            parentFragmentManager.beginTransaction()
                .replace(containerId, fragment)
                .addToBackStack(null)
                .commit()
        } else {
            // Fallback: Coba cari activity dan ganti content
            activity?.supportFragmentManager?.beginTransaction()
                ?.replace(android.R.id.content, fragment)
                ?.addToBackStack(null)
                ?.commit()
        }
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