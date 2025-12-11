package com.example.sawit.IsiDashboard

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R
import com.example.sawit.utils.KebunManager

class LihatKebunFragment : Fragment() {
    private lateinit var btnBack: ImageView
    private lateinit var rvKebun: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var tvEmptyState: TextView
    private lateinit var kebunManager: KebunManager
    private lateinit var kebunAdapter: KebunAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_lihat_kebun, container, false)

        // Initialize KebunManager
        kebunManager = KebunManager.getInstance(requireContext())

        initViews(rootView)
        setupRecyclerView()
        loadKebunData()

        return rootView
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        rvKebun = view.findViewById(R.id.rvKebun)
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        btnBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        kebunAdapter = KebunAdapter(
            onItemClick = { kebunData ->
                // TODO: Navigate ke detail kebun
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

    private fun loadKebunData() {
        val kebunList = kebunManager.getAllKebun()

        if (kebunList.isEmpty()) {
            rvKebun.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            rvKebun.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
            kebunAdapter.submitList(kebunList)
        }
    }

    private fun showKebunDetail(kebunData: com.example.sawit.model.KebunData) {
        val message = """
            📍 Lokasi: ${kebunData.lokasiKebun}
            📏 Luas Lahan: ${String.format("%.2f", kebunData.luasLahan)} Ha
            🌱 Jenis Bibit: ${kebunData.jenisBibit}
            🌳 Jumlah Tanaman: ${if (kebunData.jumlahTanaman > 0) "${kebunData.jumlahTanaman} pohon" else "Tidak diisi"}
            📅 Tahun Tanam: ${kebunData.tahunTanam}
            🏞️ Jenis Tanah: ${kebunData.jenisTanah}
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
        val isDeleted = kebunManager.deleteKebun(id)
        if (isDeleted) {
            Toast.makeText(
                requireContext(),
                "Kebun berhasil dihapus",
                Toast.LENGTH_SHORT
            ).show()
            loadKebunData()
        } else {
            Toast.makeText(
                requireContext(),
                "Gagal menghapus kebun",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data ketika fragment kembali ditampilkan
        loadKebunData()
    }
}