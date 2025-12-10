package com.example.sawit.IsiDashboard

//import android.R
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment
import com.example.sawit.R
import java.util.Calendar


class CatatPanenFragment : Fragment() {
    private lateinit var etTanggalPanen: EditText
    private lateinit var layoutTanggalPanen: RelativeLayout
    private lateinit var etLokasi: EditText
    private lateinit var etTBSMatang: EditText
    private lateinit var etTBSTidakMatang: EditText
    private lateinit var etTBSKelewatMatang: EditText
    private lateinit var btnBack: ImageView
    private lateinit var btnSubmit: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout fragment
        val rootView = inflater.inflate(R.layout.fragment_catat_panen, container, false)

        // Initialize views
        initViews(rootView)

        // Setup listeners
        setupListeners()

        return rootView
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        layoutTanggalPanen = view.findViewById(R.id.layoutTanggalPanen)
        etTanggalPanen = view.findViewById(R.id.etTanggalPanen)
        etLokasi = view.findViewById(R.id.etLokasi)
        etTBSMatang = view.findViewById(R.id.etTBSMatang)
        etTBSTidakMatang = view.findViewById(R.id.etTBSTidakMatang)
        etTBSKelewatMatang = view.findViewById(R.id.etTBSKelewatMatang)
        btnSubmit = view.findViewById(R.id.btnSubmit)
    }

    private fun setupListeners() {
        // Back button - untuk kembali ke fragment sebelumnya
        btnBack.setOnClickListener {
            // Kembali ke fragment sebelumnya
            activity?.supportFragmentManager?.popBackStack()
        }

        // Date picker for Tanggal Panen - klik pada layout
        layoutTanggalPanen.setOnClickListener {
            showDatePicker()
        }

        // GPS location button
        etLokasi.setOnClickListener {
            getGPSLocation()
        }

        // Submit button
        btnSubmit.setOnClickListener {
            submitData()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                // Format: DD/MM/YYYY
                val selectedDate = String.format("%02d/%02d/%d", selectedDay, selectedMonth + 1, selectedYear)
                etTanggalPanen.setText(selectedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    private fun getGPSLocation() {
        // Implementasi GPS Location
        // Untuk sementara menggunakan dummy data
        // Anda bisa implementasikan dengan FusedLocationProviderClient

        Toast.makeText(requireContext(), "Mendapatkan lokasi GPS...", Toast.LENGTH_SHORT).show()

        // Contoh dummy location
        etLokasi.setText("Latitude: -0.5333, Longitude: 101.4478")

        // TODO: Implementasi real GPS
        // Tambahkan permission di AndroidManifest.xml:
        // <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
        // <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

        /* Contoh implementasi GPS real:
        if (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val locationText = "Latitude: ${it.latitude}, Longitude: ${it.longitude}"
                        etLokasi.setText(locationText)
                    }
                }
        } else {
            // Request permission
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
        */
    }

    private fun submitData() {
        // Validasi input
        val tanggalPanen = etTanggalPanen.text.toString().trim()
        val lokasi = etLokasi.text.toString().trim()
        val tbsMatang = etTBSMatang.text.toString().trim()
        val tbsTidakMatang = etTBSTidakMatang.text.toString().trim()
        val tbsKelewatMatang = etTBSKelewatMatang.text.toString().trim()

        when {
            tanggalPanen.isEmpty() -> {
                Toast.makeText(requireContext(), "Silakan pilih tanggal panen", Toast.LENGTH_SHORT).show()
                return
            }
            lokasi.isEmpty() -> {
                Toast.makeText(requireContext(), "Silakan masukkan lokasi", Toast.LENGTH_SHORT).show()
                return
            }
            tbsMatang.isEmpty() -> {
                Toast.makeText(requireContext(), "Silakan masukkan berat TBS Matang", Toast.LENGTH_SHORT).show()
                return
            }
            tbsTidakMatang.isEmpty() -> {
                Toast.makeText(requireContext(), "Silakan masukkan berat TBS Tidak Matang", Toast.LENGTH_SHORT).show()
                return
            }
            tbsKelewatMatang.isEmpty() -> {
                Toast.makeText(requireContext(), "Silakan masukkan berat TBS Kelewat Matang", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Proses submit data
        // TODO: Simpan ke database atau kirim ke server

        // Contoh: Buat object data
        /*
        val panenData = PanenData(
            tanggalPanen = tanggalPanen,
            lokasi = lokasi,
            tbsMatang = tbsMatang.toDouble(),
            tbsTidakMatang = tbsTidakMatang.toDouble(),
            tbsKelewatMatang = tbsKelewatMatang.toDouble()
        )

        // Simpan ke database
        // databaseHelper.insertPanen(panenData)
        */

        Toast.makeText(requireContext(), "Data berhasil disimpan!", Toast.LENGTH_SHORT).show()

        // Kembali ke fragment sebelumnya
        activity?.supportFragmentManager?.popBackStack()
    }

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }
}