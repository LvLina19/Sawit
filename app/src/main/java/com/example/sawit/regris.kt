package com.example.sawit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.example.sawit.databinding.ActivityRegrisBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class regris : AppCompatActivity() {

    private lateinit var binding: ActivityRegrisBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100

    companion object {
        private const val TAG = "RegisterActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegrisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Setup Google Sign In
        setupGoogleSignIn()

        // Setup click listeners
        setupClickListeners()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("266121819468-hlkfoumi163lscbuocgfoiqm25ou7frk.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupClickListeners() {
        // Register button click
        binding.btnRegister.setOnClickListener {
            validateAndRegister()
        }

        // Google login button click
        binding.btnGoogleLogin.setOnClickListener {
            signInWithGoogle()
        }

        // Login link click
        binding.tvLoginLink.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun validateAndRegister() {
        val usernameEmail = binding.etUsernameEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Reset errors
        binding.tilUsernameEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null

        // Validate username/email
        if (usernameEmail.isEmpty()) {
            binding.tilUsernameEmail.error = "Email tidak boleh kosong"
            binding.etUsernameEmail.requestFocus()
            return
        }

        // Validate email format (harus email untuk Firebase Auth)
        if (!Patterns.EMAIL_ADDRESS.matcher(usernameEmail).matches()) {
            binding.tilUsernameEmail.error = "Format email tidak valid"
            binding.etUsernameEmail.requestFocus()
            return
        }

        // Validate password
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password tidak boleh kosong"
            binding.etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.tilPassword.error = "Password minimal 6 karakter"
            binding.etPassword.requestFocus()
            return
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Konfirmasi password tidak boleh kosong"
            binding.etConfirmPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = "Password tidak cocok"
            binding.etConfirmPassword.requestFocus()
            return
        }

        // All validations passed, proceed with registration
        performRegistration(usernameEmail, password)
    }

    private fun performRegistration(email: String, password: String) {
        // Show loading
        showLoading(true)

        Log.d(TAG, "Attempting registration with email: $email")

        // Register with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful
                    val user = auth.currentUser
                    Log.d(TAG, "Registration successful for user: ${user?.uid}")

                    // Create user profile in Firestore
                    user?.let {
                        createUserProfile(it.uid, email)
                    } ?: run {
                        showLoading(false)
                        Toast.makeText(this, "Registrasi gagal: User ID tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    // Registration failed
                    showLoading(false)
                    val exception = task.exception
                    Log.e(TAG, "Registration failed", exception)

                    val errorMessage = when (exception) {
                        is FirebaseAuthWeakPasswordException -> {
                            "Password terlalu lemah. Gunakan kombinasi huruf, angka, dan simbol."
                        }
                        is FirebaseAuthUserCollisionException -> {
                            "Email sudah terdaftar. Silakan gunakan email lain atau login."
                        }
                        else -> {
                            when {
                                exception?.message?.contains("network", ignoreCase = true) == true ->
                                    "Tidak ada koneksi internet. Periksa koneksi Anda."
                                else ->
                                    "Registrasi gagal: ${exception?.message}"
                            }
                        }
                    }

                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                Log.e(TAG, "Registration exception", exception)
                Toast.makeText(
                    this,
                    "Terjadi kesalahan: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun createUserProfile(userId: String, email: String) {
        // Create user profile data
        val userProfile = hashMapOf(
            "userId" to userId,
            "email" to email,
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        // Save to Firestore
        firestore.collection("users")
            .document(userId)
            .set(userProfile)
            .addOnSuccessListener {
                Log.d(TAG, "User profile created successfully")

                // Create default settings
                createDefaultSettings(userId)
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e(TAG, "Failed to create user profile", e)
                Toast.makeText(
                    this,
                    "Profil pengguna gagal dibuat: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()

                // Still navigate to dashboard even if profile creation fails
                navigateToMainActivity()
            }
    }

    private fun createDefaultSettings(userId: String) {
        val defaultSettings = hashMapOf(
            "userId" to userId,
            "language" to "id",
            "theme" to "system",
            "notificationsEnabled" to true,
            "soundEnabled" to true,
            "vibrationEnabled" to true,
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        firestore.collection("user_settings")
            .document(userId)
            .set(defaultSettings)
            .addOnSuccessListener {
                showLoading(false)
                Log.d(TAG, "Default settings created successfully")

                Toast.makeText(this, "Registrasi berhasil! Selamat datang!", Toast.LENGTH_SHORT).show()

                // Navigate to main activity
                navigateToMainActivity()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Log.e(TAG, "Failed to create default settings", e)

                // Still navigate even if settings creation fails
                Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()
                navigateToMainActivity()
            }
    }

    private fun signInWithGoogle() {
        showLoading(true)

        // Sign out first to show account chooser
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "Google sign in successful: ${account.email}")

            // Authenticate with Firebase
            firebaseAuthWithGoogle(account.idToken!!)

        } catch (e: ApiException) {
            showLoading(false)
            Log.e(TAG, "Google sign in failed", e)
            Toast.makeText(this, "Login Google gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        showLoading(true)
        Log.d(TAG, "Authenticating with Google")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false

                    Log.d(TAG, "Google authentication successful. New user: $isNewUser")

                    user?.let {
                        if (isNewUser) {
                            // New user - create profile
                            createUserProfile(it.uid, it.email ?: "")
                        } else {
                            // Existing user - just navigate
                            showLoading(false)
                            Toast.makeText(
                                this,
                                "Login Google berhasil!",
                                Toast.LENGTH_SHORT
                            ).show()
                            navigateToMainActivity()
                        }
                    }

                } else {
                    showLoading(false)
                    Log.e(TAG, "Google authentication failed", task.exception)
                    Toast.makeText(
                        this,
                        "Autentikasi Google gagal: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.btnRegister.isEnabled = false
            binding.btnGoogleLogin.isEnabled = false
            binding.etUsernameEmail.isEnabled = false
            binding.etPassword.isEnabled = false
            binding.etConfirmPassword.isEnabled = false
        } else {
            binding.btnRegister.isEnabled = true
            binding.btnGoogleLogin.isEnabled = true
            binding.etUsernameEmail.isEnabled = true
            binding.etPassword.isEnabled = true
            binding.etConfirmPassword.isEnabled = true
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, login::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, Dashboard::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        // Check if user is already signed in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "User already logged in: ${currentUser.uid}")
            navigateToMainActivity()
        }
    }
}