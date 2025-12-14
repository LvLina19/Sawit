package com.example.sawit.IsiDashboard

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.sawit.R

class GameActivity : AppCompatActivity() {

    private lateinit var webViewGame: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvGameTitle: TextView
    private lateinit var btnBack: ImageView

    private val handler = Handler(Looper.getMainLooper())
    private var playStartTime = 0L
    private val rewardThresholdMinutes = 5 // Main 5 menit = dapat poin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Initialize views
        webViewGame = findViewById(R.id.webViewGame)
        progressBar = findViewById(R.id.progressBar)
        tvGameTitle = findViewById(R.id.tvGameTitle)
        btnBack = findViewById(R.id.btnBack)

        // Get data from intent
        val gameUrl = intent.getStringExtra("GAME_URL")
        val gameTitle = intent.getStringExtra("GAME_TITLE")

        // Set title
        gameTitle?.let { tvGameTitle.text = it }

        // Back button
        btnBack.setOnClickListener { finish() }

        // Setup WebView
        setupWebView()

        // Load game
        gameUrl?.let { webViewGame.loadUrl(it) }

        // Start tracking play time
        playStartTime = System.currentTimeMillis()
        startRewardTimer()
    }

    private fun setupWebView() {
        webViewGame.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                builtInZoomControls = true
                displayZoomControls = false
                mediaPlaybackRequiresUserGesture = false
                cacheMode = WebSettings.LOAD_DEFAULT
                allowFileAccess = true
                allowContentAccess = true
            }

            setLayerType(View.LAYER_TYPE_HARDWARE, null)

            // Add JavaScript Interface untuk tracking score (opsional)
            addJavascriptInterface(WebAppInterface(this@GameActivity), "Android")

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    url?.let { view?.loadUrl(it) }
                    return true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = View.GONE
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    progressBar.visibility = if (newProgress < 100) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun startRewardTimer() {
        // Cek setiap menit
        handler.postDelayed(object : Runnable {
            override fun run() {
                val playTimeMinutes = (System.currentTimeMillis() - playStartTime) / 60000

                if (playTimeMinutes >= rewardThresholdMinutes) {
                    giveReward()
                    handler.removeCallbacks(this) // Stop timer setelah kasih reward
                } else {
                    handler.postDelayed(this, 60000) // Check again in 1 minute
                }
            }
        }, 60000) // Start checking after 1 minute
    }

    private fun giveReward() {
        val points = 100 // Berikan 100 poin

        // Save poin ke SharedPreferences
        val prefs = getSharedPreferences("game_rewards", Context.MODE_PRIVATE)
        val currentPoints = prefs.getInt("total_points", 0)
        prefs.edit().putInt("total_points", currentPoints + points).apply()

        // Show reward dialog
        showRewardDialog(points)
    }

    private fun showRewardDialog(points: Int) {
        AlertDialog.Builder(this)
            .setTitle("🎉 Selamat!")
            .setMessage("Kamu telah bermain selama $rewardThresholdMinutes menit!\n\nHadiah: $points poin")
            .setPositiveButton("Ambil Hadiah") { dialog, _ ->
                Toast.makeText(this, "✅ $points poin berhasil ditambahkan!", Toast.LENGTH_LONG).show()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    // JavaScript Interface untuk komunikasi game -> Android
    inner class WebAppInterface(private val context: Context) {

        @JavascriptInterface
        fun submitScore(score: Int) {
            // Dipanggil dari JavaScript game (kalau game support custom tracking)
            runOnUiThread {
                Toast.makeText(context, "Score: $score", Toast.LENGTH_SHORT).show()

                // Save score ke database/backend
                saveScoreToBackend(score)
            }
        }

        @JavascriptInterface
        fun gameCompleted() {
            runOnUiThread {
                giveReward()
            }
        }
    }

    private fun saveScoreToBackend(score: Int) {
        // TODO: Kirim score ke backend API untuk leaderboard
        // Contoh: ApiService.submitScore(userId, score)
    }

    override fun onBackPressed() {
        if (webViewGame.canGoBack()) {
            webViewGame.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        webViewGame.onPause()
    }

    override fun onResume() {
        super.onResume()
        webViewGame.onResume()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null) // Stop all timers
        webViewGame.destroy()
        super.onDestroy()
    }
}