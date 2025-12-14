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

class EditProfilFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val userId by lazy { auth.currentUser?.uid ?: "" }

    private lateinit var btnBack: ImageView
    private lateinit var etName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSave: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_profil, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initViews(view)
        setupClickListeners()
        loadUserProfile()

        return view
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        etName = view.findViewById(R.id.etName)
        etEmail = view.findViewById(R.id.etEmail)
        etPhone = view.findViewById(R.id.etPhone)
        btnSave = view.findViewById(R.id.btnSave)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun loadUserProfile() {
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    etName.setText(document.getString("name"))
                    etEmail.setText(document.getString("email"))
                    etPhone.setText(document.getString("phone"))
                }
            }
    }

    private fun saveProfile() {
        val name = etName.text.toString()
        val email = etEmail.text.toString()
        val phone = etPhone.text.toString()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val profileData = hashMapOf(
            "name" to name,
            "email" to email,
            "phone" to phone,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(userId)
            .update(profileData as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Profil berhasil diupdate", Toast.LENGTH_SHORT).show()
                parentFragmentManager.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}