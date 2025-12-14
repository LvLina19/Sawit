package com.example.sawit.IsiDashboard

import ai.onnxruntime.OnnxTensor
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.sawit.R
import com.example.sawit.ml.OnnxModelHelper
import com.example.sawit.ml.PredictionResult
import kotlinx.coroutines.launch
import java.io.InputStream
import java.nio.FloatBuffer

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
            Toast.makeText(requireContext(), "Model berhasil dimuat", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
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

            // Tampilkan preview
            ivPreview.setImageBitmap(selectedImageBitmap)

            // Enable button cek kematangan
            btnCekKematangan.isEnabled = true
            btnCekKematangan.alpha = 1.0f

            // Hide result if showing
            resultContainer.visibility = View.GONE

            Toast.makeText(requireContext(), "Gambar berhasil dipilih", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal memuat gambar: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun analyzeImage(bitmap: Bitmap) {
        // Tampilkan loading
        btnCekKematangan.isEnabled = false
        btnCekKematangan.text = "Memproses..."

        lifecycleScope.launch {
            try {
                val result = onnxHelper?.predictMaturity(bitmap)

                // DEBUG: Log hasil prediksi
                android.util.Log.d("DeteksiFragment", "Label: ${result?.label}")
                android.util.Log.d("DeteksiFragment", "Confidence RAW: ${result?.confidence}")
                android.util.Log.d("DeteksiFragment", "Confidence x100: ${result?.confidence?.times(100)}")
                android.util.Log.d("DeteksiFragment", "Error: ${result?.error}")

                // Kembalikan state button
                btnCekKematangan.isEnabled = true
                btnCekKematangan.text = "Cek Kematangan"

                if (result != null && result.error == null) {
                    showResult(result)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${result?.error ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                android.util.Log.e("DeteksiFragment", "Exception: ${e.message}", e)
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

    // Tambahkan fungsi softmax
    private fun softmax(logits: FloatArray): FloatArray {
        val maxLogit = logits.maxOrNull() ?: 0f
        val exps = logits.map { kotlin.math.exp((it - maxLogit).toDouble()).toFloat() }
        val sumExps = exps.sum()
        return exps.map { it / sumExps }.toFloatArray()
    }
    private fun showResult(result: com.example.sawit.ml.PredictionResult) {
        // Update result label
        tvResultLabel.text = result.label

        // Update confidence - TIDAK PERLU DIKALI 100 LAGI!
        val confidenceInt = result.confidence.toInt() // Langsung toInt() saja
        tvConfidence.text = "$confidenceInt%"
        progressConfidence.progress = confidenceInt

        // Show result container with animation
        resultContainer.visibility = View.VISIBLE
        resultContainer.alpha = 0f
        resultContainer.animate()
            .alpha(1f)
            .setDuration(300)
            .start()
    }    private fun resetDetection() {
        resultContainer.visibility = View.GONE
        btnCekKematangan.isEnabled = false
        btnCekKematangan.alpha = 0.5f
        selectedImageBitmap?.recycle()
        selectedImageBitmap = null
        ivPreview.setImageResource(R.drawable.ic_launcher_background)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        onnxHelper?.close()
        selectedImageBitmap?.recycle()
        selectedImageBitmap = null
    }
}