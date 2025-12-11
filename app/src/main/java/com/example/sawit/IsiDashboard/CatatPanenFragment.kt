package com.example.sawit.IsiDashboard

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.sawit.R
import com.example.sawit.model.PanenData
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

    private lateinit var panenManager: PanenManager
    private var kebunId: Int = 0
    private var kebunData: com.example.sawit.model.KebunData? = null
    private val calendar = Calendar.getInstance()

    companion object {
        private const val ARG_KEBUN_ID = "kebun_id"
        private const val ARG_KEBUN_DATA = "kebun_data"

        fun newInstance(kebunId: Int, kebunData: com.example.sawit.model.KebunData): CatatPanenFragment {
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
        arguments?.let {
            kebunId = it.getInt(ARG_KEBUN_ID, 0)
            kebunData = it.getParcelable(ARG_KEBUN_DATA)
        }
        panenManager = PanenManager.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_catat_panen, container, false)

        initViews(rootView)
        setupListeners()

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
        datePickerDialog.show()
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        etTanggalPanen.setText(dateFormat.format(calendar.time))
    }

    private fun submitData() {
        android.util.Log.d("CatatPanen", "=== SUBMIT DATA START ===")

        // Validasi input
        val tanggal = etTanggalPanen.text.toString().trim()
        val lokasi = etLokasi.text.toString().trim()
        val tbsMatangStr = etTBSMatang.text.toString().trim()
        val tbsTidakMatangStr = etTBSTidakMatang.text.toString().trim()
        val tbsKelewatMatangStr = etTBSKelewatMatang.text.toString().trim()

        android.util.Log.d("CatatPanen", "Tanggal: $tanggal")
        android.util.Log.d("CatatPanen", "Lokasi: $lokasi")

        if (tanggal.isEmpty()) {
            showToast("Pilih tanggal panen")
            return
        }

        if (lokasi.isEmpty()) {
            showToast("Masukkan lokasi")
            return
        }

        if (tbsMatangStr.isEmpty() && tbsTidakMatangStr.isEmpty() && tbsKelewatMatangStr.isEmpty()) {
            showToast("Masukkan minimal satu data berat TBS")
            return
        }

        // Konversi ke double (default 0 jika kosong)
        val tbsMatang = tbsMatangStr.toDoubleOrNull() ?: 0.0
        val tbsTidakMatang = tbsTidakMatangStr.toDoubleOrNull() ?: 0.0
        val tbsKelewatMatang = tbsKelewatMatangStr.toDoubleOrNull() ?: 0.0

        android.util.Log.d("CatatPanen", "TBS Matang: $tbsMatang kg")

        // Hitung jumlah buah (estimasi: 1 buah = 10kg)
        val jumlahMatang = (tbsMatang / 10).toInt()
        val jumlahTidakMatang = (tbsTidakMatang / 10).toInt()
        val jumlahKelewatMatang = (tbsKelewatMatang / 10).toInt()

        // Buat object PanenData
        val panenData = PanenData(
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

        android.util.Log.d("CatatPanen", "PanenData created")

        // Simpan data
        val isSaved = panenManager.savePanen(panenData)

        android.util.Log.d("CatatPanen", "Is Saved: $isSaved")

        if (isSaved) {
            showToast("Data panen berhasil disimpan")

            // Validasi kebunData
            android.util.Log.d("CatatPanen", "KebunData: ${kebunData?.namaKebun}")
            android.util.Log.d("CatatPanen", "KebunData null?: ${kebunData == null}")

            if (kebunData == null) {
                android.util.Log.e("CatatPanen", "ERROR: KebunData is NULL!")
                showToast("Error: Data kebun tidak tersedia")
                activity?.supportFragmentManager?.popBackStack()
                return
            }

            // Ambil semua data panen untuk kebun ini
            val panenList = panenManager.getPanenByKebunId(kebunId)
            android.util.Log.d("CatatPanen", "PanenList size: ${panenList.size}")

            // Navigate ke DetailLaporanFragment
            android.util.Log.d("CatatPanen", "Calling navigateToDetailLaporan...")
            navigateToDetailLaporan(kebunData!!, panenList)
        } else {
            android.util.Log.e("CatatPanen", "ERROR: Failed to save!")
            showToast("Gagal menyimpan data panen")
        }

        android.util.Log.d("CatatPanen", "=== SUBMIT DATA END ===")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToDetailLaporan(
        kebunData: com.example.sawit.model.KebunData,
        panenList: List<PanenData>
    ) {
        android.util.Log.d("CatatPanen", "=== NAVIGATE START ===")
        android.util.Log.d("CatatPanen", "Activity: $activity")
        android.util.Log.d("CatatPanen", "FragmentManager: ${activity?.supportFragmentManager}")

        try {
            // Buat DetailLaporanFragment dengan data
            val detailLaporanFragment = DetailLaporanFragment.newInstance(kebunData, panenList)
            android.util.Log.d("CatatPanen", "DetailLaporanFragment created")

            activity?.supportFragmentManager?.beginTransaction()?.apply {
                android.util.Log.d("CatatPanen", "Transaction started")
                replace(R.id.fragmentContainer, detailLaporanFragment)
                android.util.Log.d("CatatPanen", "Replace called")
                addToBackStack(null)
                android.util.Log.d("CatatPanen", "AddToBackStack called")
                commit()
                android.util.Log.d("CatatPanen", "Commit called")
            }

            android.util.Log.d("CatatPanen", "=== NAVIGATE END ===")
        } catch (e: Exception) {
            android.util.Log.e("CatatPanen", "ERROR in navigation: ${e.message}", e)
            showToast("Error: ${e.message}")
        }
    }
}