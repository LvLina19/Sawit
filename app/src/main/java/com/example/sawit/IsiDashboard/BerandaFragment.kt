package com.example.sawit.IsiDashboard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.example.sawit.R
class BerandaFragment : Fragment() {
    private lateinit var cardDeteksi: CardView
    private lateinit var cardCatatPanen: CardView
    private lateinit var cardEdukasi: CardView
    private lateinit var cardVisualisasi: CardView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_beranda, container, false)

        // Initialize views
        initViews(view)

        // Setup click listeners
        setupClickListeners()

        return view
    }

    private fun initViews(view: View) {
        cardDeteksi = view.findViewById(R.id.cardDeteksi)
        cardCatatPanen = view.findViewById(R.id.cardCatatPanen)
        cardEdukasi = view.findViewById(R.id.cardEdukasi)
        cardVisualisasi = view.findViewById(R.id.cardVisualisasi)
    }

    private fun setupClickListeners() {
        cardDeteksi.setOnClickListener {
            Toast.makeText(context, "Deteksi Kematangan Sawit", Toast.LENGTH_SHORT).show()
            // Navigate to DeteksiFragment
            navigateToFragment(DeteksiFragment())
        }

        cardCatatPanen.setOnClickListener {
            Toast.makeText(context, "Catat Hasil Panen", Toast.LENGTH_SHORT).show()
            // Navigate to LaporanFragment
            navigateToFragment(LaporanFragment())
        }

        cardEdukasi.setOnClickListener {
            Toast.makeText(context, "Edukasi Tentang Sawit", Toast.LENGTH_SHORT).show()
        }

        cardVisualisasi.setOnClickListener {
            Toast.makeText(context, "Visualisasi Grafik", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragmentContainer, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }
}