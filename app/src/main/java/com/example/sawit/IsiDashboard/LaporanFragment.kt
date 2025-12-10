package com.example.sawit.IsiDashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.example.sawit.R
class LaporanFragment : Fragment() {
    private lateinit var btnTambahKebun: CardView
    private lateinit var btnCatatPanen: CardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout fragment
        val rootView = inflater.inflate(R.layout.fragment_laporan, container, false)

        // Initialize views
        initViews(rootView)

        // Setup click listeners
        setupClickListeners()

        return rootView
    }

    private fun initViews(view: View) {
        btnTambahKebun = view.findViewById(R.id.btnTambahKebun)
        btnCatatPanen = view.findViewById(R.id.btnCatatPanen)
    }

    private fun setupClickListeners() {
        // Tombol Tambahkan Kebun
        btnTambahKebun.setOnClickListener {
            // Navigasi ke TambahKebunFragment
            navigateToFragment(TambahKebunFragment())
        }

        // Tombol Catat Panen - mengarah ke CatatPanenFragment
        btnCatatPanen.setOnClickListener {
            // Navigasi ke CatatPanenFragment
            navigateToFragment(CatatPanenFragment())
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.fragmentContainer, fragment) // Ganti dengan ID container Anda
            addToBackStack(null) // Tambahkan ke back stack agar bisa kembali
            commit()
        }
    }
}