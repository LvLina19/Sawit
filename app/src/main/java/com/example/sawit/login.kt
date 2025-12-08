package com.example.sawit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    // Views
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var btnEmailLogin: Button
    private lateinit var btnGoogle: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Views
        initViews()

        // Setup Google Sign-In
        setupGoogleSignIn()

        // Setup Click Listeners
        setupClickListeners()
    }

    private fun initViews() {
        inputEmail = findViewById(R.id.et_username_email)
        inputPassword = findViewById(R.id.et_password)
        btnEmailLogin = findViewById(R.id.btn_login)
        btnGoogle = findViewById(R.id.btn_google_login)
        progressBar = findViewById(R.id.progressBar)
        tvRegister = findViewById(R.id.tv_login_link)
    }

    private fun setupGoogleSignIn() {
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("266121819468-hlkfoumi163lscbuocgfoiqm25ou7frk.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Setup Activity Result Launcher (cara baru, bukan onActivityResult)
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Login Google gagal: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                showLoading(false)
            }
        }
    }

    private fun setupClickListeners() {
        // Login dengan Email & Password
        btnEmailLogin.setOnClickListener {
            loginWithEmail()
        }

        // Login dengan Google
        btnGoogle.setOnClickListener {
            loginWithGoogle()
        }

        // Navigasi ke Register
        tvRegister.setOnClickListener {
            // Ganti RegisterActivity dengan nama activity register Anda
             startActivity(Intent(this, regris::class.java))
            Toast.makeText(this, "Fitur register belum tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginWithEmail() {
        val email = inputEmail.text.toString().trim()
        val password = inputPassword.text.toString().trim()

        // Validasi input
        if (!validateInput(email, password)) {
            return
        }

        // Tampilkan loading
        showLoading(true)

        // Login ke Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Login Berhasil!",
                        Toast.LENGTH_SHORT
                    ).show()
                    goToMain()
                } else {
                    val errorMessage = when {
                        task.exception?.message?.contains("password") == true ->
                            "Password salah"
                        task.exception?.message?.contains("user") == true ->
                            "Email tidak terdaftar"
                        task.exception?.message?.contains("network") == true ->
                            "Tidak ada koneksi internet"
                        else -> "Login gagal: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun loginWithGoogle() {
        showLoading(true)

        // Sign out terlebih dahulu untuk memastikan dialog pilih akun muncul
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        showLoading(true)

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Login Google Berhasil!",
                        Toast.LENGTH_SHORT
                    ).show()
                    goToMain()
                } else {
                    Toast.makeText(
                        this,
                        "Autentikasi Google gagal: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                inputEmail.error = "Email tidak boleh kosong"
                inputEmail.requestFocus()
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                inputEmail.error = "Format email tidak valid"
                inputEmail.requestFocus()
                false
            }
            password.isEmpty() -> {
                inputPassword.error = "Password tidak boleh kosong"
                inputPassword.requestFocus()
                false
            }
            password.length < 6 -> {
                inputPassword.error = "Password minimal 6 karakter"
                inputPassword.requestFocus()
                false
            }
            else -> true
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnEmailLogin.isEnabled = !isLoading
        btnGoogle.isEnabled = !isLoading
        inputEmail.isEnabled = !isLoading
        inputPassword.isEnabled = !isLoading
    }

    private fun goToMain() {
        // Ganti MainActivity dengan nama activity utama Anda
        val intent = Intent(this, Dashboard::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        // Cek apakah user sudah login
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // User sudah login, langsung ke MainActivity
            goToMain()
        }
    }
}