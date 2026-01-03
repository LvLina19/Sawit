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
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.sawit.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class BerandaFragment : Fragment() {
    // Views dari XML
    private lateinit var tvLihatSemuaDokter: TextView
    private lateinit var btnLihatVideo: CardView
    private lateinit var viewPagerBanner: ViewPager2
    private lateinit var dotsIndicator: LinearLayout
    private lateinit var tvGreeting: TextView
    private lateinit var ivProfile: ImageView

    // Menu cards
    private lateinit var cardDeteksi: CardView
    private lateinit var cardCatatPanen: CardView
    private lateinit var cardEdukasi: CardView
    private lateinit var cardVisualisasi: CardView

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0

    // List gambar banner
    private val bannerImages = listOf(
        R.drawable.banner_sawit,
        R.drawable.gambar_lahan_sawit,
        R.drawable.article_quality,
        R.drawable.article_tbs
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_beranda, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize views
        initViews(view)

        // Setup banner ViewPager
        setupBanner()

        // Setup click listeners
        setupClickListeners()

        // Load user data (nama dan foto profil)
        loadUserProfile()

        return view
    }

    private fun initViews(view: View) {
        tvLihatSemuaDokter = view.findViewById(R.id.tvLihatSemuaDokter)
        btnLihatVideo = view.findViewById(R.id.btnLihatVideo)
        viewPagerBanner = view.findViewById(R.id.viewPagerBanner)
        dotsIndicator = view.findViewById(R.id.dotsIndicator)
        tvGreeting = view.findViewById(R.id.tvGreeting)
        ivProfile = view.findViewById(R.id.ivProfile)

        // Menu cards
        cardDeteksi = view.findViewById(R.id.cardDeteksi)
        cardCatatPanen = view.findViewById(R.id.cardCatatPanen)
        cardEdukasi = view.findViewById(R.id.cardEdukasi)
        cardVisualisasi = view.findViewById(R.id.cardVisualisasi)
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User sudah login, load data dari Firestore
            firestore.collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Load nama user
                        val userName = document.getString("name") ?: "Kawan Sawit"
                        tvGreeting.text = "Hi, $userName"

                        // Load foto profil
                        val profileImageUrl = document.getString("profileImage")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            // Load foto dari ImgBB menggunakan Glide
                            Glide.with(this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.profile) // Gambar default saat loading
                                .error(R.drawable.profile) // Gambar default jika error
                                .circleCrop() // Membuat gambar berbentuk bulat
                                .into(ivProfile)
                        } else {
                            // Jika tidak ada foto, pakai default
                            ivProfile.setImageResource(R.drawable.profile)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Gagal memuat profil: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Tetap tampilkan default
                    tvGreeting.text = "Hi, Kawan Sawit"
                    ivProfile.setImageResource(R.drawable.profile)
                }
        } else {
            // User belum login
            tvGreeting.text = "Hi, Kawan Sawit"
            ivProfile.setImageResource(R.drawable.profile)
        }
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
                currentPage = position
            }
        })

        // Auto-slide banner
        val autoSlideRunnable = object : Runnable {
            override fun run() {
                if (currentPage >= bannerImages.size - 1) {
                    currentPage = 0
                } else {
                    currentPage++
                }
                viewPagerBanner.setCurrentItem(currentPage, true)
                handler.postDelayed(this, 4000) // Slide setiap 4 detik
            }
        }
        handler.postDelayed(autoSlideRunnable, 4000)
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
        // Klik foto profil untuk ke halaman Edit Profil
        ivProfile.setOnClickListener {
            navigateToFragment(com.example.sawit.pengaturan.EditProfilFragment())
        }

        // Menu cards click listeners
        cardDeteksi.setOnClickListener {
            Toast.makeText(context, "Deteksi Kematangan Sawit", Toast.LENGTH_SHORT).show()
            navigateToFragment(DeteksiFragment())
        }

        cardCatatPanen.setOnClickListener {
            Toast.makeText(context, "Catat Hasil Panen", Toast.LENGTH_SHORT).show()
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

        // Lihat Semua Dokter
        tvLihatSemuaDokter.setOnClickListener {
            Toast.makeText(context, "Lihat Semua Dokter Sawit", Toast.LENGTH_SHORT).show()
        }

        // Button Lihat Video
        btnLihatVideo.setOnClickListener {
            Toast.makeText(context, "Lihat Video Edukasi", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.fragmentContainer, fragment)
            ?.addToBackStack(null)
            ?.commit()
    }

    override fun onResume() {
        super.onResume()
        // Reload profile setiap kali fragment di-resume (misal balik dari EditProfil)
        loadUserProfile()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null) // Stop auto-slide saat fragment destroyed
    }
}