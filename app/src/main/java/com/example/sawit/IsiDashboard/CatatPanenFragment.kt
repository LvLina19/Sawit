package com.example.sawit.IsiDashboard

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sawit.R
import com.example.sawit.model.KebunData
import com.example.sawit.model.PanenData
import com.example.sawit.utils.KebunManager
import com.example.sawit.utils.PanenManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CatatPanenFragment : Fragment() {

    private lateinit var btnBack: ImageView
    private lateinit var layoutTanggalPanen: RelativeLayout
    private lateinit var etTanggalPanen: EditText
    private lateinit var etLokasi: EditText
    private lateinit var etTBSMatang: EditText
    private lateinit var etTBSTidakMatang: EditText
    private lateinit var etTBSKelewatMatang: EditText
    private lateinit var btnSubmit: LinearLayout
    private lateinit var progressBar: ProgressBar

    private lateinit var panenManager: PanenManager
    private lateinit var kebunManager: KebunManager
    private var kebunId: Int = 0
    private var kebunData: KebunData? = null
    private val calendar = Calendar.getInstance()

    companion object {
        private const val TAG = "CatatPanenFragment"
        private const val ARG_KEBUN_ID = "kebun_id"
        private const val ARG_KEBUN_DATA = "kebun_data"

        fun newInstance(kebunId: Int): CatatPanenFragment {
            Log.d(TAG, ">>> newInstance called with kebunId: $kebunId")

            if (kebunId == 0) {
                Log.e(TAG, "!!! WARNING: Creating fragment with kebunId = 0")
            }

            val fragment = CatatPanenFragment()
            val args = Bundle()
            args.putInt(ARG_KEBUN_ID, kebunId)
            fragment.arguments = args
            return fragment
        }

        fun newInstance(kebunId: Int, kebunData: KebunData): CatatPanenFragment {
            Log.d(TAG, ">>> newInstance called with kebunId: $kebunId, namaKebun: ${kebunData.namaKebun}")

            if (kebunId == 0) {
                Log.e(TAG, "!!! WARNING: Creating fragment with kebunId = 0")
            }

            val fragment = CatatPanenFragment()
            val args = Bundle()
            args.putInt(ARG_KEBUN_ID, kebunId)
            args.putParcelable(ARG_KEBUN_DATA, kebunData)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize managers
        panenManager = PanenManager.getInstance(requireContext())
        kebunManager = KebunManager.getInstance(requireContext())

        // Get kebunId from arguments
        kebunId = arguments?.getInt(ARG_KEBUN_ID, 0) ?: 0
        kebunData = arguments?.getParcelable(ARG_KEBUN_DATA)

        Log.d(TAG, "onCreate - Kebun ID: $kebunId")
        Log.d(TAG, "onCreate - KebunData: ${kebunData?.namaKebun}")

        // Validation
        if (kebunId == 0) {
            Log.e(TAG, "CRITICAL ERROR: Kebun ID is 0!")
            Toast.makeText(requireContext(), "Error: Data kebun tidak valid", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_catat_panen, container, false)

        // Validasi kebunId saat view dibuat
        if (kebunId == 0) {
            Log.e(TAG, "ERROR: Invalid kebun ID (0) in onCreateView")
            Toast.makeText(requireContext(), "Error: Data kebun tidak valid", Toast.LENGTH_SHORT).show()
            activity?.supportFragmentManager?.popBackStack()
            return rootView
        }

        initViews(rootView)
        setupListeners()
        loadKebunData()

        return rootView
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        layoutTanggalPanen = view.findViewById(R.id.layoutTanggalPanen)
        etTanggalPanen = view.findViewById(R.id.etTanggalPanen)
        etLokasi = view.findViewById(R.id.etLokasi)
        etTBSMatang = view.findViewById(R.id.etTBSMatang)
        etTBSTidakMatang = view.findViewById(R.id.etTBSTidakMatang)
        etTBSKelewatMatang = view.findViewById(R.id.etTBSKelewatMatang)
        btnSubmit = view.findViewById(R.id.btnSubmit)
        progressBar = view.findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        layoutTanggalPanen.setOnClickListener {
            showDatePicker()
        }

        btnSubmit.setOnClickListener {
            submitData()
        }
    }

    private fun loadKebunData() {
        // Set tanggal hari ini terlebih dahulu
        updateDateDisplay()

        // Validasi kebunId
        if (kebunId == 0) {
            Log.e(TAG, "Cannot load kebun data: Invalid ID (0)")

            // Fallback ke KebunData dari arguments
            if (kebunData != null) {
                Log.d(TAG, "Using KebunData from arguments: ${kebunData?.namaKebun}")
                etLokasi.setText(kebunData?.lokasiKebun ?: "")
                return
            }

            Toast.makeText(requireContext(), "Error: Data kebun tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        // Jika sudah ada kebunData dari arguments, gunakan itu
        if (kebunData != null) {
            Log.d(TAG, "Using existing KebunData: ${kebunData?.namaKebun}")
            etLokasi.setText(kebunData?.lokasiKebun ?: "")
            return
        }

        // Load dari Firebase jika belum ada
        Log.d(TAG, "Loading kebun data from Firebase for ID: $kebunId")
        showLoading(true)

        kebunManager.getKebunById(kebunId) { loadedKebunData ->
            activity?.runOnUiThread {
                showLoading(false)

                if (loadedKebunData != null) {
                    kebunData = loadedKebunData
                    etLokasi.setText(loadedKebunData.lokasiKebun)
                    Log.d(TAG, "Kebun data loaded successfully: ${loadedKebunData.namaKebun}")
                } else {
                    Log.w(TAG, "Kebun data not found for ID: $kebunId")
                    Toast.makeText(requireContext(), "Data kebun tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                updateDateDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Batasi tanggal maksimal ke hari ini
        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        etTanggalPanen.setText(dateFormat.format(calendar.time))
    }

    private fun submitData() {
        Log.d(TAG, "=== SUBMIT DATA START ===")
        Log.d(TAG, "Kebun ID: $kebunId")

        // Validasi input
        if (!validateInput()) {
            Log.w(TAG, "Validation failed")
            return
        }

        // Validasi kebunId sekali lagi sebelum submit
        if (kebunId == 0) {
            Log.e(TAG, "SUBMIT ERROR: Kebun ID is 0!")
            Toast.makeText(requireContext(), "Error: Data kebun tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Ambil data dari input
        val tanggal = etTanggalPanen.text.toString().trim()
        val lokasi = etLokasi.text.toString().trim()
        val tbsMatang = etTBSMatang.text.toString().toDoubleOrNull() ?: 0.0
        val tbsTidakMatang = etTBSTidakMatang.text.toString().toDoubleOrNull() ?: 0.0
        val tbsKelewatMatang = etTBSKelewatMatang.text.toString().toDoubleOrNull() ?: 0.0

        // Hitung jumlah buah (asumsi 1 buah = 10 kg)
        val jumlahMatang = (tbsMatang / 10).toInt()
        val jumlahTidakMatang = (tbsTidakMatang / 10).toInt()
        val jumlahKelewatMatang = (tbsKelewatMatang / 10).toInt()

        // Buat objek PanenData
        val panenData = PanenData(
            id = 0, // Firebase akan generate
            kebunId = kebunId,
            tanggalPanen = tanggal,
            lokasi = lokasi,
            tbsMatang = tbsMatang,
            tbsTidakMatang = tbsTidakMatang,
            tbsKelewatMatang = tbsKelewatMatang,
            jumlahMatang = jumlahMatang,
            jumlahTidakMatang = jumlahTidakMatang,
            jumlahKelewatMatang = jumlahKelewatMatang,
            hargaPerKg = 2000.0,
            timestamp = System.currentTimeMillis()
        )

        Log.d(TAG, "Saving PanenData: $panenData")

        // Simpan ke Firebase
        panenManager.savePanen(panenData) { success, errorMessage ->
            activity?.runOnUiThread {
                showLoading(false)

                if (success) {
                    Log.d(TAG, "✓ Data panen berhasil disimpan")
                    showSuccessDialog()
                } else {
                    Log.e(TAG, "✗ Gagal menyimpan data: $errorMessage")
                    Toast.makeText(
                        requireContext(),
                        "Gagal menyimpan data: ${errorMessage ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        Log.d(TAG, "=== SUBMIT DATA END ===")
    }

    private fun validateInput(): Boolean {
        val tanggal = etTanggalPanen.text.toString().trim()
        val lokasi = etLokasi.text.toString().trim()
        val tbsMatangStr = etTBSMatang.text.toString().trim()
        val tbsTidakMatangStr = etTBSTidakMatang.text.toString().trim()
        val tbsKelewatMatangStr = etTBSKelewatMatang.text.toString().trim()

        when {
            tanggal.isEmpty() -> {
                Toast.makeText(requireContext(), "Pilih tanggal panen terlebih dahulu", Toast.LENGTH_SHORT).show()
                return false
            }
            lokasi.isEmpty() -> {
                etLokasi.error = "Lokasi harus diisi"
                etLokasi.requestFocus()
                return false
            }
            tbsMatangStr.isEmpty() && tbsTidakMatangStr.isEmpty() && tbsKelewatMatangStr.isEmpty() -> {
                Toast.makeText(requireContext(), "Masukkan minimal satu data berat TBS", Toast.LENGTH_SHORT).show()
                return false
            }
        }

        return true
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Berhasil")
            .setMessage("Data panen berhasil disimpan!")
            .setPositiveButton("Lihat Laporan") { dialog, _ ->
                dialog.dismiss()
                navigateToDetailLaporan()
            }
            .setNegativeButton("Tambah Lagi") { dialog, _ ->
                dialog.dismiss()
                clearForm()
            }
            .setCancelable(false)
            .show()
    }

    private fun clearForm() {
        etTBSMatang.setText("")
        etTBSTidakMatang.setText("")
        etTBSKelewatMatang.setText("")
        calendar.timeInMillis = System.currentTimeMillis()
        updateDateDisplay()
        etTBSMatang.requestFocus()
    }

    private fun navigateToDetailLaporan() {
        Log.d(TAG, "=== NAVIGATE TO DETAIL LAPORAN ===")

        if (kebunId == 0) {
            Log.e(TAG, "Cannot navigate: Invalid Kebun ID")
            Toast.makeText(requireContext(), "Error: Data kebun tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        showLoading(true)

        // Ambil kebun data (gunakan yang sudah ada atau load dari Firebase)
        if (kebunData != null) {
            loadPanenDataAndNavigate(kebunData!!)
        } else {
            kebunManager.getKebunById(kebunId) { loadedKebunData ->
                if (loadedKebunData == null) {
                    activity?.runOnUiThread {
                        showLoading(false)
                        Log.e(TAG, "KebunData not found for ID: $kebunId")
                        Toast.makeText(requireContext(), "Error: Data kebun tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                    return@getKebunById
                }

                loadPanenDataAndNavigate(loadedKebunData)
            }
        }
    }

    private fun loadPanenDataAndNavigate(kebun: KebunData) {
        // Ambil semua data panen untuk kebun ini
        panenManager.getPanenByKebunId(kebunId) { panenList ->
            activity?.runOnUiThread {
                showLoading(false)

                Log.d(TAG, "KebunData: ${kebun.namaKebun}")
                Log.d(TAG, "PanenList size: ${panenList.size}")

                if (panenList.isEmpty()) {
                    Toast.makeText(requireContext(), "Belum ada data panen", Toast.LENGTH_SHORT).show()
                    activity?.supportFragmentManager?.popBackStack()
                    return@runOnUiThread
                }

                try {
                    // Navigate ke DetailLaporanFragment
                    val detailLaporanFragment = DetailLaporanFragment.newInstance(
                        kebun,
                        ArrayList(panenList)
                    )

                    activity?.supportFragmentManager?.beginTransaction()?.apply {
                        replace(R.id.fragmentContainer, detailLaporanFragment)
                        addToBackStack("DetailLaporan")
                        commit()
                    }

                    Log.d(TAG, "✓ Navigation completed successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "✗ ERROR in fragment transaction", e)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSubmit.isEnabled = !show
        btnSubmit.isClickable = !show
        btnBack.isEnabled = !show
        etTBSMatang.isEnabled = !show
        etTBSTidakMatang.isEnabled = !show
        etTBSKelewatMatang.isEnabled = !show
        etLokasi.isEnabled = !show
        layoutTanggalPanen.isClickable = !show
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView called")
    }
}

//package com.example.sawit.IsiDashboard
//
//import android.app.DatePickerDialog
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.RelativeLayout
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import com.example.sawit.R
//import com.example.sawit.model.PanenData
//import com.example.sawit.utils.PanenManager
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Locale
//
//class CatatPanenFragment : Fragment() {
//
//    private lateinit var btnBack: ImageView
//    private lateinit var layoutTanggalPanen: RelativeLayout
//    private lateinit var etTanggalPanen: EditText
//    private lateinit var etLokasi: EditText
//    private lateinit var etTBSMatang: EditText
//    private lateinit var etTBSTidakMatang: EditText
//    private lateinit var etTBSKelewatMatang: EditText
//    private lateinit var btnSubmit: LinearLayout
//
//    private lateinit var panenManager: PanenManager
//    private var kebunId: Int = 0
//    private var kebunData: com.example.sawit.model.KebunData? = null
//    private val calendar = Calendar.getInstance()
//
//    companion object {
//        private const val ARG_KEBUN_ID = "kebun_id"
//        private const val ARG_KEBUN_DATA = "kebun_data"
//
//        fun newInstance(kebunId: Int, kebunData: com.example.sawit.model.KebunData): CatatPanenFragment {
//            val fragment = CatatPanenFragment()
//            val args = Bundle()
//            args.putInt(ARG_KEBUN_ID, kebunId)
//            args.putParcelable(ARG_KEBUN_DATA, kebunData)
//            fragment.arguments = args
//            return fragment
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//            kebunId = it.getInt(ARG_KEBUN_ID, 0)
//            kebunData = it.getParcelable(ARG_KEBUN_DATA)
//        }
//        panenManager = PanenManager.getInstance(requireContext())
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val rootView = inflater.inflate(R.layout.fragment_catat_panen, container, false)
//
//        initViews(rootView)
//        setupListeners()
//
//        return rootView
//    }
//
//    private fun initViews(view: View) {
//        btnBack = view.findViewById(R.id.btnBack)
//        layoutTanggalPanen = view.findViewById(R.id.layoutTanggalPanen)
//        etTanggalPanen = view.findViewById(R.id.etTanggalPanen)
//        etLokasi = view.findViewById(R.id.etLokasi)
//        etTBSMatang = view.findViewById(R.id.etTBSMatang)
//        etTBSTidakMatang = view.findViewById(R.id.etTBSTidakMatang)
//        etTBSKelewatMatang = view.findViewById(R.id.etTBSKelewatMatang)
//        btnSubmit = view.findViewById(R.id.btnSubmit)
//    }
//
//    private fun setupListeners() {
//        btnBack.setOnClickListener {
//            activity?.supportFragmentManager?.popBackStack()
//        }
//
//        layoutTanggalPanen.setOnClickListener {
//            showDatePicker()
//        }
//
//        btnSubmit.setOnClickListener {
//            submitData()
//        }
//    }
//
//    private fun showDatePicker() {
//        val datePickerDialog = DatePickerDialog(
//            requireContext(),
//            { _, year, month, dayOfMonth ->
//                calendar.set(year, month, dayOfMonth)
//                updateDateDisplay()
//            },
//            calendar.get(Calendar.YEAR),
//            calendar.get(Calendar.MONTH),
//            calendar.get(Calendar.DAY_OF_MONTH)
//        )
//        datePickerDialog.show()
//    }
//
//    private fun updateDateDisplay() {
//        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
//        etTanggalPanen.setText(dateFormat.format(calendar.time))
//    }
//
//    private fun submitData() {
//        android.util.Log.d("CatatPanen", "=== SUBMIT DATA START ===")
//
//        // Validasi input
//        val tanggal = etTanggalPanen.text.toString().trim()
//        val lokasi = etLokasi.text.toString().trim()
//        val tbsMatangStr = etTBSMatang.text.toString().trim()
//        val tbsTidakMatangStr = etTBSTidakMatang.text.toString().trim()
//        val tbsKelewatMatangStr = etTBSKelewatMatang.text.toString().trim()
//
//        android.util.Log.d("CatatPanen", "Tanggal: $tanggal")
//        android.util.Log.d("CatatPanen", "Lokasi: $lokasi")
//
//        if (tanggal.isEmpty()) {
//            showToast("Pilih tanggal panen")
//            return
//        }
//
//        if (lokasi.isEmpty()) {
//            showToast("Masukkan lokasi")
//            return
//        }
//
//        if (tbsMatangStr.isEmpty() && tbsTidakMatangStr.isEmpty() && tbsKelewatMatangStr.isEmpty()) {
//            showToast("Masukkan minimal satu data berat TBS")
//            return
//        }
//
//        // Konversi ke double (default 0 jika kosong)
//        val tbsMatang = tbsMatangStr.toDoubleOrNull() ?: 0.0
//        val tbsTidakMatang = tbsTidakMatangStr.toDoubleOrNull() ?: 0.0
//        val tbsKelewatMatang = tbsKelewatMatangStr.toDoubleOrNull() ?: 0.0
//
//        android.util.Log.d("CatatPanen", "TBS Matang: $tbsMatang kg")
//
//        // Hitung jumlah buah (estimasi: 1 buah = 10kg)
//        val jumlahMatang = (tbsMatang / 10).toInt()
//        val jumlahTidakMatang = (tbsTidakMatang / 10).toInt()
//        val jumlahKelewatMatang = (tbsKelewatMatang / 10).toInt()
//
//        // Buat object PanenData
//        val panenData = PanenData(
//            kebunId = kebunId,
//            tanggalPanen = tanggal,
//            lokasi = lokasi,
//            tbsMatang = tbsMatang,
//            tbsTidakMatang = tbsTidakMatang,
//            tbsKelewatMatang = tbsKelewatMatang,
//            jumlahMatang = jumlahMatang,
//            jumlahTidakMatang = jumlahTidakMatang,
//            jumlahKelewatMatang = jumlahKelewatMatang,
//            hargaPerKg = 2000.0,
//            timestamp = System.currentTimeMillis()
//        )
//
//        android.util.Log.d("CatatPanen", "PanenData created")
//
//        // Simpan data
//        val isSaved = panenManager.savePanen(panenData)
//
//        android.util.Log.d("CatatPanen", "Is Saved: $isSaved")
//
//        if (isSaved) {
//            showToast("Data panen berhasil disimpan")
//
//            // Validasi kebunData
//            android.util.Log.d("CatatPanen", "KebunData: ${kebunData?.namaKebun}")
//            android.util.Log.d("CatatPanen", "KebunData null?: ${kebunData == null}")
//
//            if (kebunData == null) {
//                android.util.Log.e("CatatPanen", "ERROR: KebunData is NULL!")
//                showToast("Error: Data kebun tidak tersedia")
//                activity?.supportFragmentManager?.popBackStack()
//                return
//            }
//
//            // Ambil semua data panen untuk kebun ini
//            val panenList = panenManager.getPanenByKebunId(kebunId)
//            android.util.Log.d("CatatPanen", "PanenList size: ${panenList.size}")
//
//            // Navigate ke DetailLaporanFragment
//            android.util.Log.d("CatatPanen", "Calling navigateToDetailLaporan...")
//            navigateToDetailLaporan(kebunData!!, panenList)
//        } else {
//            android.util.Log.e("CatatPanen", "ERROR: Failed to save!")
//            showToast("Gagal menyimpan data panen")
//        }
//
//        android.util.Log.d("CatatPanen", "=== SUBMIT DATA END ===")
//    }
//
//    private fun showToast(message: String) {
//        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//    }
//
//    private fun navigateToDetailLaporan(
//        kebunData: com.example.sawit.model.KebunData,
//        panenList: List<PanenData>
//    ) {
//        android.util.Log.d("CatatPanen", "=== NAVIGATE START ===")
//        android.util.Log.d("CatatPanen", "Activity: $activity")
//        android.util.Log.d("CatatPanen", "FragmentManager: ${activity?.supportFragmentManager}")
//
//        try {
//            // Buat DetailLaporanFragment dengan data
//            val detailLaporanFragment = DetailLaporanFragment.newInstance(kebunData, panenList)
//            android.util.Log.d("CatatPanen", "DetailLaporanFragment created")
//
//            activity?.supportFragmentManager?.beginTransaction()?.apply {
//                android.util.Log.d("CatatPanen", "Transaction started")
//                replace(R.id.fragmentContainer, detailLaporanFragment)
//                android.util.Log.d("CatatPanen", "Replace called")
//                addToBackStack(null)
//                android.util.Log.d("CatatPanen", "AddToBackStack called")
//                commit()
//                android.util.Log.d("CatatPanen", "Commit called")
//            }
//
//            android.util.Log.d("CatatPanen", "=== NAVIGATE END ===")
//        } catch (e: Exception) {
//            android.util.Log.e("CatatPanen", "ERROR in navigation: ${e.message}", e)
//            showToast("Error: ${e.message}")
//        }
//    }
//}