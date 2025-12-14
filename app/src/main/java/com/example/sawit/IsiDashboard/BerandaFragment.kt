package com.example.sawit.IsiDashboard

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.viewpager2.widget.ViewPager2
import com.example.sawit.R

class BerandaFragment : Fragment() {
    private lateinit var cardDeteksi: CardView
    private lateinit var cardCatatPanen: CardView
    private lateinit var cardEdukasi: CardView
    private lateinit var cardVisualisasi: CardView
    private lateinit var viewPagerBanner: ViewPager2
    private lateinit var dotsIndicator: LinearLayout

    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0

    // List gambar banner (ganti dengan gambar Anda)
    private val bannerImages = listOf(
        R.drawable.banner_sawit,
        R.drawable.gambar_lahan_sawit,     // Gambar banner kedua
        R.drawable.article_tbs,     // Gambar banner ketiga
        R.drawable.article_quality      // Tambahkan lebih banyak jika mau
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_beranda, container, false)

        // Initialize views
        initViews(view)

        // Setup banner ViewPager
        setupBanner()

        // Setup click listeners
        setupClickListeners()

        return view
    }

    private fun initViews(view: View) {
        cardDeteksi = view.findViewById(R.id.cardDeteksi)
        cardCatatPanen = view.findViewById(R.id.cardCatatPanen)
        cardEdukasi = view.findViewById(R.id.cardEdukasi)
        cardVisualisasi = view.findViewById(R.id.cardVisualisasi)
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner)
        dotsIndicator = view.findViewById(R.id.dotsIndicator)
    }

    private fun setupBanner() {
        // Setup adapter
        val bannerAdapter = BannerAdapter(bannerImages)
        viewPagerBanner.adapter = bannerAdapter

        // Setup dots indicator
        setupDotsIndicator()
        setCurrentIndicator(0)

        // ViewPager page change callback
        viewPagerBanner.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })

        // Auto-slide banner
        val autoSlideRunnable = object : Runnable {
            override fun run() {
                if (currentPage == bannerImages.size) {
                    currentPage = 0
                }
                viewPagerBanner.setCurrentItem(currentPage++, true)
                handler.postDelayed(this, 3000) // Slide setiap 3 detik
            }
        }
        handler.postDelayed(autoSlideRunnable, 3000)
    }

    private fun setupDotsIndicator() {
        val dots = arrayOfNulls<ImageView>(bannerImages.size)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in dots.indices) {
            dots[i] = ImageView(requireContext())
            dots[i]?.setImageDrawable(
                resources.getDrawable(R.drawable.dot_indicator_inactive, null)
            )
            dots[i]?.layoutParams = layoutParams
            dotsIndicator.addView(dots[i])
        }
    }

    private fun setCurrentIndicator(position: Int) {
        val childCount = dotsIndicator.childCount
        for (i in 0 until childCount) {
            val imageView = dotsIndicator.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageDrawable(
                    resources.getDrawable(R.drawable.dot_indicator, null)
                )
            } else {
                imageView.setImageDrawable(
                    resources.getDrawable(R.drawable.dot_indicator_inactive, null)
                )
            }
        }
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
            navigateToFragment(EdukasiFragment())
        }

        cardVisualisasi.setOnClickListener {
            Toast.makeText(context, "Visualisasi Grafik", Toast.LENGTH_SHORT).show()
            navigateToFragment(RiwayatDeteksiFragment())
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragmentContainer, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null) // Stop auto-slide saat fragment destroyed
    }
}


//package com.example.sawit.IsiDashboard
//
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.cardview.widget.CardView
//import com.example.sawit.R
//class BerandaFragment : Fragment() {
//    private lateinit var cardDeteksi: CardView
//    private lateinit var cardCatatPanen: CardView
//    private lateinit var cardEdukasi: CardView
//    private lateinit var cardVisualisasi: CardView
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_beranda, container, false)
//
//        // Initialize views
//        initViews(view)
//
//        // Setup click listeners
//        setupClickListeners()
//
//        return view
//    }
//
//    private fun initViews(view: View) {
//        cardDeteksi = view.findViewById(R.id.cardDeteksi)
//        cardCatatPanen = view.findViewById(R.id.cardCatatPanen)
//        cardEdukasi = view.findViewById(R.id.cardEdukasi)
//        cardVisualisasi = view.findViewById(R.id.cardVisualisasi)
//    }
//
//    private fun setupClickListeners() {
//        cardDeteksi.setOnClickListener {
//            Toast.makeText(context, "Deteksi Kematangan Sawit", Toast.LENGTH_SHORT).show()
//            // Navigate to DeteksiFragment
//            navigateToFragment(DeteksiFragment())
//        }
//
//        cardCatatPanen.setOnClickListener {
//            Toast.makeText(context, "Catat Hasil Panen", Toast.LENGTH_SHORT).show()
//            // Navigate to LaporanFragment
//            navigateToFragment(LaporanFragment())
//        }
//
//        cardEdukasi.setOnClickListener {
//            Toast.makeText(context, "Edukasi Tentang Sawit", Toast.LENGTH_SHORT).show()
//            navigateToFragment(EdukasiFragment())
//        }
//
//        cardVisualisasi.setOnClickListener {
//            Toast.makeText(context, "Visualisasi Grafik", Toast.LENGTH_SHORT).show()
//            navigateToFragment(RiwayatDeteksiFragment())
//        }
//    }
//
//    private fun navigateToFragment(fragment: Fragment) {
//        activity?.supportFragmentManager?.beginTransaction()
//            ?.replace(R.id.fragmentContainer, fragment)
//            ?.addToBackStack(null)
//            ?.commit()
//    }
//}