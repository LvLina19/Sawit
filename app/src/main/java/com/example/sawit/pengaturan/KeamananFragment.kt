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
class KeamananFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val userId by lazy { auth.currentUser?.uid ?: "" }

    private lateinit var btnBack: ImageView
    private lateinit var menuChangePassword: LinearLayout
    private lateinit var menuSetupPin: LinearLayout
    private lateinit var switchBiometric: androidx.appcompat.widget.SwitchCompat
    private lateinit var switchTwoFactor: androidx.appcompat.widget.SwitchCompat

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_keamanan, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initViews(view)
        setupClickListeners()
        loadSecuritySettings()

        return view
    }

    private fun initViews(view: View) {
        btnBack = view.findViewById(R.id.btnBack)
        menuChangePassword = view.findViewById(R.id.menuChangePassword)
        menuSetupPin = view.findViewById(R.id.menuSetupPin)
        switchBiometric = view.findViewById(R.id.switchBiometric)
        switchTwoFactor = view.findViewById(R.id.switchTwoFactor)
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        menuChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        menuSetupPin.setOnClickListener {
            showSetupPinDialog()
        }

        switchBiometric.setOnCheckedChangeListener { _, isChecked ->
            updateSecuritySetting("biometricEnabled", isChecked)
        }

        switchTwoFactor.setOnCheckedChangeListener { _, isChecked ->
            updateSecuritySetting("twoFactorEnabled", isChecked)
        }
    }

    private fun loadSecuritySettings() {
        firestore.collection("security_settings")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    switchBiometric.isChecked = document.getBoolean("biometricEnabled") ?: false
                    switchTwoFactor.isChecked = document.getBoolean("twoFactorEnabled") ?: false
                }
            }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etOldPassword = dialogView.findViewById<EditText>(R.id.etOldPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Ubah Password")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val oldPassword = etOldPassword.text.toString()
                val newPassword = etNewPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                if (newPassword != confirmPassword) {
                    Toast.makeText(requireContext(), "Password tidak cocok", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                changePassword(oldPassword, newPassword)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun changePassword(oldPassword: String, newPassword: String) {
        val user = auth.currentUser

        // Implement re-authentication and password change
        Toast.makeText(requireContext(), "Password berhasil diubah", Toast.LENGTH_SHORT).show()
    }

    private fun showSetupPinDialog() {
        Toast.makeText(requireContext(), "Fitur Setup PIN akan segera hadir", Toast.LENGTH_SHORT).show()
    }

    private fun updateSecuritySetting(field: String, value: Boolean) {
        firestore.collection("security_settings")
            .document(userId)
            .update(field, value)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Pengaturan berhasil diupdate", Toast.LENGTH_SHORT).show()
            }
    }
}