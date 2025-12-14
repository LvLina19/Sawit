package com.example.sawit.pengaturan
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.sawit.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class LaporkanMasalahFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private val userId by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    private lateinit var btnBack: ImageView
    private lateinit var spinnerCategory: Spinner
    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnSubmitReport: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_laporkan_masalah, container, false)

        firestore = FirebaseFirestore.getInstance()

        initViews(view)
        setupClickListeners()

        return view
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        spinnerCategory = view.findViewById(R.id.spinnerCategory)
        etTitle = view.findViewById(R.id.etTitle)
        etDescription = view.findViewById(R.id.etDescription)
        btnSubmitReport = view.findViewById(R.id.btnSubmitReport)

        spinnerCategory.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            arrayOf("Bug", "Fitur", "Akun", "Lainnya")
        )
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSubmitReport.setOnClickListener {
            submitReport()
        }
    }

    private fun submitReport() {
        val category = spinnerCategory.selectedItem.toString().lowercase()
        val title = etTitle.text.toString()
        val description = etDescription.text.toString()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(requireContext(), "Mohon lengkapi semua field", Toast.LENGTH_SHORT).show()
            return
        }

        val report = hashMapOf(
            "userId" to userId,
            "category" to category,
            "title" to title,
            "description" to description,
            "status" to "pending",
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("problem_reports")
            .add(report)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Laporan berhasil dikirim", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}