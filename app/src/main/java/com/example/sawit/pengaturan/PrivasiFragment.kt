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
class PrivasiFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private val userId by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    private lateinit var btnBack: ImageView
    private lateinit var spinnerProfileVisibility: Spinner
    private lateinit var switchOnlineStatus: androidx.appcompat.widget.SwitchCompat
    private lateinit var switchActivityHistory: androidx.appcompat.widget.SwitchCompat
    private lateinit var switchDataSharing: androidx.appcompat.widget.SwitchCompat

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_privasi, container, false)

        firestore = FirebaseFirestore.getInstance()

        initViews(view)
        setupClickListeners()
        loadPrivacySettings()

        return view
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        spinnerProfileVisibility = view.findViewById(R.id.spinnerProfileVisibility)
        switchOnlineStatus = view.findViewById(R.id.switchOnlineStatus)
        switchActivityHistory = view.findViewById(R.id.switchActivityHistory)
        switchDataSharing = view.findViewById(R.id.switchDataSharing)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        spinnerProfileVisibility.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            arrayOf("Publik", "Teman Saja", "Privat")
        )

        spinnerProfileVisibility.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                val visibility = when (pos) {
                    0 -> "public"
                    1 -> "friends"
                    else -> "private"
                }
                updatePrivacySetting("profileVisibility", visibility)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        switchOnlineStatus.setOnCheckedChangeListener { _, isChecked ->
            updatePrivacySetting("showOnlineStatus", isChecked)
        }

        switchActivityHistory.setOnCheckedChangeListener { _, isChecked ->
            updatePrivacySetting("showActivityHistory", isChecked)
        }

        switchDataSharing.setOnCheckedChangeListener { _, isChecked ->
            updatePrivacySetting("dataSharing", isChecked)
        }
    }

    private fun loadPrivacySettings() {
        firestore.collection("privacy_settings")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val visibility = document.getString("profileVisibility") ?: "public"
                    val visibilityIndex = when (visibility) {
                        "public" -> 0
                        "friends" -> 1
                        else -> 2
                    }
                    spinnerProfileVisibility.setSelection(visibilityIndex)

                    switchOnlineStatus.isChecked = document.getBoolean("showOnlineStatus") ?: true
                    switchActivityHistory.isChecked = document.getBoolean("showActivityHistory") ?: true
                    switchDataSharing.isChecked = document.getBoolean("dataSharing") ?: false
                }
            }
    }

    private fun updatePrivacySetting(field: String, value: Any) {
        firestore.collection("privacy_settings")
            .document(userId)
            .update(field, value)
    }
}
