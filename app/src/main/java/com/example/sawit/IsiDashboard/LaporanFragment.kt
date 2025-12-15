package com.example.sawit.IsiDashboard

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.example.sawit.R
import com.example.sawit.model.KebunData
import com.example.sawit.model.PanenData
import com.example.sawit.utils.KebunManager
import com.example.sawit.utils.PanenManager

class LaporanFragment : Fragment() {

    private lateinit var btnTambahKebun: CardView
    private lateinit var btnCatatPanen: CardView
    private lateinit var btnLihatPanen: CardView
    private lateinit var kebunManager: KebunManager
    private lateinit var panenManager: PanenManager

    companion object {
        private const val TAG = "LaporanFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Managers
        kebunManager = KebunManager.getInstance(requireContext())
        panenManager = PanenManager.getInstance(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_laporan, container, false)

        initViews(rootView)
        setupClickListeners()

        return rootView
    }

    private fun initViews(view: View) {
        btnTambahKebun = view.findViewById(R.id.btnTambahKebun)
        btnCatatPanen = view.findViewById(R.id.btnCatatPanen)
        btnLihatPanen = view.findViewById(R.id.btnLihatPanen)
    }

    private fun setupClickListeners() {
        // Tombol Tambahkan Kebun
        btnTambahKebun.setOnClickListener {
            Log.d(TAG, "Tambah Kebun clicked")
            navigateToFragment(TambahKebunFragment())
        }

        // Tombol Catat Panen - HARUS PILIH KEBUN DULU
        btnCatatPanen.setOnClickListener {
            Log.d(TAG, "Catat Panen clicked")
            showKebunSelectionDialog()
        }

        // Tombol Lihat Panen - TAMPILKAN LIST KEBUN
        btnLihatPanen.setOnClickListener {
            Log.d(TAG, "Lihat Panen clicked")
            showKebunSelectionForLaporan()
        }
    }

