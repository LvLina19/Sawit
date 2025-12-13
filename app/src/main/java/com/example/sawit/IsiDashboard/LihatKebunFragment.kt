package com.example.sawit.IsiDashboard

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
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
import com.example.sawit.model.KebunData
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

    companion object {
        private const val TAG = "LihatKebunFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_lihat_kebun, container, false)

        kebunManager = KebunManager.getInstance(requireContext())

        initViews(rootView)
        setupRecyclerView()
        setupRealtimeListener()

        return rootView
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        rvKebun = view.findViewById(R.id.rvKebun)
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState)
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

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
                Log.d(TAG, "Item clicked: ${kebunData.namaKebun}")
                showKebunDetail(kebunData)
            },
            onEditClick = { kebunData ->
                Log.d(TAG, "Edit clicked: ${kebunData.namaKebun}")
                navigateToEditKebun(kebunData)
            },
            onDeleteClick = { kebunData ->
                Log.d(TAG, "Delete clicked: ${kebunData.namaKebun}")
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
        if (isFirstLoad) {
            showLoading(true)
        }

        kebunListener = kebunManager.listenToKebunChanges { kebunList ->
            if (isFirstLoad) {
                showLoading(false)
                isFirstLoad = false
            }

            updateUI(kebunList)
        }
    }

    private fun updateUI(kebunList: List<KebunData>) {
        if (kebunList.isEmpty()) {
            rvKebun.visibility = View.GONE
            layoutEmptyState.visibility = View.VISIBLE
        } else {
            rvKebun.visibility = View.VISIBLE
            layoutEmptyState.visibility = View.GONE
            kebunAdapter.submitList(kebunList)
            Log.d(TAG, "Updated list with ${kebunList.size} items")
        }
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            rvKebun.visibility = View.GONE
            layoutEmptyState.visibility = View.GONE
        }
    }

    private fun navigateToEditKebun(kebunData: KebunData) {
        try {
            Log.d(TAG, "Navigating to EditKebunFragment with data: ${kebunData.namaKebun}")

            val editFragment = EditKebunFragment.newInstance(kebunData)

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, editFragment)
                .addToBackStack("EditKebun")
                .commit()

            Log.d(TAG, "Fragment transaction committed")
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to edit: ${e.message}", e)
            showToast("Gagal membuka halaman edit: ${e.message}")
        }
    }

    private fun showKebunDetail(kebunData: KebunData) {
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

    private fun showDeleteConfirmation(kebunData: KebunData) {
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
        kebunListener?.let { kebunManager.removeListener(it) }
    }
}