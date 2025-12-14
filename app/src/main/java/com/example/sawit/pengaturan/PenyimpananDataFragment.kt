package com.example.sawit.pengaturan
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.sawit.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class PenyimpananDataFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private val userId by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    private lateinit var btnBack: ImageView
    private lateinit var tvCacheSize: TextView
    private lateinit var tvTotalSize: TextView
    private lateinit var btnClearCache: Button
    private lateinit var btnClearAll: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_penyimpanan_data, container, false)

        firestore = FirebaseFirestore.getInstance()

        initViews(view)
        setupClickListeners()
        loadStorageInfo()

        return view
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        tvCacheSize = view.findViewById(R.id.tvCacheSize)
        tvTotalSize = view.findViewById(R.id.tvTotalSize)
        btnClearCache = view.findViewById(R.id.btnClearCache)
        btnClearAll = view.findViewById(R.id.btnClearAll)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnClearCache.setOnClickListener {
            clearCache()
        }

        btnClearAll.setOnClickListener {
            showClearAllDialog()
        }
    }

    private fun loadStorageInfo() {
        val cacheSize = requireContext().cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
        tvCacheSize.text = formatFileSize(cacheSize)
        tvTotalSize.text = formatFileSize(cacheSize)
    }

    private fun clearCache() {
        requireContext().cacheDir.deleteRecursively()
        Toast.makeText(requireContext(), "Cache berhasil dihapus", Toast.LENGTH_SHORT).show()
        loadStorageInfo()
    }

    private fun showClearAllDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Semua Data")
            .setMessage("Apakah Anda yakin ingin menghapus semua data lokal?")
            .setPositiveButton("Ya") { _, _ ->
                clearCache()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun formatFileSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$size B"
        }
    }
}
