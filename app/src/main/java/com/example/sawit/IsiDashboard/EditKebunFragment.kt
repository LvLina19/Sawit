package com.example.sawit.IsiDashboard

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import com.example.sawit.R
import com.example.sawit.model.KebunData
import com.example.sawit.utils.KebunManager
import java.util.Calendar

class EditKebunFragment : Fragment() {
    private lateinit var btnBack: ImageView
    private lateinit var etNamaKebun: EditText
    private lateinit var etLuasLahan: EditText
    private lateinit var etLokasiKebun: EditText
    private lateinit var spinnerJenisBibit: Spinner
    private lateinit var etJumlahTanaman: EditText
    private lateinit var spinnerTahunTanam: Spinner

    // CardView untuk Jenis Tanah
    private lateinit var btnMineral: CardView
    private lateinit var btnGambut: CardView
    private lateinit var btnMineralVolkanik: CardView
    private lateinit var btnMineralBerpasir: CardView

    private lateinit var btnSimpan: CardView
    private lateinit var progressBar: ProgressBar

    private lateinit var kebunManager: KebunManager
    private var kebunData: KebunData? = null
    private var selectedJenisTanah: String = ""

    companion object {
        private const val TAG = "EditKebunFragment"
        private const val ARG_KEBUN_DATA = "kebun_data"

        fun newInstance(kebunData: KebunData): EditKebunFragment {
            val fragment = EditKebunFragment()
            val args = Bundle()
            args.putParcelable(ARG_KEBUN_DATA, kebunData)
            fragment.arguments = args
            Log.d(TAG, "newInstance created with kebun: ${kebunData.namaKebun}")
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate called")
        kebunData = arguments?.getParcelable(ARG_KEBUN_DATA)
        Log.d(TAG, "Kebun data received: ${kebunData?.namaKebun}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView called")
        val rootView = inflater.inflate(R.layout.fragment_edit_kebun, container, false)

        kebunManager = KebunManager.getInstance(requireContext())

        if (kebunData == null) {
            Log.e(TAG, "Kebun data is null!")
            showToast("Data kebun tidak ditemukan")
            activity?.supportFragmentManager?.popBackStack()
            return rootView
        }

        initViews(rootView)
        setupSpinners()
        setupJenisTanahButtons()
        fillFormWithData()

        return rootView
    }

    private fun initViews(view: View) {
        try {
            btnBack = view.findViewById(R.id.btnBack)
            etNamaKebun = view.findViewById(R.id.etNamaKebun)
            etLuasLahan = view.findViewById(R.id.etLuasLahan)
            etLokasiKebun = view.findViewById(R.id.etLokasiKebun)
            spinnerJenisBibit = view.findViewById(R.id.spinnerJenisBibit)
            etJumlahTanaman = view.findViewById(R.id.etJumlahTanaman)
            spinnerTahunTanam = view.findViewById(R.id.spinnerTahunTanam)

            // Jenis Tanah CardViews
            btnMineral = view.findViewById(R.id.btnMineral)
            btnGambut = view.findViewById(R.id.btnGambut)
            btnMineralVolkanik = view.findViewById(R.id.btnMineralVolkanik)
            btnMineralBerpasir = view.findViewById(R.id.btnMineralBerpasir)

            btnSimpan = view.findViewById(R.id.btnSimpan)
            progressBar = view.findViewById(R.id.progressBar)

            btnBack.setOnClickListener {
                showCancelConfirmation()
            }

            btnSimpan.setOnClickListener {
                if (validateInput()) {
                    updateKebun()
                }
            }

            Log.d(TAG, "All views initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing views: ${e.message}", e)
            showToast("Error: ${e.message}")
        }
    }

    private fun setupSpinners() {
        // Spinner Jenis Bibit
        val jenisBibitList = listOf(
            "Pilih Jenis Bibit",
            "Tenera",
            "Dura",
            "Pisifera",
            "DxP (Tenera Hibrida)"
        )
        val bibitAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            jenisBibitList
        )
        bibitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerJenisBibit.adapter = bibitAdapter

        // Spinner Tahun Tanam
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val tahunList = mutableListOf("Pilih Tahun Tanam")
        for (year in currentYear downTo 1980) {
            tahunList.add("Tahun $year")
        }
        val tahunAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            tahunList
        )
        tahunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTahunTanam.adapter = tahunAdapter
    }

    private fun setupJenisTanahButtons() {
        btnMineral.setOnClickListener {
            selectJenisTanah("Mineral", btnMineral)
        }

        btnGambut.setOnClickListener {
            selectJenisTanah("Gambut", btnGambut)
        }

        btnMineralVolkanik.setOnClickListener {
            selectJenisTanah("Mineral Volkanik", btnMineralVolkanik)
        }

        btnMineralBerpasir.setOnClickListener {
            selectJenisTanah("Mineral Berpasir", btnMineralBerpasir)
        }
    }

    private fun selectJenisTanah(jenisTanah: String, selectedButton: CardView) {
        selectedJenisTanah = jenisTanah
        Log.d(TAG, "Selected jenis tanah: $jenisTanah")

        // Reset semua button ke state tidak aktif
        resetJenisTanahButtons()

        // Set button yang dipilih ke state aktif
        selectedButton.apply {
            setCardBackgroundColor(resources.getColor(R.color.green_primary, null))
            alpha = 1.0f
        }

        // Update text color dari TextView di dalam CardView
        val textView = selectedButton.getChildAt(0) as? TextView
        textView?.apply {
            setTextColor(resources.getColor(android.R.color.white, null))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }

    private fun resetJenisTanahButtons() {
        val buttons = listOf(btnMineral, btnGambut, btnMineralVolkanik, btnMineralBerpasir)

        buttons.forEach { button ->
            button.apply {
                setCardBackgroundColor(resources.getColor(R.color.green_light, null))
                alpha = 0.6f
            }

            val textView = button.getChildAt(0) as? TextView
            textView?.apply {
                setTextColor(resources.getColor(R.color.green_primary, null))
                setTypeface(null, android.graphics.Typeface.NORMAL)
            }
        }
    }

    private fun fillFormWithData() {
        kebunData?.let { data ->
            Log.d(TAG, "Filling form with data: ${data.namaKebun}")

            etNamaKebun.setText(data.namaKebun)
            etLuasLahan.setText(data.luasLahan.toString())
            etLokasiKebun.setText(data.lokasiKebun)

            if (data.jumlahTanaman > 0) {
                etJumlahTanaman.setText(data.jumlahTanaman.toString())
            }

            // Set spinner selections
            setSpinnerSelection(spinnerJenisBibit, data.jenisBibit)
            setSpinnerSelection(spinnerTahunTanam, data.tahunTanam)

            // Set jenis tanah
            setJenisTanahSelection(data.jenisTanah)
        }
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String) {
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == value) {
                spinner.setSelection(i)
                break
            }
        }
    }

    private fun setJenisTanahSelection(jenisTanah: String) {
        Log.d(TAG, "Setting jenis tanah selection: $jenisTanah")
        when (jenisTanah) {
            "Mineral" -> selectJenisTanah("Mineral", btnMineral)
            "Gambut" -> selectJenisTanah("Gambut", btnGambut)
            "Mineral Volkanik" -> selectJenisTanah("Mineral Volkanik", btnMineralVolkanik)
            "Mineral Berpasir" -> selectJenisTanah("Mineral Berpasir", btnMineralBerpasir)
        }
    }

    private fun validateInput(): Boolean {
        val namaKebun = etNamaKebun.text.toString().trim()
        val luasLahanStr = etLuasLahan.text.toString().trim()
        val lokasiKebun = etLokasiKebun.text.toString().trim()
        val jenisBibit = spinnerJenisBibit.selectedItem.toString()
        val tahunTanam = spinnerTahunTanam.selectedItem.toString()

        when {
            namaKebun.isEmpty() -> {
                etNamaKebun.error = "Nama kebun harus diisi"
                etNamaKebun.requestFocus()
                return false
            }
            luasLahanStr.isEmpty() -> {
                etLuasLahan.error = "Luas lahan harus diisi"
                etLuasLahan.requestFocus()
                return false
            }
            luasLahanStr.toDoubleOrNull() == null || luasLahanStr.toDouble() <= 0 -> {
                etLuasLahan.error = "Luas lahan harus berupa angka positif"
                etLuasLahan.requestFocus()
                return false
            }
            lokasiKebun.isEmpty() -> {
                etLokasiKebun.error = "Lokasi kebun harus diisi"
                etLokasiKebun.requestFocus()
                return false
            }
            jenisBibit == "Pilih Jenis Bibit" -> {
                showToast("Pilih jenis bibit terlebih dahulu")
                return false
            }
            tahunTanam == "Pilih Tahun Tanam" -> {
                showToast("Pilih tahun tanam terlebih dahulu")
                return false
            }
            selectedJenisTanah.isEmpty() -> {
                showToast("Pilih jenis tanah terlebih dahulu")
                return false
            }
        }

        return true
    }

    private fun updateKebun() {
        showLoading(true)

        val updatedKebun = kebunData!!.copy(
            namaKebun = etNamaKebun.text.toString().trim(),
            luasLahan = etLuasLahan.text.toString().toDouble(),
            lokasiKebun = etLokasiKebun.text.toString().trim(),
            jenisBibit = spinnerJenisBibit.selectedItem.toString(),
            jumlahTanaman = etJumlahTanaman.text.toString().toIntOrNull() ?: 0,
            tahunTanam = spinnerTahunTanam.selectedItem.toString(),
            jenisTanah = selectedJenisTanah
        )

        Log.d(TAG, "Updating kebun with ID: ${updatedKebun.id}")

        kebunManager.updateKebun(updatedKebun) { success, errorMessage ->
            activity?.runOnUiThread {
                showLoading(false)

                if (success) {
                    Log.d(TAG, "Kebun updated successfully")
                    showSuccessDialog()
                } else {
                    Log.e(TAG, "Failed to update kebun: $errorMessage")
                    showToast("Gagal mengupdate kebun: ${errorMessage ?: "Unknown error"}")
                }
            }
        }
    }

    private fun showSuccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Berhasil")
            .setMessage("Data kebun berhasil diupdate!")
            .setPositiveButton("OK") { _, _ ->
                activity?.supportFragmentManager?.popBackStack()
            }
            .setCancelable(false)
            .show()
    }

    private fun showCancelConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Batalkan Edit")
            .setMessage("Perubahan yang belum disimpan akan hilang. Yakin ingin membatalkan?")
            .setPositiveButton("Ya, Batalkan") { _, _ ->
                activity?.supportFragmentManager?.popBackStack()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSimpan.isEnabled = !show
        btnSimpan.isClickable = !show
        btnBack.isEnabled = !show
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}


