package com.example.sawit.IsiDashboard

import android.os.Bundle
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

class TambahKebunFragment : Fragment() {

    private lateinit var btnBack: ImageView
    private lateinit var etNamaKebun: EditText
    private lateinit var etLuasLahan: EditText
    private lateinit var etLokasiKebun: EditText
    private lateinit var spinnerJenisBibit: Spinner
    private lateinit var etJumlahTanaman: EditText
    private lateinit var spinnerTahunTanam: Spinner
    private lateinit var btnMineral: CardView
    private lateinit var btnGambut: CardView
    private lateinit var btnMineralVolkanik: CardView
    private lateinit var btnMineralBerpasir: CardView
    private lateinit var btnSubmit: CardView

    private lateinit var kebunManager: KebunManager
    private var selectedJenisTanah: String = "Mineral"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_tambah_kebun, container, false)

        kebunManager = KebunManager.getInstance(requireContext())

        initViews(rootView)
        setupSpinners()
        setupListeners()
        updateJenisTanahUI()

        return rootView
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        etNamaKebun = view.findViewById(R.id.etNamaKebun)
        etLuasLahan = view.findViewById(R.id.etLuasLahan)
        etLokasiKebun = view.findViewById(R.id.etLokasiKebun)
        spinnerJenisBibit = view.findViewById(R.id.spinnerJenisBibit)
        etJumlahTanaman = view.findViewById(R.id.etJumlahTanaman)
        spinnerTahunTanam = view.findViewById(R.id.spinnerTahunTanam)
        btnMineral = view.findViewById(R.id.btnMineral)
        btnGambut = view.findViewById(R.id.btnGambut)
        btnMineralVolkanik = view.findViewById(R.id.btnMineralVolkanik)
        btnMineralBerpasir = view.findViewById(R.id.btnMineralBerpasir)
        btnSubmit = view.findViewById(R.id.btnSubmit)
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

    private fun setupListeners() {
        btnBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

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

        btnSubmit.setOnClickListener {
            submitKebun()
        }
    }

    private fun updateJenisTanahUI() {
        setButtonUnselected(btnMineral)
        setButtonUnselected(btnGambut)
        setButtonUnselected(btnMineralVolkanik)
        setButtonUnselected(btnMineralBerpasir)

        when (selectedJenisTanah) {
            "Mineral" -> setButtonSelected(btnMineral)
            "Gambut" -> setButtonSelected(btnGambut)
            "Mineral Volkanik" -> setButtonSelected(btnMineralVolkanik)
            "Mineral Berpasir" -> setButtonSelected(btnMineralBerpasir)
        }
    }

    private fun setButtonSelected(button: CardView) {
        button.setCardBackgroundColor(0xFF4CAF50.toInt())
        val textView = button.getChildAt(0) as? TextView
        textView?.apply {
            setTextColor(0xFFFFFFFF.toInt())
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
    }

    private fun setButtonUnselected(button: CardView) {
        button.setCardBackgroundColor(0xFFE8F5E9.toInt())
        val textView = button.getChildAt(0) as? TextView
        textView?.apply {
            setTextColor(0xFF4CAF50.toInt())
            setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }

    private fun submitKebun() {
        val namaKebun = etNamaKebun.text.toString().trim()
        val luasLahanStr = etLuasLahan.text.toString().trim()
        val lokasiKebun = etLokasiKebun.text.toString().trim()
        val jenisBibit = spinnerJenisBibit.selectedItem.toString()
        val jumlahTanamanStr = etJumlahTanaman.text.toString().trim()
        val tahunTanam = spinnerTahunTanam.selectedItem.toString()

        // Validasi
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

        if (jenisBibit == "Pilih Jenis Bibit") {
            showToast("Jenis bibit harus dipilih")
            return
        }

        if (tahunTanam == "Pilih Tahun Tanam") {
            showToast("Tahun tanam harus dipilih")
            return
        }

        val luasLahan = luasLahanStr.toDoubleOrNull() ?: 0.0
        val jumlahTanaman = if (jumlahTanamanStr.isNotEmpty()) {
            jumlahTanamanStr.toIntOrNull() ?: 0
        } else {
            0
        }

        val kebunData = KebunData(
            namaKebun = namaKebun,
            luasLahan = luasLahan,
            lokasiKebun = lokasiKebun,
            jenisBibit = jenisBibit,
            jumlahTanaman = jumlahTanaman,
            tahunTanam = tahunTanam,
            jenisTanah = selectedJenisTanah
        )

        // Save to Firebase
        kebunManager.saveKebun(kebunData) { success, errorMessage ->
            if (success) {
                showToast("✅ Kebun berhasil ditambahkan")
                clearForm()
                navigateToLihatKebun()
            } else {
                showToast("❌ Gagal: ${errorMessage ?: "Unknown error"}")
            }
        }
    }

    private fun clearForm() {
        etNamaKebun.text?.clear()
        etLuasLahan.text?.clear()
        etLokasiKebun.text?.clear()
        spinnerJenisBibit.setSelection(0)
        etJumlahTanaman.text?.clear()
        spinnerTahunTanam.setSelection(0)
        selectedJenisTanah = "Mineral"
        updateJenisTanahUI()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
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