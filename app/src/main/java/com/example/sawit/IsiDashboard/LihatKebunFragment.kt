package com.example.sawit.IsiDashboard

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R
import com.example.sawit.utils.KebunManager
import com.google.firebase.database.ValueEventListener

class LihatKebunFragment : Fragment() {
    private lateinit var btnBack: ImageView
    private lateinit var rvKebun: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var tvEmptyState: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var kebunManager: KebunManager
    private lateinit var kebunAdapter: KebunAdapter
    private var kebunListener: ValueEventListener? = null
    private var isFirstLoad = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_lihat_kebun, container, false)

        kebunManager = KebunManager.getInstance(requireContext())

        initViews(rootView)
        setupRecyclerView()
        setupRealtimeListener() // Hanya pakai real-time listener saja

        return rootView
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        rvKebun = view.findViewById(R.id.rvKebun)
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        // Tambahkan ProgressBar di layout atau buat programmatically
        progressBar = ProgressBar(requireContext()).apply {
            visibility = View.VISIBLE
        }

        btnBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        kebunAdapter = KebunAdapter(
            onItemClick = { kebunData ->
                showKebunDetail(kebunData)
            },
            onDeleteClick = { kebunData ->
                showDeleteConfirmation(kebunData)
            }
        )

        rvKebun.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = kebunAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupRealtimeListener() {
        // Show loading only on first load
        if (isFirstLoad) {
            showLoading(true)
        }

        // Listen untuk perubahan real-time dari Firebase
        kebunListener = kebunManager.listenToKebunChanges { kebunList ->
            // Hide loading setelah data pertama datang
            if (isFirstLoad) {
                showLoading(false)
                isFirstLoad = false
            }

            updateUI(kebunList)
        }
    }

    private fun updateUI(kebunList: List<com.example.sawit.model.KebunData>) {
        if (kebunList.isEmpty()) {
            rvKebun.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            rvKebun.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
            kebunAdapter.submitList(kebunList)
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            rvKebun.visibility = View.GONE
            layoutEmptyState.visibility = View.GONE
            // Jika ada ProgressBar di layout, show it
            // progressBar.visibility = View.VISIBLE
        } else {
            // progressBar.visibility = View.GONE
        }
    }

    private fun showKebunDetail(kebunData: com.example.sawit.model.KebunData) {
        val message = """
            📍 Lokasi: ${kebunData.lokasiKebun}
            📏 Luas Lahan: ${kebunData.getFormattedLuas()}
            🌱 Jenis Bibit: ${kebunData.jenisBibit}
            🌳 Jumlah Tanaman: ${kebunData.getFormattedJumlahTanaman()}
            📅 Tahun Tanam: ${kebunData.tahunTanam}
            ⏳ Usia Kebun: ${kebunData.getFormattedUsia()}
            🏞️ Jenis Tanah: ${kebunData.jenisTanah}
            🌾 Tanaman/Ha: ${if (kebunData.getTanamanPerHektar() > 0) "${kebunData.getTanamanPerHektar()} pohon/ha" else "-"}
        """.trimIndent()

        AlertDialog.Builder(requireContext())
            .setTitle("ℹ️ ${kebunData.namaKebun}")
            .setMessage(message)
            .setPositiveButton("Tutup", null)
            .show()
    }

    private fun showDeleteConfirmation(kebunData: com.example.sawit.model.KebunData) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Kebun")
            .setMessage("Apakah Anda yakin ingin menghapus kebun '${kebunData.namaKebun}'?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteKebun(kebunData.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteKebun(id: Int) {
        kebunManager.deleteKebun(id) { success, errorMessage ->
            if (success) {
                showToast("Kebun berhasil dihapus")
                // Data akan otomatis ter-update melalui realtime listener
            } else {
                showToast("Gagal menghapus kebun: ${errorMessage ?: "Unknown error"}")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove listener untuk menghindari memory leak
        kebunListener?.let { kebunManager.removeListener(it) }
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
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TextView
//import android.widget.Toast
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.sawit.R
//import com.example.sawit.utils.KebunManager
//
//class LihatKebunFragment : Fragment() {
//    private lateinit var btnBack: ImageView
//    private lateinit var rvKebun: RecyclerView
//    private lateinit var layoutEmptyState: LinearLayout
//    private lateinit var tvEmptyState: TextView
//    private lateinit var kebunManager: KebunManager
//    private lateinit var kebunAdapter: KebunAdapter
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val rootView = inflater.inflate(R.layout.fragment_lihat_kebun, container, false)
//
//        // Initialize KebunManager
//        kebunManager = KebunManager.getInstance(requireContext())
//
//        initViews(rootView)
//        setupRecyclerView()
//        loadKebunData()
//
//        return rootView
//    }
//
//    private fun initViews(view: View) {
//        btnBack = view.findViewById(R.id.btnBack)
//        rvKebun = view.findViewById(R.id.rvKebun)
//        layoutEmptyState = view.findViewById(R.id.layoutEmptyState)
//        tvEmptyState = view.findViewById(R.id.tvEmptyState)
//
//        btnBack.setOnClickListener {
//            activity?.supportFragmentManager?.popBackStack()
//        }
//    }
//
//    private fun setupRecyclerView() {
//        kebunAdapter = KebunAdapter(
//            onItemClick = { kebunData ->
//                // TODO: Navigate ke detail kebun
//                showKebunDetail(kebunData)
//            },
//            onDeleteClick = { kebunData ->
//                showDeleteConfirmation(kebunData)
//            }
//        )
//
//        rvKebun.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = kebunAdapter
//            setHasFixedSize(true)
//        }
//    }
//
//    private fun loadKebunData() {
//        val kebunList = kebunManager.getAllKebun()
//
//        if (kebunList.isEmpty()) {
//            rvKebun.visibility = View.GONE
//            layoutEmptyState.visibility = View.VISIBLE
//        } else {
//            rvKebun.visibility = View.VISIBLE
//            layoutEmptyState.visibility = View.GONE
//            kebunAdapter.submitList(kebunList)
//        }
//    }
//
//    private fun showKebunDetail(kebunData: com.example.sawit.model.KebunData) {
//        val message = """
//            📍 Lokasi: ${kebunData.lokasiKebun}
//            📏 Luas Lahan: ${String.format("%.2f", kebunData.luasLahan)} Ha
//            🌱 Jenis Bibit: ${kebunData.jenisBibit}
//            🌳 Jumlah Tanaman: ${if (kebunData.jumlahTanaman > 0) "${kebunData.jumlahTanaman} pohon" else "Tidak diisi"}
//            📅 Tahun Tanam: ${kebunData.tahunTanam}
//            🏞️ Jenis Tanah: ${kebunData.jenisTanah}
//        """.trimIndent()
//
//        AlertDialog.Builder(requireContext())
//            .setTitle("ℹ️ ${kebunData.namaKebun}")
//            .setMessage(message)
//            .setPositiveButton("Tutup", null)
//            .show()
//    }
//
//    private fun showDeleteConfirmation(kebunData: com.example.sawit.model.KebunData) {
//        AlertDialog.Builder(requireContext())
//            .setTitle("Hapus Kebun")
//            .setMessage("Apakah Anda yakin ingin menghapus kebun '${kebunData.namaKebun}'?")
//            .setPositiveButton("Hapus") { _, _ ->
//                deleteKebun(kebunData.id)
//            }
//            .setNegativeButton("Batal", null)
//            .show()
//    }
//
//    private fun deleteKebun(id: Int) {
//        val isDeleted = kebunManager.deleteKebun(id)
//        if (isDeleted) {
//            Toast.makeText(
//                requireContext(),
//                "Kebun berhasil dihapus",
//                Toast.LENGTH_SHORT
//            ).show()
//            loadKebunData()
//        } else {
//            Toast.makeText(
//                requireContext(),
//                "Gagal menghapus kebun",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }
//
//    override fun onResume() {
//        super.onResume()
//        // Refresh data ketika fragment kembali ditampilkan
//        loadKebunData()
//    }
//}