//package com.example.sawit.IsiDashboard
//
//import android.app.AlertDialog
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.*
//import androidx.cardview.widget.CardView
//import com.example.sawit.R
//import com.example.sawit.model.KebunData
//import com.example.sawit.utils.KebunManager
//import java.util.Calendar
//
//class EditKebunFragment : Fragment() {
//    private lateinit var btnBack: ImageView
//    private lateinit var etNamaKebun: EditText
//    private lateinit var etLuasLahan: EditText
//    private lateinit var etLokasiKebun: EditText
//    private lateinit var spinnerJenisBibit: Spinner
//    private lateinit var etJumlahTanaman: EditText
//    private lateinit var spinnerTahunTanam: Spinner
//
//    // CardView untuk Jenis Tanah
//    private lateinit var btnMineral: CardView
//    private lateinit var btnGambut: CardView
//    private lateinit var btnMineralVolkanik: CardView
//    private lateinit var btnMineralBerpasir: CardView
//
//    private lateinit var btnSimpan: CardView
//    private lateinit var progressBar: ProgressBar
//
//    private lateinit var kebunManager: KebunManager
//    private var kebunData: KebunData? = null
//    private var selectedJenisTanah: String = ""
//
//    companion object {
//        private const val ARG_KEBUN_DATA = "kebun_data"
//
//        fun newInstance(kebunData: KebunData): EditKebunFragment {
//            val fragment = EditKebunFragment()
//            val args = Bundle()
//            args.putParcelable(ARG_KEBUN_DATA, kebunData)
//            fragment.arguments = args
//            return fragment
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val rootView = inflater.inflate(R.layout.fragment_edit_kebun, container, false)
//
//        kebunManager = KebunManager.getInstance(requireContext())
//        kebunData = arguments?.getParcelable(ARG_KEBUN_DATA)
//
//        if (kebunData == null) {
//            showToast("Data kebun tidak ditemukan")
//            activity?.supportFragmentManager?.popBackStack()
//            return rootView
//        }
//
//        initViews(rootView)
//        setupSpinners()
//        setupJenisTanahButtons()
//        fillFormWithData()
//
//        return rootView
//    }
//
//    private fun initViews(view: View) {
//        btnBack = view.findViewById(R.id.btnBack)
//        etNamaKebun = view.findViewById(R.id.etNamaKebun)
//        etLuasLahan = view.findViewById(R.id.etLuasLahan)
//        etLokasiKebun = view.findViewById(R.id.etLokasiKebun)
//        spinnerJenisBibit = view.findViewById(R.id.spinnerJenisBibit)
//        etJumlahTanaman = view.findViewById(R.id.etJumlahTanaman)
//        spinnerTahunTanam = view.findViewById(R.id.spinnerTahunTanam)
//
//        // Jenis Tanah CardViews
//        btnMineral = view.findViewById(R.id.btnMineral)
//        btnGambut = view.findViewById(R.id.btnGambut)
//        btnMineralVolkanik = view.findViewById(R.id.btnMineralVolkanik)
//        btnMineralBerpasir = view.findViewById(R.id.btnMineralBerpasir)
//
//        btnSimpan = view.findViewById(R.id.btnSimpan)
//        progressBar = view.findViewById(R.id.progressBar)
//
//        btnBack.setOnClickListener {
//            showCancelConfirmation()
//        }
//
//        btnSimpan.setOnClickListener {
//            if (validateInput()) {
//                updateKebun()
//            }
//        }
//    }
//
//    private fun setupSpinners() {
//        // Spinner Jenis Bibit
//        val jenisBibitList = listOf(
//            "Pilih Jenis Bibit",
//            "Tenera",
//            "Dura",
//            "Pisifera",
//            "DxP (Tenera Hibrida)"
//        )
//        val bibitAdapter = ArrayAdapter(
//            requireContext(),
//            android.R.layout.simple_spinner_item,
//            jenisBibitList
//        )
//        bibitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerJenisBibit.adapter = bibitAdapter
//
//        // Spinner Tahun Tanam
//        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
//        val tahunList = mutableListOf("Pilih Tahun Tanam")
//        for (year in currentYear downTo 1980) {
//            tahunList.add("Tahun $year")
//        }
//        val tahunAdapter = ArrayAdapter(
//            requireContext(),
//            android.R.layout.simple_spinner_item,
//            tahunList
//        )
//        tahunAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinnerTahunTanam.adapter = tahunAdapter
//    }
//
//    private fun setupJenisTanahButtons() {
//        btnMineral.setOnClickListener {
//            selectJenisTanah("Mineral", btnMineral)
//        }
//
//        btnGambut.setOnClickListener {
//            selectJenisTanah("Gambut", btnGambut)
//        }
//
//        btnMineralVolkanik.setOnClickListener {
//            selectJenisTanah("Mineral Volkanik", btnMineralVolkanik)
//        }
//
//        btnMineralBerpasir.setOnClickListener {
//            selectJenisTanah("Mineral Berpasir", btnMineralBerpasir)
//        }
//    }
//
//    private fun selectJenisTanah(jenisTanah: String, selectedButton: CardView) {
//        selectedJenisTanah = jenisTanah
//
//        // Reset semua button ke state tidak aktif
//        resetJenisTanahButtons()
//
//        // Set button yang dipilih ke state aktif
//        selectedButton.setCardBackgroundColor(
//            resources.getColor(android.R.color.holo_green_light, null)
//        )
//
//        // Update text color
//        val textView = selectedButton.findViewById<TextView>(
//            when (selectedButton.id) {
//                R.id.btnMineral -> R.id.btnMineral
//                R.id.btnGambut -> R.id.btnGambut
//                R.id.btnMineralVolkanik -> R.id.btnMineralVolkanik
//                else -> R.id.btnMineralBerpasir
//            }
//        )
//
//        (selectedButton.getChildAt(0) as? TextView)?.apply {
//            setTextColor(resources.getColor(android.R.color.white, null))
//            setTypeface(null, android.graphics.Typeface.BOLD)
//        }
//    }
//
//    private fun resetJenisTanahButtons() {
//        val buttons = listOf(btnMineral, btnGambut, btnMineralVolkanik, btnMineralBerpasir)
//
//        buttons.forEach { button ->
//            button.setCardBackgroundColor(
//                resources.getColor(android.R.color.holo_green_light, null)
//            )
//            button.alpha = 0.3f
//
//            (button.getChildAt(0) as? TextView)?.apply {
//                setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
//                setTypeface(null, android.graphics.Typeface.NORMAL)
//            }
//        }
//    }
//
//    private fun fillFormWithData() {
//        kebunData?.let { data ->
//            etNamaKebun.setText(data.namaKebun)
//            etLuasLahan.setText(data.luasLahan.toString())
//            etLokasiKebun.setText(data.lokasiKebun)
//
//            if (data.jumlahTanaman > 0) {
//                etJumlahTanaman.setText(data.jumlahTanaman.toString())
//            }
//
//            // Set spinner selections
//            setSpinnerSelection(spinnerJenisBibit, data.jenisBibit)
//            setSpinnerSelection(spinnerTahunTanam, data.tahunTanam)
//
//            // Set jenis tanah
//            setJenisTanahSelection(data.jenisTanah)
//        }
//    }
//
//    private fun setSpinnerSelection(spinner: Spinner, value: String) {
//        val adapter = spinner.adapter
//        for (i in 0 until adapter.count) {
//            if (adapter.getItem(i).toString() == value) {
//                spinner.setSelection(i)
//                break
//            }
//        }
//    }
//
//    private fun setJenisTanahSelection(jenisTanah: String) {
//        when (jenisTanah) {
//            "Mineral" -> selectJenisTanah("Mineral", btnMineral)
//            "Gambut" -> selectJenisTanah("Gambut", btnGambut)
//            "Mineral Volkanik" -> selectJenisTanah("Mineral Volkanik", btnMineralVolkanik)
//            "Mineral Berpasir" -> selectJenisTanah("Mineral Berpasir", btnMineralBerpasir)
//        }
//    }
//
//    private fun validateInput(): Boolean {
//        val namaKebun = etNamaKebun.text.toString().trim()
//        val luasLahanStr = etLuasLahan.text.toString().trim()
//        val lokasiKebun = etLokasiKebun.text.toString().trim()
//        val jenisBibit = spinnerJenisBibit.selectedItem.toString()
//        val tahunTanam = spinnerTahunTanam.selectedItem.toString()
//
//        when {
//            namaKebun.isEmpty() -> {
//                etNamaKebun.error = "Nama kebun harus diisi"
//                etNamaKebun.requestFocus()
//                return false
//            }
//            luasLahanStr.isEmpty() -> {
//                etLuasLahan.error = "Luas lahan harus diisi"
//                etLuasLahan.requestFocus()
//                return false
//            }
//            luasLahanStr.toDoubleOrNull() == null || luasLahanStr.toDouble() <= 0 -> {
//                etLuasLahan.error = "Luas lahan harus berupa angka positif"
//                etLuasLahan.requestFocus()
//                return false
//            }
//            lokasiKebun.isEmpty() -> {
//                etLokasiKebun.error = "Lokasi kebun harus diisi"
//                etLokasiKebun.requestFocus()
//                return false
//            }
//            jenisBibit == "Pilih Jenis Bibit" -> {
//                showToast("Pilih jenis bibit terlebih dahulu")
//                return false
//            }
//            tahunTanam == "Pilih Tahun Tanam" -> {
//                showToast("Pilih tahun tanam terlebih dahulu")
//                return false
//            }
//            selectedJenisTanah.isEmpty() -> {
//                showToast("Pilih jenis tanah terlebih dahulu")
//                return false
//            }
//        }
//
//        return true
//    }
//
//    private fun updateKebun() {
//        showLoading(true)
//
//        val updatedKebun = kebunData!!.copy(
//            namaKebun = etNamaKebun.text.toString().trim(),
//            luasLahan = etLuasLahan.text.toString().toDouble(),
//            lokasiKebun = etLokasiKebun.text.toString().trim(),
//            jenisBibit = spinnerJenisBibit.selectedItem.toString(),
//            jumlahTanaman = etJumlahTanaman.text.toString().toIntOrNull() ?: 0,
//            tahunTanam = spinnerTahunTanam.selectedItem.toString(),
//            jenisTanah = selectedJenisTanah
//        )
//
//        kebunManager.updateKebun(updatedKebun) { success, errorMessage ->
//            showLoading(false)
//
//            if (success) {
//                showSuccessDialog()
//            } else {
//                showToast("Gagal mengupdate kebun: ${errorMessage ?: "Unknown error"}")
//            }
//        }
//    }
//
//    private fun showSuccessDialog() {
//        AlertDialog.Builder(requireContext())
//            .setTitle("Berhasil")
//            .setMessage("Data kebun berhasil diupdate!")
//            .setPositiveButton("OK") { _, _ ->
//                activity?.supportFragmentManager?.popBackStack()
//            }
//            .setCancelable(false)
//            .show()
//    }
//
//    private fun showCancelConfirmation() {
//        AlertDialog.Builder(requireContext())
//            .setTitle("Batalkan Edit")
//            .setMessage("Perubahan yang belum disimpan akan hilang. Yakin ingin membatalkan?")
//            .setPositiveButton("Ya, Batalkan") { _, _ ->
//                activity?.supportFragmentManager?.popBackStack()
//            }
//            .setNegativeButton("Tidak", null)
//            .show()
//    }
//
//    private fun showLoading(show: Boolean) {
//        progressBar.visibility = if (show) View.VISIBLE else View.GONE
//        btnSimpan.isEnabled = !show
//        btnSimpan.isClickable = !show
//        btnBack.isEnabled = !show
//    }
//
//    private fun showToast(message: String) {
//        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//    }
//}