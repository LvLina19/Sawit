package com.example.sawit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.sawit.IsiDashboard.BerandaFragment
import com.example.sawit.IsiDashboard.DeteksiFragment
import com.example.sawit.IsiDashboard.LaporanFragment
import com.example.sawit.IsiDashboard.PengaturanFragment
import com.example.sawit.IsiDashboard.EdukasiFragment
import com.google.firebase.auth.FirebaseAuth
import org.opencv.android.OpenCVLoader  // TAMBAHKAN INI

class Dashboard : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var navBeranda: LinearLayout
    private lateinit var navLaporan: LinearLayout
    private lateinit var navDeteksi: FrameLayout
    private lateinit var navSearch: LinearLayout
    private lateinit var navPengaturan: LinearLayout

    private lateinit var iconBeranda: ImageView
    private lateinit var iconLaporan: ImageView
    private lateinit var iconSearch: ImageView
    private lateinit var iconPengaturan: ImageView

    private lateinit var labelBeranda: TextView
    private lateinit var labelLaporan: TextView
    private lateinit var labelSearch: TextView
    private lateinit var labelPengaturan: TextView

    companion object {
        private const val TAG = "Dashboard"

        // Static initialization block untuk load OpenCV
        init {
            if (!OpenCVLoader.initDebug()) {
                Log.e(TAG, "OpenCV initialization failed!")
            } else {
                Log.d(TAG, "OpenCV loaded successfully")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TAMBAHKAN: Inisialisasi OpenCV sekali lagi untuk memastikan
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "Gagal menginisialisasi OpenCV")
            Toast.makeText(this, "Gagal memuat OpenCV library", Toast.LENGTH_LONG).show()
        } else {
            Log.d(TAG, "OpenCV berhasil dimuat")
        }

        // Cek autentikasi di awal
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            redirectToLogin()
            return
        }

        setContentView(R.layout.activity_dashboard)

        // Initialize views
        initViews()

        // Load default fragment (Beranda)
        if (savedInstanceState == null) {
            loadFragment(BerandaFragment())
        }

        // Setup click listeners
        setupClickListeners()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun initViews() {
        navBeranda = findViewById(R.id.navBeranda)
        navLaporan = findViewById(R.id.navLaporan)
        navDeteksi = findViewById(R.id.navDeteksi)
        navSearch = findViewById(R.id.navSearch)
        navPengaturan = findViewById(R.id.navPengaturan)

        iconBeranda = findViewById(R.id.iconBeranda)
        iconLaporan = findViewById(R.id.iconLaporan)
        iconSearch = findViewById(R.id.iconSearch)
        iconPengaturan = findViewById(R.id.iconPengaturan)

        labelBeranda = findViewById(R.id.labelBeranda)
        labelLaporan = findViewById(R.id.labelLaporan)
        labelSearch = findViewById(R.id.Edukasi)
        labelPengaturan = findViewById(R.id.labelPengaturan)
    }

    private fun setupClickListeners() {
        navBeranda.setOnClickListener {
            loadFragment(BerandaFragment())
            updateSelectedMenu("beranda")
        }

        navLaporan.setOnClickListener {
            loadFragment(LaporanFragment())
            updateSelectedMenu("laporan")
        }

        navDeteksi.setOnClickListener {
            loadFragment(DeteksiFragment())
            updateSelectedMenu("deteksi")
        }

        navSearch.setOnClickListener {
            loadFragment(EdukasiFragment())
            updateSelectedMenu("search")
        }

        navPengaturan.setOnClickListener {
            loadFragment(PengaturanFragment())
            updateSelectedMenu("pengaturan")
        }
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser == null) {
            redirectToLogin()
        }
    }

    private fun updateSelectedMenu(selected: String) {
        val activeColor = ContextCompat.getColor(this, R.color.green_primary)
        val inactiveColor = ContextCompat.getColor(this, R.color.gray_inactive)

        // Reset all to inactive
        iconBeranda.setColorFilter(inactiveColor)
        iconLaporan.setColorFilter(inactiveColor)
        iconSearch.setColorFilter(inactiveColor)
        iconPengaturan.setColorFilter(inactiveColor)

        labelBeranda.setTextColor(inactiveColor)
        labelLaporan.setTextColor(inactiveColor)
        labelSearch.setTextColor(inactiveColor)
        labelPengaturan.setTextColor(inactiveColor)

        // Set active color for selected menu
        when (selected) {
            "beranda" -> {
                iconBeranda.setColorFilter(activeColor)
                labelBeranda.setTextColor(activeColor)
            }
            "laporan" -> {
                iconLaporan.setColorFilter(activeColor)
                labelLaporan.setTextColor(activeColor)
            }
            "deteksi" -> {
                // Deteksi button tetap dengan gradient
            }
            "search" -> {
                iconSearch.setColorFilter(activeColor)
                labelSearch.setTextColor(activeColor)
            }
            "pengaturan" -> {
                iconPengaturan.setColorFilter(activeColor)
                labelPengaturan.setTextColor(activeColor)
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}