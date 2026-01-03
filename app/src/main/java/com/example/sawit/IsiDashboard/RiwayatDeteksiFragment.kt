package com.example.sawit.IsiDashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R
import com.example.sawit.adapter.RiwayatDeteksiAdapter
import com.example.sawit.model.RiwayatDeteksiRepository
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class RiwayatDeteksiFragment : Fragment() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var searchView: SearchView
    private lateinit var rvRiwayat: RecyclerView
    private lateinit var layoutEmpty: LinearLayout

    private lateinit var adapter: RiwayatDeteksiAdapter
    private lateinit var repository: RiwayatDeteksiRepository

    companion object {
        private const val TAG = "RiwayatDeteksiFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_riwayat_deteksi, container, false)

        initViews(view)
        setupRecyclerView()
        setupListeners()

        repository = RiwayatDeteksiRepository(requireContext())

        loadRiwayat()

        return view
    }

    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        searchView = view.findViewById(R.id.etSearch)
        rvRiwayat = view.findViewById(R.id.rvRiwayat)
        layoutEmpty = view.findViewById(R.id.layoutEmpty)
    }

    private fun setupRecyclerView() {
        adapter = RiwayatDeteksiAdapter { riwayat ->
            showDeleteConfirmation(riwayat.id ?: "")
        }

        rvRiwayat.layoutManager = LinearLayoutManager(requireContext())
        rvRiwayat.adapter = adapter
    }

    private fun setupListeners() {
        toolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchRiwayat(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    loadRiwayat()
                } else {
                    searchRiwayat(newText)
                }
                return true
            }
        })
    }

    private fun loadRiwayat() {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Loading riwayat...")

                val result = repository.getAllRiwayat()

                result.fold(
                    onSuccess = { list ->
                        Log.d(TAG, "Berhasil load ${list.size} riwayat")

                        if (list.isEmpty()) {
                            showEmptyState()
                        } else {
                            showRecyclerView()
                            adapter.submitList(list)
                        }
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Gagal load riwayat", error)
                        Toast.makeText(
                            requireContext(),
                            "Gagal memuat data: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        showEmptyState()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception saat load riwayat", e)
                Toast.makeText(
                    requireContext(),
                    "Error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun searchRiwayat(query: String) {
        lifecycleScope.launch {
            try {
                val result = repository.searchRiwayat(query)

                result.fold(
                    onSuccess = { list ->
                        if (list.isEmpty()) {
                            showEmptyState()
                        } else {
                            showRecyclerView()
                            adapter.submitList(list)
                        }
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            requireContext(),
                            "Gagal mencari: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception saat search", e)
            }
        }
    }

    private fun showDeleteConfirmation(riwayatId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Riwayat")
            .setMessage("Apakah Anda yakin ingin menghapus riwayat ini?")
            .setPositiveButton("Hapus") { _, _ ->
                deleteRiwayat(riwayatId)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun deleteRiwayat(riwayatId: String) {
        lifecycleScope.launch {
            try {
                val result = repository.deleteRiwayat(riwayatId)

                result.fold(
                    onSuccess = {
                        Toast.makeText(
                            requireContext(),
                            "Riwayat berhasil dihapus",
                            Toast.LENGTH_SHORT
                        ).show()
                        loadRiwayat()
                    },
                    onFailure = { error ->
                        Toast.makeText(
                            requireContext(),
                            "Gagal menghapus: ${error.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception saat delete", e)
            }
        }
    }

    private fun showEmptyState() {
        rvRiwayat.visibility = View.GONE
        layoutEmpty.visibility = View.VISIBLE
    }

    private fun showRecyclerView() {
        rvRiwayat.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE
    }
}