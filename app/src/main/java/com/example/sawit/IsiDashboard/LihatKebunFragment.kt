package com.example.sawit.IsiDashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.R
import com.example.sawit.utils.KebunManager

class LihatKebunFragment : Fragment() {
    private lateinit var btnBack: ImageView
    private lateinit var rvKebun: RecyclerView
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
        tvEmptyState = view.findViewById(R.id.tvEmptyState)

        btnBack.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    private fun setupRecyclerView() {
        kebunAdapter = KebunAdapter(
            onItemClick = { kebunData ->
                Toast.makeText(requireContext(), "Kebun: ${kebunData.namaKebun}", Toast.LENGTH_SHORT).show()
            },
            onDeleteClick = { kebunData ->
                deleteKebun(kebunData.id)
            }
        )

        rvKebun.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = kebunAdapter
        }
    }

    private fun loadKebunData() {
        val kebunList = kebunManager.getAllKebun()

        if (kebunList.isEmpty()) {
            rvKebun.visibility = View.GONE
            tvEmptyState.visibility = View.VISIBLE
        } else {
            rvKebun.visibility = View.VISIBLE
            tvEmptyState.visibility = View.GONE
            kebunAdapter.submitList(kebunList)
        }
    }

    private fun deleteKebun(id: Int) {
        val isDeleted = kebunManager.deleteKebun(id)
        if (isDeleted) {
            Toast.makeText(requireContext(), "Kebun berhasil dihapus", Toast.LENGTH_SHORT).show()
            loadKebunData()
        } else {
            Toast.makeText(requireContext(), "Gagal menghapus kebun", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data ketika fragment kembali ditampilkan
        loadKebunData()
    }
}