    /**
     * Tampilkan dialog untuk memilih kebun sebelum catat panen
     */
    private fun showKebunSelectionDialog() {
        Log.d(TAG, "Loading kebun list from Firebase...")

        // Disable button sementara
        btnCatatPanen.isEnabled = false

        // Load semua kebun dari Firebase
        kebunManager.getAllKebun { kebunList ->
            activity?.runOnUiThread {
                // Enable button kembali
                btnCatatPanen.isEnabled = true

                // Validasi: Cek apakah ada kebun
                if (kebunList.isNullOrEmpty()) {
                    Log.w(TAG, "No kebun found in Firebase")
                    showNoKebunDialog()
                    return@runOnUiThread
                }

                Log.d(TAG, "Found ${kebunList.size} kebun")

                // Log semua kebun untuk debugging
                kebunList.forEachIndexed { index, kebun ->
                    Log.d(TAG, "Kebun[$index]: ID=${kebun.id}, Nama=${kebun.namaKebun}")
                    if (kebun.id == 0) {
                        Log.e(TAG, "!!! WARNING: Kebun '${kebun.namaKebun}' has ID = 0")
                    }
                }

                // Filter kebun yang valid (ID tidak 0)
                val validKebunList = kebunList.filter { it.id != 0 }

                if (validKebunList.isEmpty()) {
                    Log.e(TAG, "All kebun have invalid ID (0)")
                    Toast.makeText(
                        context,
                        "Error: Data kebun tidak valid. Silakan tambah kebun baru.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@runOnUiThread
                }

                // Jika hanya ada 1 kebun, langsung navigate
                if (validKebunList.size == 1) {
                    val kebun = validKebunList[0]
                    Log.d(TAG, "Only 1 kebun found, auto-selecting: ${kebun.namaKebun}")
                    navigateToCatatPanen(kebun)
                    return@runOnUiThread
                }

                // Jika lebih dari 1, tampilkan dialog pilihan
                showKebunListDialog(validKebunList)
            }
        }
    }

    /**
     * Tampilkan dialog list kebun untuk melihat laporan
     */
    private fun showKebunSelectionForLaporan() {
        Log.d(TAG, "Loading kebun list for laporan...")

        // Disable button sementara
        btnLihatPanen.isEnabled = false

        // Load semua kebun dari Firebase
        kebunManager.getAllKebun { kebunList ->
            activity?.runOnUiThread {
                // Enable button kembali
                btnLihatPanen.isEnabled = true

                // Validasi: Cek apakah ada kebun
                if (kebunList.isNullOrEmpty()) {
                    Log.w(TAG, "No kebun found in Firebase")
                    showNoKebunDialog()
                    return@runOnUiThread
                }

                Log.d(TAG, "Found ${kebunList.size} kebun for laporan")

                // Filter kebun yang valid (ID tidak 0)
                val validKebunList = kebunList.filter { it.id != 0 }

                if (validKebunList.isEmpty()) {
                    Log.e(TAG, "All kebun have invalid ID (0)")
                    Toast.makeText(
                        context,
                        "Error: Data kebun tidak valid. Silakan tambah kebun baru.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@runOnUiThread
                }

                // Jika hanya ada 1 kebun, langsung load panennya
                if (validKebunList.size == 1) {
                    val kebun = validKebunList[0]
                    Log.d(TAG, "Only 1 kebun found, auto-selecting: ${kebun.namaKebun}")
                    loadPanenAndNavigateToLaporan(kebun)
                    return@runOnUiThread
                }

                // Jika lebih dari 1, tampilkan dialog pilihan
                showKebunListDialogForLaporan(validKebunList)
            }
        }
    }

    /**
     * Tampilkan dialog list kebun untuk dipilih (untuk catat panen)
     */
    private fun showKebunListDialog(kebunList: List<KebunData>) {
        val kebunNames = kebunList.map {
            "${it.namaKebun}\n${it.lokasiKebun}"
        }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Kebun untuk Catat Panen")
            .setItems(kebunNames) { dialog, which ->
                val selectedKebun = kebunList[which]
                Log.d(TAG, "User selected: ${selectedKebun.namaKebun} (ID: ${selectedKebun.id})")
                navigateToCatatPanen(selectedKebun)
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Tampilkan dialog list kebun untuk melihat laporan
     */
    private fun showKebunListDialogForLaporan(kebunList: List<KebunData>) {
        val kebunNames = kebunList.map {
            "${it.namaKebun}\n${it.lokasiKebun}"
        }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Kebun untuk Melihat Laporan")
            .setItems(kebunNames) { dialog, which ->
                val selectedKebun = kebunList[which]
                Log.d(TAG, "User selected kebun for laporan: ${selectedKebun.namaKebun} (ID: ${selectedKebun.id})")
                loadPanenAndNavigateToLaporan(selectedKebun)
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Load data panen untuk kebun yang dipilih dan navigate ke detail laporan
     */
    private fun loadPanenAndNavigateToLaporan(kebun: KebunData) {
        Log.d(TAG, "Loading panen data for kebun: ${kebun.namaKebun} (ID: ${kebun.id})")

        // Show loading
        Toast.makeText(context, "Memuat data panen...", Toast.LENGTH_SHORT).show()

        // Load panen data untuk kebun ini
        panenManager.getPanenByKebunId(kebun.id) { panenList ->
            activity?.runOnUiThread {
                if (panenList.isNullOrEmpty()) {
                    Log.w(TAG, "No panen data found for kebun: ${kebun.namaKebun}")
                    Toast.makeText(
                        context,
                        "Belum ada data panen untuk kebun ini",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@runOnUiThread
                }

                Log.d(TAG, "Found ${panenList.size} panen records for kebun: ${kebun.namaKebun}")
                navigateToDetailLaporan(kebun, panenList)
            }
        }
    }

    /**
     * Navigate ke CatatPanenFragment dengan data kebun yang valid
     */
    private fun navigateToCatatPanen(kebun: KebunData) {
        Log.d(TAG, "=== NAVIGATING TO CATAT PANEN ===")
        Log.d(TAG, "Kebun ID: ${kebun.id}")
        Log.d(TAG, "Kebun Nama: ${kebun.namaKebun}")
        Log.d(TAG, "Kebun Lokasi: ${kebun.lokasiKebun}")

        // Validasi final sebelum navigate
        if (kebun.id == 0) {
            Log.e(TAG, "!!! CRITICAL: Trying to navigate with kebunId = 0")
            Toast.makeText(
                context,
                "Error: Data kebun tidak valid",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Buat fragment dengan data yang benar
        val fragment = CatatPanenFragment.newInstance(kebun.id, kebun)

        // Navigate
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.fragmentContainer, fragment)
            addToBackStack("CatatPanen")
            commit()
        }

        Log.d(TAG, "✓ Navigation to CatatPanen completed")
    }

    /**
     * Navigate ke DetailLaporanFragment dengan data kebun dan panen
     */
    private fun navigateToDetailLaporan(kebun: KebunData, panenList: List<PanenData>) {
        Log.d(TAG, "=== NAVIGATING TO DETAIL LAPORAN ===")
        Log.d(TAG, "Kebun ID: ${kebun.id}")
        Log.d(TAG, "Kebun Nama: ${kebun.namaKebun}")
        Log.d(TAG, "Total Panen: ${panenList.size}")

        // Validasi final
        if (kebun.id == 0) {
            Log.e(TAG, "!!! CRITICAL: Trying to navigate with kebunId = 0")
            Toast.makeText(
                context,
                "Error: Data kebun tidak valid",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Buat fragment dengan data kebun dan panen
        val fragment = DetailLaporanFragment.newInstance(kebun, panenList)

        // Navigate
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.fragmentContainer, fragment)
            addToBackStack("DetailLaporan")
            commit()
        }

        Log.d(TAG, "✓ Navigation to DetailLaporan completed")
    }

    /**
     * Dialog jika belum ada kebun sama sekali
     */
    private fun showNoKebunDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Belum Ada Kebun")
            .setMessage("Anda belum memiliki data kebun. Silakan tambahkan kebun terlebih dahulu sebelum mencatat panen.")
            .setPositiveButton("Tambah Kebun") { dialog, _ ->
                dialog.dismiss()
                navigateToFragment(TambahKebunFragment())
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    /**
     * Navigate ke fragment lain (untuk Tambah Kebun)
     */
    private fun navigateToFragment(fragment: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.fragmentContainer, fragment)
            addToBackStack(null)
            commit()
        }
    }
    private fun debugAllPanenInFirebase() {
        Log.d(TAG, "🔍 === DEBUG: CHECKING ALL PANEN IN FIREBASE ===")

        panenManager.getAllPanen { allPanen ->
            activity?.runOnUiThread {
                if (allPanen == null || allPanen.isEmpty()) {
                    Log.e(TAG, "❌ NO PANEN DATA IN FIREBASE AT ALL!")
                    return@runOnUiThread
                }

                Log.d(TAG, "📊 Total panen in Firebase: ${allPanen.size}")
                Log.d(TAG, "")

                // Group by kebun ID
                val groupedByKebun = allPanen.groupBy { it.kebunId }

                groupedByKebun.forEach { (kebunId, panenList) ->
                    Log.d(TAG, "═══════════════════════════════════════")
                    Log.d(TAG, "🏡 KEBUN ID: $kebunId")
                    Log.d(TAG, "   Total records: ${panenList.size}")
                    Log.d(TAG, "")

                    panenList.forEachIndexed { index, panen ->
                        Log.d(TAG, "   📅 Record [$index]:")
                        Log.d(TAG, "      Panen ID: ${panen.id}")
                        Log.d(TAG, "      Tanggal: ${panen.tanggalPanen}")
                        Log.d(TAG, "      Formatted: ${panen.getFormattedTanggalPendek()}")
                        Log.d(TAG, "      Total Berat: ${panen.getTotalBerat()} Kg")
                        Log.d(TAG, "      Timestamp: ${panen.timestamp}")
                        Log.d(TAG, "")
                    }
                }

                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "🔍 === END DEBUG ===")
            }
        }
    }

    // PANGGIL FUNGSI INI di onResume()
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "LaporanFragment resumed")

        // DEBUG: Uncomment line ini untuk cek semua data
        debugAllPanenInFirebase()
    }
}