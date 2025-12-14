package com.example.sawit.IsiDashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sawit.R
import com.example.sawit.databinding.FragmentRiwayatDeteksiBinding
import com.example.sawit.databinding.ItemRiwayatBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RiwayatDeteksiFragment : Fragment() {
    private var _binding: FragmentRiwayatDeteksiBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: RiwayatAdapter
    private var allData = listOf<RiwayatDeteksi>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiwayatDeteksiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchView()
        loadData()
    }

    private fun setupRecyclerView() {
        adapter = RiwayatAdapter(
            onDeleteClick = { riwayat ->
                deleteRiwayat(riwayat)
            }
        )

        binding.rvRiwayat.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@RiwayatDeteksiFragment.adapter
        }
    }

    private fun setupSearchView() {
        binding.etSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterData(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterData(newText)
                return true
            }
        })
    }

    private fun loadData() {
        // Contoh data dummy
        allData = listOf(
            RiwayatDeteksi(
                id = 1,
                imageUrl = "",
                jenisBuah = "Buah Matang",
                lokasi = "Riau",
                tanggal = "25/11/2024",
                kepercayaan = 94,
                area = 2.5
            ),
            RiwayatDeteksi(
                id = 2,
                imageUrl = "",
                jenisBuah = "Buah Matang",
                lokasi = "Riau",
                tanggal = "25/11/2024",
                kepercayaan = 94,
                area = 2.5
            ),
            RiwayatDeteksi(
                id = 3,
                imageUrl = "",
                jenisBuah = "Buah Matang",
                lokasi = "Riau",
                tanggal = "25/11/2024",
                kepercayaan = 94,
                area = 2.5
            ),
            RiwayatDeteksi(
                id = 4,
                imageUrl = "",
                jenisBuah = "Buah Matang",
                lokasi = "Riau",
                tanggal = "25/11/2024",
                kepercayaan = 94,
                area = 2.5
            )
        )

        adapter.submitList(allData)
        updateEmptyState()
    }

    private fun filterData(query: String?) {
        val filteredList = if (query.isNullOrEmpty()) {
            allData
        } else {
            allData.filter { riwayat ->
                riwayat.jenisBuah.contains(query, ignoreCase = true) ||
                        riwayat.lokasi.contains(query, ignoreCase = true) ||
                        riwayat.tanggal.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(filteredList)
        updateEmptyState()
    }

    private fun deleteRiwayat(riwayat: RiwayatDeteksi) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Riwayat")
            .setMessage("Apakah Anda yakin ingin menghapus riwayat ini?")
            .setPositiveButton("Hapus") { _, _ ->
                allData = allData.filter { it.id != riwayat.id }
                adapter.submitList(allData)
                updateEmptyState()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateEmptyState() {
        if (allData.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            binding.rvRiwayat.visibility = View.GONE
        } else {
            binding.layoutEmpty.visibility = View.GONE
            binding.rvRiwayat.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Data Class
data class RiwayatDeteksi(
    val id: Int,
    val imageUrl: String,
    val jenisBuah: String,
    val lokasi: String,
    val tanggal: String,
    val kepercayaan: Int,
    val area: Double
)

// Adapter
class RiwayatAdapter(
    private val onDeleteClick: (RiwayatDeteksi) -> Unit
) : ListAdapter<RiwayatDeteksi, RiwayatAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemRiwayatBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(riwayat: RiwayatDeteksi) {
            binding.apply {
                // Load image dengan Glide
                Glide.with(itemView.context)
                    .load(riwayat.imageUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .into(ivRiwayat)

                tvJenisBuah.text = riwayat.jenisBuah
                tvLokasi.text = riwayat.lokasi
                tvTanggal.text = riwayat.tanggal
                tvKepercayaan.text = "Kepercayaan: ${riwayat.kepercayaan}%"
                tvArea.text = "Area: ${riwayat.area} ha"

                btnDelete.setOnClickListener {
                    onDeleteClick(riwayat)
                }

                root.setOnClickListener {
                    // Navigate to detail
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RiwayatDeteksi>() {
            override fun areItemsTheSame(
                oldItem: RiwayatDeteksi,
                newItem: RiwayatDeteksi
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: RiwayatDeteksi,
                newItem: RiwayatDeteksi
            ): Boolean = oldItem == newItem
        }
    }
}