package com.example.sawit.IsiDashboard

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.example.sawit.R
import com.example.sawit.model.KebunData
import com.example.sawit.utils.KebunManager
import java.util.Calendar

class TambahKebunFragment : Fragment() {

    private lateinit var btnBack: ImageView
    private lateinit var etNamaKebun: EditText
    private lateinit var etLuasLahan: EditText
    private lateinit var etLokasiKebun: EditText
    private lateinit var etJenisBibit: EditText
    private lateinit var etJumlahTanaman: EditText
    private lateinit var etTahunTanam: EditText
    private lateinit var layoutTahunTanam: RelativeLayout
    private lateinit var btnMineral: CardView
    private lateinit var btnGambut: CardView
    private lateinit var btnMineralVolkanik: CardView
    private lateinit var btnMineralBerpasir: CardView
    private lateinit var btnSubmit: CardView

    private lateinit var kebunManager: KebunManager
    private var selectedJenisTanah: String = "Mineral" // Default

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_tambah_kebun, container, false)

        // Initialize KebunManager
        kebunManager = KebunManager.getInstance(requireContext())

        initViews(rootView)
        setupListeners()

        // Set initial UI state sesuai dengan default selection
        updateJenisTanahUI()

        return rootView
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        etNamaKebun = view.findViewById(R.id.etNamaKebun)
        etLuasLahan = view.findViewById(R.id.etLuasLahan)
        etLokasiKebun = view.findViewById(R.id.etLokasiKebun)
        etJenisBibit = view.findViewById(R.id.etJenisBibit)
        etJumlahTanaman = view.findViewById(R.id.etJumlahTanaman)
        etTahunTanam = view.findViewById(R.id.etTahunTanam)
        layoutTahunTanam = view.findViewById(R.id.layoutTahunTanam)
        btnMineral = view.findViewById(R.id.btnMineral)
        btnGambut = view.findViewById(R.id.btnGambut)
        btnMineralVolkanik = view.findViewById(R.id.btnMineralVolkanik)
        btnMineralBerpasir = view.findViewById(R.id.btnMineralBerpasir)
        btnSubmit = view.findViewById(R.id.btnSubmit)
    }

    private fun setupListeners() {
        // Back button
        btnBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        // Date picker untuk tahun tanam
        layoutTahunTanam.setOnClickListener {
            showDatePicker()
        }

        // Jenis Tanah Selection
        btnMineral.setOnClickListener {
            selectedJenisTanah = "Mineral"
            updateJenisTanahUI()
        }

        btnGambut.setOnClickListener {
            selectedJenisTanah = "Gambut"
            updateJenisTanahUI()
        }

        btnMineralVolkanik.setOnClickListener {
            selectedJenisTanah = "Mineral Volkanik"
            updateJenisTanahUI()
        }

        btnMineralBerpasir.setOnClickListener {
            selectedJenisTanah = "Mineral Berpasir"
            updateJenisTanahUI()
        }

        // Submit button
        btnSubmit.setOnClickListener {
            submitKebun()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, _ ->
                val monthName = getMonthName(selectedMonth)
                etTahunTanam.setText("$monthName $selectedYear")
            },
            year,
            month,
            1
        )

        datePickerDialog.show()
    }

    private fun getMonthName(month: Int): String {
        val months = arrayOf(
            "Januari", "Februari", "Maret", "April", "Mei", "Juni",
            "Juli", "Agustus", "September", "Oktober", "November", "Desember"
        )
        return months[month]
    }

    private fun updateJenisTanahUI() {
        // Reset semua tombol ke state tidak aktif (hijau muda)
        setButtonUnselected(btnMineral)
        setButtonUnselected(btnGambut)
        setButtonUnselected(btnMineralVolkanik)
        setButtonUnselected(btnMineralBerpasir)

        // Set tombol yang dipilih ke state aktif (hijau tua)
        when (selectedJenisTanah) {
            "Mineral" -> setButtonSelected(btnMineral)
            "Gambut" -> setButtonSelected(btnGambut)
            "Mineral Volkanik" -> setButtonSelected(btnMineralVolkanik)
            "Mineral Berpasir" -> setButtonSelected(btnMineralBerpasir)
        }
    }

    private fun setButtonSelected(button: CardView) {
        // Warna AKTIF: Background hijau tua (#4CAF50), text putih, bold
        button.setCardBackgroundColor(0xFF4CAF50.toInt())
        val textView = button.getChildAt(0) as? android.widget.TextView
        textView?.apply {
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }

    private fun setButtonUnselected(button: CardView) {
        // Warna TIDAK AKTIF: Background hijau muda (#E8F5E9), text hijau (#4CAF50), normal
        button.setCardBackgroundColor(0xFFE8F5E9.toInt())
        val textView = button.getChildAt(0) as? android.widget.TextView
        textView?.apply {
            setTextColor(0xFF4CAF50.toInt())
            setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }

    private fun submitKebun() {
        // Validasi input
        val namaKebun = etNamaKebun.text.toString().trim()
        val luasLahanStr = etLuasLahan.text.toString().trim()
        val lokasiKebun = etLokasiKebun.text.toString().trim()
        val jenisBibit = etJenisBibit.text.toString().trim()
        val jumlahTanamanStr = etJumlahTanaman.text.toString().trim()
        val tahunTanam = etTahunTanam.text.toString().trim()

        // Validasi field wajib
        if (namaKebun.isEmpty()) {
            showToast("Nama kebun harus diisi")
            return
        }

        if (luasLahanStr.isEmpty()) {
            showToast("Luas lahan harus diisi")
            return
        }

        if (lokasiKebun.isEmpty()) {
            showToast("Lokasi kebun harus diisi")
            return
        }

        if (jenisBibit.isEmpty()) {
            showToast("Jenis bibit harus diisi")
            return
        }

        if (tahunTanam.isEmpty()) {
            showToast("Tahun tanam harus diisi")
            return
        }

        // Parse data
        val luasLahan = luasLahanStr.toDoubleOrNull() ?: 0.0
        val jumlahTanaman = if (jumlahTanamanStr.isNotEmpty()) {
            jumlahTanamanStr.toIntOrNull() ?: 0
        } else {
            0
        }

        // Create KebunData object
        val kebunData = KebunData(
            namaKebun = namaKebun,
            luasLahan = luasLahan,
            lokasiKebun = lokasiKebun,
            jenisBibit = jenisBibit,
            jumlahTanaman = jumlahTanaman,
            tahunTanam = tahunTanam,
            jenisTanah = selectedJenisTanah
        )

        // Save to KebunManager
        val isSaved = kebunManager.saveKebun(kebunData)

        if (isSaved) {
            showToast("Kebun berhasil ditambahkan")

            // Clear form
            clearForm()

            // Navigate ke LihatKebunFragment
            navigateToLihatKebun()
        } else {
            showToast("Gagal menambahkan kebun")
        }
    }

    private fun clearForm() {
        etNamaKebun.text?.clear()
        etLuasLahan.text?.clear()
        etLokasiKebun.text?.clear()
        etJenisBibit.text?.clear()
        etJumlahTanaman.text?.clear()
        etTahunTanam.text?.clear()
        selectedJenisTanah = "Mineral"
        updateJenisTanahUI()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLihatKebun() {
        // Opsi 1: Replace dengan fragment baru
        val lihatKebunFragment = LihatKebunFragment()

        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.fragmentContainer, lihatKebunFragment)
            addToBackStack(null)
            commit()
        }

        // Opsi 2: Jika ingin pop back ke fragment sebelumnya
        // activity?.supportFragmentManager?.popBackStack()
    }
}