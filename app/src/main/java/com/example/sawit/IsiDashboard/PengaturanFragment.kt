package com.example.sawit.IsiDashboard

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.sawit.login
import com.example.sawit.R
import com.example.sawit.model.UserSettings
import com.example.sawit.pengaturan.EditProfilFragment
import com.example.sawit.pengaturan.KeamananFragment
import com.example.sawit.pengaturan.LaporkanMasalahFragment
import com.example.sawit.pengaturan.NotifikasiFragment
import com.example.sawit.pengaturan.PenyimpananDataFragment
import com.example.sawit.pengaturan.PrivasiFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class PengaturanFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val userId by lazy { auth.currentUser?.uid ?: "" }

    // Views
    private lateinit var btnBack: ImageView
    private lateinit var menuEditProfil: LinearLayout
    private lateinit var menuKeamanan: LinearLayout
    private lateinit var menuNotifikasi: LinearLayout
    private lateinit var menuPrivasi: LinearLayout
    private lateinit var menuPenyimpananData: LinearLayout
    private lateinit var menuLaporkanMasalah: LinearLayout
    private lateinit var menuTambahkanAkun: LinearLayout
    private lateinit var menuKeluar: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pengaturan, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initViews(view)
        setupClickListeners()
        loadUserSettings()

        return view
    }

    private fun redirectToLogin() {
        Toast.makeText(
            requireContext(),
            "Sesi Anda telah berakhir. Silakan login kembali.",
            Toast.LENGTH_LONG
        ).show()
        val intent = Intent(requireContext(), login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun saveThemePreference(themeCode: String) {
        // TAMBAHKAN: Check userId
        if (userId.isEmpty()) return

        requireActivity().getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("theme", themeCode)
            .apply()

        firestore.collection("user_settings")
            .document(userId)
            .update("theme", themeCode)
            .addOnSuccessListener {
                applyTheme(themeCode)
            }
    }

    private fun initializeDefaultSettings() {
        // TAMBAHKAN: Check userId
        if (userId.isEmpty()) return

        val defaultSettings = UserSettings(
            userId = userId,
            language = "id",
            theme = "system",
            notificationsEnabled = true,
            soundEnabled = true,
            vibrationEnabled = true
        )

        firestore.collection("user_settings")
            .document(userId)
            .set(defaultSettings)
            .addOnSuccessListener {
                // Settings initialized
            }
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        menuEditProfil = view.findViewById(R.id.menuEditProfil)
        menuKeamanan = view.findViewById(R.id.menuKeamanan)
        menuNotifikasi = view.findViewById(R.id.menuNotifikasi)
        menuPrivasi = view.findViewById(R.id.menuPrivasi)
        menuPenyimpananData = view.findViewById(R.id.menuPenyimpananData)
        menuLaporkanMasalah = view.findViewById(R.id.menuLaporkanMasalah)
        menuTambahkanAkun = view.findViewById(R.id.menuTambahkanAkun)
        menuKeluar = view.findViewById(R.id.menuKeluar)
    }

    private fun setupClickListeners() {
        // Back button - kembali ke fragment sebelumnya atau Beranda
        btnBack.setOnClickListener {
            // Navigasi ke BerandaFragment
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, BerandaFragment())
                .commit()
        }

        // Edit Profil
        menuEditProfil.setOnClickListener {
            navigateToFragment(EditProfilFragment())
        }

        // Keamanan
        menuKeamanan.setOnClickListener {
            navigateToFragment(KeamananFragment())
        }

        // Notifikasi
        menuNotifikasi.setOnClickListener {
            navigateToFragment(NotifikasiFragment())
        }

        // Privasi
        menuPrivasi.setOnClickListener {
            navigateToFragment(PrivasiFragment())
        }


        // Penyimpanan Data
        menuPenyimpananData.setOnClickListener {
            navigateToFragment(PenyimpananDataFragment())
        }

        // Laporkan Masalah
        menuLaporkanMasalah.setOnClickListener {
            navigateToFragment(LaporkanMasalahFragment())
        }

        // Tambahkan Akun
        menuTambahkanAkun.setOnClickListener {
            Toast.makeText(requireContext(), "Fitur akan segera hadir", Toast.LENGTH_SHORT).show()
        }

        // Keluar
        menuKeluar.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadUserSettings() {
        firestore.collection("user_settings")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    initializeDefaultSettings()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showLanguageDialog() {
        val languages = arrayOf("Bahasa Indonesia", "English")
        val prefs =
            requireActivity().getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE)
        val currentLanguage = prefs.getString("language", "id") ?: "id"

        val selectedIndex = if (currentLanguage == "id") 0 else 1

        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Bahasa")
            .setSingleChoiceItems(languages, selectedIndex) { dialog, which ->
                val langCode = if (which == 0) "id" else "en"
                saveLanguagePreference(langCode)
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun saveLanguagePreference(langCode: String) {
        // Save to SharedPreferences
        requireActivity().getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("language", langCode)
            .apply()

        // Save to Firebase
        firestore.collection("user_settings")
            .document(userId)
            .update("language", langCode)
            .addOnSuccessListener {
                // Terapkan bahasa langsung
                setLocale(requireContext(), langCode)

                // Restart activity untuk memuat ulang semua UI
                requireActivity().recreate()
            }
    }

    // Fungsi untuk menerapkan bahasa
    private fun setLocale(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Terang", "Gelap", "Ikuti Sistem")
        val prefs =
            requireActivity().getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE)
        val currentTheme = prefs.getString("theme", "system") ?: "system"

        val selectedIndex = when (currentTheme) {
            "light" -> 0
            "dark" -> 1
            else -> 2
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Pilih Tema")
            .setSingleChoiceItems(themes, selectedIndex) { dialog, which ->
                val themeCode = when (which) {
                    0 -> "light"
                    1 -> "dark"
                    else -> "system"
                }
                saveThemePreference(themeCode)
                dialog.dismiss()
            }
            .setNegativeButton("Batal", null)
            .show()
    }


    private fun applyTheme(themeCode: String) {
        // Implementasi apply theme
        // Bisa menggunakan AppCompatDelegate.setDefaultNightMode()
        when (themeCode) {
            "light" -> {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                )
            }

            "dark" -> {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                )
            }

            else -> {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(
                    androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                )
            }
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Keluar")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setPositiveButton("Ya") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun performLogout() {
        // Clear local data
        requireActivity().getSharedPreferences("Settings", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        // Sign out from Firebase
        auth.signOut()

        // Navigate to login
        val intent = Intent(requireContext(), login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
}