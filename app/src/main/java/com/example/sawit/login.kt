package com.example.sawit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider

class login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var imgLoading: ImageView
    private lateinit var rotateAnim: Animation

    // Views
    private lateinit var loadingOverlay: View
    private lateinit var inputEmail: EditText
    private lateinit var inputPassword: EditText
    private lateinit var btnEmailLogin: Button
    private lateinit var btnGoogle: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvRegister: TextView
    private lateinit var Tv_Lupa_Password: TextView

    companion object {
        private const val TAG = "LoginActivity"
    }

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
        imgLoading = findViewById(R.id.imgLoading)
        rotateAnim = AnimationUtils.loadAnimation(this, R.anim.rotate_loading)

        loadingOverlay = findViewById(R.id.loadingOverlay)
        inputEmail = findViewById(R.id.et_username_email)
        inputPassword = findViewById(R.id.et_password)
        btnEmailLogin = findViewById(R.id.btn_login)
        btnGoogle = findViewById(R.id.btn_google_login)
        progressBar = findViewById(R.id.progressBar)
        tvRegister = findViewById(R.id.tv_login_link)
        Tv_Lupa_Password = findViewById(R.id.Tv_Lupa_Password)
    }

    private fun setupGoogleSignIn() {
        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("266121819468-hlkfoumi163lscbuocgfoiqm25ou7frk.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Setup Activity Result Launcher
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
                    Log.e(TAG, "Google sign in failed", e)
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
            startActivity(Intent(this, regris::class.java))
        }

        Tv_Lupa_Password.setOnClickListener {
            startActivity(Intent(this, LupaPassword_activity::class.java))
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

        // Log untuk debugging
        Log.d(TAG, "Attempting login with email: $email")

        // Login ke Firebase
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    // Login berhasil
                    val user = auth.currentUser
                    Log.d(TAG, "Login successful for user: ${user?.uid}")

                    Toast.makeText(
                        this,
                        "Login Berhasil!",
                        Toast.LENGTH_SHORT
                    ).show()
                    goToMain()
                } else {
                    // Login gagal - handle berbagai jenis error
                    val exception = task.exception
                    Log.e(TAG, "Login failed", exception)

                    val errorMessage = when (exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            // Password salah atau email format salah
                            "Email atau password yang Anda masukkan salah. Silakan coba lagi."
                        }
                        is FirebaseAuthInvalidUserException -> {
                            // User tidak ditemukan atau disabled
                            when (exception.errorCode) {
                                "ERROR_USER_NOT_FOUND" ->
                                    "Email tidak terdaftar. Silakan daftar terlebih dahulu."
                                "ERROR_USER_DISABLED" ->
                                    "Akun Anda telah dinonaktifkan. Hubungi administrator."
                                else ->
                                    "Akun tidak valid: ${exception.message}"
                            }
                        }
                        else -> {
                            // Error lainnya
                            when {
                                exception?.message?.contains("network", ignoreCase = true) == true ->
                                    "Tidak ada koneksi internet. Periksa koneksi Anda."
                                exception?.message?.contains("too many requests", ignoreCase = true) == true ->
                                    "Terlalu banyak percobaan login. Coba lagi nanti."
                                else ->
                                    "Login gagal: ${exception?.message}"
                            }
                        }
                    }

                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                Log.e(TAG, "Login exception", exception)
                Toast.makeText(
                    this,
                    "Terjadi kesalahan: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
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
        Log.d(TAG, "Authenticating with Google")

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Log.d(TAG, "Google login successful for user: ${user?.uid}")

                    Toast.makeText(
                        this,
                        "Login Google Berhasil!",
                        Toast.LENGTH_SHORT
                    ).show()
                    goToMain()
                } else {
                    Log.e(TAG, "Google authentication failed", task.exception)
                    Toast.makeText(
                        this,
                        "Autentikasi Google gagal: ${task.exception?.message}",
                        Toast.LENGTH_LONG
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
        if (isLoading) {
            loadingOverlay.visibility = View.VISIBLE
            imgLoading.startAnimation(rotateAnim)
        } else {
            imgLoading.clearAnimation()
            loadingOverlay.visibility = View.GONE
        }

        btnEmailLogin.isEnabled = !isLoading
        btnGoogle.isEnabled = !isLoading
        inputEmail.isEnabled = !isLoading
        inputPassword.isEnabled = !isLoading
    }


    private fun goToMain() {
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
            Log.d(TAG, "User already logged in: ${currentUser.uid}")
            // User sudah login, langsung ke Dashboard
            goToMain()
        }
    }
}