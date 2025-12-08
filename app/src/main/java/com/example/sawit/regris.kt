package com.example.sawit

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.example.sawit.databinding.ActivityRegrisBinding

class regris : AppCompatActivity() {

    private lateinit var binding: ActivityRegrisBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegrisBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Google Sign In
        setupGoogleSignIn()

        // Setup click listeners
        setupClickListeners()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
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
            binding.tilUsernameEmail.error = "Username atau email tidak boleh kosong"
            binding.etUsernameEmail.requestFocus()
            return
        }

        // Validate if it's email format
        if (usernameEmail.contains("@") && !Patterns.EMAIL_ADDRESS.matcher(usernameEmail).matches()) {
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

    private fun performRegistration(usernameEmail: String, password: String) {
        // TODO: Implement your registration logic here
        // This could be:
        // - Firebase Authentication
        // - REST API call to your backend
        // - Local database storage

        // Example success response
        Toast.makeText(this, "Registrasi berhasil!", Toast.LENGTH_SHORT).show()

        // Navigate to login or main activity
        navigateToMainActivity()
        finish()
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
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
            // Google Sign In successful
            Toast.makeText(this, "Login Google berhasil: ${account?.email}", Toast.LENGTH_SHORT).show()

            // TODO: Send token to your backend or handle authentication
            // account?.idToken - use this for backend verification

            // Navigate to main activity
            navigateToMainActivity()

        } catch (e: ApiException) {
            Toast.makeText(this, "Login Google gagal: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToLogin() {
        // TODO: Replace with your actual Login activity
        val intent = Intent(this, login::class.java)
        startActivity(intent)
    }

    private fun navigateToMainActivity() {
        // TODO: Replace with your actual Main activity
        val intent = Intent(this, Dashboard::class.java)
        startActivity(intent)
        finish()
    }
}