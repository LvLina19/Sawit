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
import android.widget.TextView
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

    // TextViews untuk mengubah warna teks
    private lateinit var tvMineral: TextView
    private lateinit var tvGambut: TextView
    private lateinit var tvMineralVolkanik: TextView
    private lateinit var tvMineralBerpasir: TextView

    private var selectedJenisTanah: String = "Mineral"
    private lateinit var kebunManager: KebunManager

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

        // Initialize TextViews untuk mengubah warna teks
        tvMineral = btnMineral.findViewById<TextView>(android.R.id.text1) ?:
                btnMineral.getChildAt(0) as TextView
        tvGambut = btnGambut.findViewById<TextView>(android.R.id.text1) ?:
                btnGambut.getChildAt(0) as TextView
        tvMineralVolkanik = btnMineralVolkanik.findViewById<TextView>(android.R.id.text1) ?:
                btnMineralVolkanik.getChildAt(0) as TextView
        tvMineralBerpasir = btnMineralBerpasir.findViewById<TextView>(android.R.id.text1) ?:
                btnMineralBerpasir.getChildAt(0) as TextView
    }

    private fun setupListeners() {
        // Back button
        btnBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

        // Tahun Tanam picker
        layoutTahunTanam.setOnClickListener {
            showMonthYearPicker()
        }

        // Jenis Tanah Selection
        btnMineral.setOnClickListener {
            selectJenisTanah("Mineral")
        }

        btnGambut.setOnClickListener {
            selectJenisTanah("Gambut")
        }

        btnMineralVolkanik.setOnClickListener {
            selectJenisTanah("Mineral Volkanik")
        }

        btnMineralBerpasir.setOnClickListener {
            selectJenisTanah("Mineral Berpasir")
        }

        // Submit button
        btnSubmit.setOnClickListener {
            submitData()
        }
    }

    private fun showMonthYearPicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, _ ->
                val monthNames = arrayOf(
                    "Januari", "Februari", "Maret", "April", "Mei", "Juni",
                    "Juli", "Agustus", "September", "Oktober", "November", "Desember"
                )
                val selectedDate = "${monthNames[selectedMonth]} $selectedYear"
                etTahunTanam.setText(selectedDate)
            },
            year, month, 1
        )

        datePickerDialog.show()
    }

    private fun selectJenisTanah(jenisTanah: String) {
        selectedJenisTanah = jenisTanah

        // Warna unselected (hijau muda) dan selected (hijau tua)
        val colorUnselected = android.graphics.Color.parseColor("#E8F5E9")
        val colorSelected = android.graphics.Color.parseColor("#4CAF50")
        val textColorUnselected = android.graphics.Color.parseColor("#4CAF50")
        val textColorSelected = android.graphics.Color.parseColor("#FFFFFF")

        // Reset all buttons to unselected state
        btnMineral.setCardBackgroundColor(colorUnselected)
        btnGambut.setCardBackgroundColor(colorUnselected)
        btnMineralVolkanik.setCardBackgroundColor(colorUnselected)
        btnMineralBerpasir.setCardBackgroundColor(colorUnselected)

        // Reset all text colors to unselected
        tvMineral.setTextColor(textColorUnselected)
        tvGambut.setTextColor(textColorUnselected)
        tvMineralVolkanik.setTextColor(textColorUnselected)
        tvMineralBerpasir.setTextColor(textColorUnselected)

        // Reset text style
        tvMineral.setTypeface(null, android.graphics.Typeface.NORMAL)
        tvGambut.setTypeface(null, android.graphics.Typeface.NORMAL)
        tvMineralVolkanik.setTypeface(null, android.graphics.Typeface.NORMAL)
        tvMineralBerpasir.setTypeface(null, android.graphics.Typeface.NORMAL)

        // Set selected button
        when (jenisTanah) {
            "Mineral" -> {
                btnMineral.setCardBackgroundColor(colorSelected)
                tvMineral.setTextColor(textColorSelected)
                tvMineral.setTypeface(null, android.graphics.Typeface.BOLD)
            }
            "Gambut" -> {
                btnGambut.setCardBackgroundColor(colorSelected)
                tvGambut.setTextColor(textColorSelected)
                tvGambut.setTypeface(null, android.graphics.Typeface.BOLD)
            }
            "Mineral Volkanik" -> {
                btnMineralVolkanik.setCardBackgroundColor(colorSelected)
                tvMineralVolkanik.setTextColor(textColorSelected)
                tvMineralVolkanik.setTypeface(null, android.graphics.Typeface.BOLD)
            }
            "Mineral Berpasir" -> {
                btnMineralBerpasir.setCardBackgroundColor(colorSelected)
                tvMineralBerpasir.setTextColor(textColorSelected)
                tvMineralBerpasir.setTypeface(null, android.graphics.Typeface.BOLD)
            }
        }
    }

    private fun submitData() {
        val namaKebun = etNamaKebun.text.toString().trim()
        val luasLahan = etLuasLahan.text.toString().trim()
        val lokasiKebun = etLokasiKebun.text.toString().trim()
        val jenisBibit = etJenisBibit.text.toString().trim()
        val jumlahTanaman = etJumlahTanaman.text.toString().trim()
        val tahunTanam = etTahunTanam.text.toString().trim()

        // Validasi
        when {
            namaKebun.isEmpty() -> {
                Toast.makeText(requireContext(), "Silakan masukkan nama kebun", Toast.LENGTH_SHORT).show()
                return
            }
            luasLahan.isEmpty() -> {
                Toast.makeText(requireContext(), "Silakan masukkan luas lahan", Toast.LENGTH_SHORT).show()
                return
            }
            lokasiKebun.isEmpty() -> {
                Toast.makeText(requireContext(), "Silakan pilih lokasi kebun", Toast.LENGTH_SHORT).show()
                return
            }
            jenisBibit.isEmpty() -> {
                Toast.makeText(requireContext(), "Silakan masukkan jenis bibit", Toast.LENGTH_SHORT).show()
                return
            }
            tahunTanam.isEmpty() -> {
                Toast.makeText(requireContext(), "Silakan pilih tahun tanam", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Validasi angka
        val luasLahanValue = luasLahan.toDoubleOrNull()
        if (luasLahanValue == null) {
            Toast.makeText(requireContext(), "Luas lahan harus berupa angka yang valid", Toast.LENGTH_SHORT).show()
            return
        }

        val jumlahTanamanValue = if (jumlahTanaman.isNotEmpty()) {
            jumlahTanaman.toIntOrNull() ?: 0
        } else {
            0
        }

        // Buat object KebunData
        val kebunData = KebunData(
            namaKebun = namaKebun,
            luasLahan = luasLahanValue,
            lokasiKebun = lokasiKebun,
            jenisBibit = jenisBibit,
            jumlahTanaman = jumlahTanamanValue,
            tahunTanam = tahunTanam,
            jenisTanah = selectedJenisTanah
        )

        // Simpan ke KebunManager
        val isSaved = kebunManager.saveKebun(kebunData)

        if (isSaved) {
            Toast.makeText(requireContext(), "Data kebun berhasil disimpan!", Toast.LENGTH_SHORT).show()

            // Pindah ke LihatKebunFragment
            navigateToLihatKebun()
        } else {
            Toast.makeText(requireContext(), "Gagal menyimpan data kebun", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLihatKebun() {
        val lihatKebunFragment = LihatKebunFragment()

        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.fragmentContainer, lihatKebunFragment)
            addToBackStack(null)
            commit()
        }
    }
}