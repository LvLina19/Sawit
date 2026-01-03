package com.example.sawit

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.animation.ValueAnimator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.model.ChatMessage
import com.example.sawit.model.GeminiApiClient
import com.example.sawit.utils.GeminiRequest
import com.example.sawit.utils.Content
import com.example.sawit.utils.Part
import kotlinx.coroutines.launch
import kotlin.math.abs

class ChatBotActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var micButton: ImageButton

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var backButton: ImageButton

    // Voice recording UI
    private lateinit var voiceRecordingOverlay: ConstraintLayout
    private lateinit var waveBar1: View
    private lateinit var waveBar2: View
    private lateinit var waveBar3: View
    private lateinit var waveBar4: View
    private lateinit var waveBar5: View
    private lateinit var recordingText: TextView
    private lateinit var recordingTimer: TextView
    private lateinit var slideToCancel: TextView

    private var waveAnimator: AnimatorSet? = null
    private var timerAnimator: ValueAnimator? = null

    private val chatList = mutableListOf<ChatMessage>()
    private val apiKey = "AIzaSyCX8AblhVcpRofRnuUWyBR4MgHXrsLw1hE"

    private val RECORD_AUDIO_PERMISSION_CODE = 101
    private var isRecording = false
    private var recognizedText = ""
    private var recordingStartTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chatbot)

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        checkAudioPermission()
        setupViews()
        setupSpeechRecognizer()

        addBotMessage("Halo 🌴 Saya SawitMaju Bot.\nSilakan tanya soal harga sawit, panen, atau kebun.")

        backButton.setOnClickListener {
            finish()
        }

        sendButton.setOnClickListener {
            val userText = messageInput.text.toString().trim()
            if (userText.isNotEmpty()) {
                sendMessage(userText)
            }
        }
    }

    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "✅ Permission microphone diberikan", Toast.LENGTH_SHORT).show()
                setupSpeechRecognizer()
            } else {
                Toast.makeText(
                    this,
                    "❌ Permission ditolak, fitur suara tidak bisa digunakan",
                    Toast.LENGTH_LONG
                ).show()
                micButton.isEnabled = false
            }
        }
    }

    private fun setupSpeechRecognizer() {
        micButton = findViewById(R.id.micButton)

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(
                this,
                "❌ Speech recognition tidak tersedia di device ini",
                Toast.LENGTH_LONG
            ).show()
            micButton.isEnabled = false
            return
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            micButton.isEnabled = false
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )
                if (!matches.isNullOrEmpty()) {
                    recognizedText = matches[0]

                    if (recognizedText.isNotEmpty()) {
                        sendMessage(recognizedText)
                    }
                }
                stopRecording()
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Error audio"
                    SpeechRecognizer.ERROR_CLIENT -> "Error client"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission tidak cukup"
                    SpeechRecognizer.ERROR_NETWORK -> "Error jaringan"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Timeout jaringan"
                    SpeechRecognizer.ERROR_NO_MATCH -> "Tidak ada suara terdeteksi"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer sedang sibuk"
                    SpeechRecognizer.ERROR_SERVER -> "Error server"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tidak ada suara terdeteksi"
                    else -> "Error: $error"
                }

                if (error != SpeechRecognizer.ERROR_NO_MATCH &&
                    error != SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    Toast.makeText(
                        this@ChatBotActivity,
                        "❌ $errorMessage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                stopRecording()
            }

            override fun onReadyForSpeech(params: Bundle?) {
                recordingText.text = "Sedang merekam..."
            }

            override fun onBeginningOfSpeech() {
                recordingText.text = "Mendengarkan..."
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Update wave bars based on sound level
                updateWaveBars(rmsdB)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                recordingText.text = "Memproses..."
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )
                if (!matches.isNullOrEmpty()) {
                    recordingText.text = matches[0]
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        // Touch listener untuk press and hold
        micButton.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        // Visual feedback
                        view.animate()
                            .scaleX(0.9f)
                            .scaleY(0.9f)
                            .setDuration(100)
                            .start()

                        startRecording(speechIntent)
                    } else {
                        Toast.makeText(
                            this,
                            "❌ Permission microphone belum diberikan",
                            Toast.LENGTH_SHORT
                        ).show()
                        checkAudioPermission()
                    }
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Reset visual feedback
                    view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    if (isRecording) {
                        speechRecognizer.stopListening()
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun startRecording(speechIntent: Intent) {
        if (isRecording) return

        isRecording = true
        recognizedText = ""
        recordingStartTime = System.currentTimeMillis()

        // Show overlay with animation
        voiceRecordingOverlay.visibility = View.VISIBLE
        voiceRecordingOverlay.alpha = 0f
        voiceRecordingOverlay.animate()
            .alpha(1f)
            .setDuration(200)
            .start()

        recordingText.text = "Tahan untuk merekam..."
        recordingTimer.text = "0:00"

        startWaveAnimation()
        startTimerAnimation()

        // Hide keyboard
        messageInput.clearFocus()

        try {
            speechRecognizer.startListening(speechIntent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "❌ Gagal memulai: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
            stopRecording()
        }
    }

    private fun stopRecording() {
        if (!isRecording) return

        isRecording = false

        // Hide overlay with animation
        voiceRecordingOverlay.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                voiceRecordingOverlay.visibility = View.GONE
            }
            .start()

        stopWaveAnimation()
        stopTimerAnimation()
    }

    private fun startWaveAnimation() {
        waveAnimator?.cancel()

        val animators = listOf(
            createWaveAnimator(waveBar1, 300, 0),
            createWaveAnimator(waveBar2, 400, 50),
            createWaveAnimator(waveBar3, 500, 100),
            createWaveAnimator(waveBar4, 400, 150),
            createWaveAnimator(waveBar5, 300, 200)
        )

        waveAnimator = AnimatorSet().apply {
            playTogether(animators)
            start()
        }
    }

    private fun createWaveAnimator(view: View, duration: Long, startDelay: Long): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, "scaleY", 0.3f, 1.0f, 0.3f).apply {
            this.duration = duration
            this.startDelay = startDelay
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    private fun updateWaveBars(rmsdB: Float) {
        // Normalize dB to scale (0.3 to 1.0)
        val normalized = ((rmsdB + 2) / 10).coerceIn(0.3f, 1.0f)

        // Update each bar with slight variations
        waveBar1.scaleY = normalized * 0.8f
        waveBar2.scaleY = normalized * 1.0f
        waveBar3.scaleY = normalized * 1.2f
        waveBar4.scaleY = normalized * 1.0f
        waveBar5.scaleY = normalized * 0.8f
    }

    private fun stopWaveAnimation() {
        waveAnimator?.cancel()
        // Reset bars
        listOf(waveBar1, waveBar2, waveBar3, waveBar4, waveBar5).forEach {
            it.scaleY = 0.3f
        }
    }

    private fun startTimerAnimation() {
        timerAnimator?.cancel()
        timerAnimator = ValueAnimator.ofInt(0, 60).apply {
            duration = 60000 // 60 seconds
            addUpdateListener { animator ->
                val seconds = animator.animatedValue as Int
                val minutes = seconds / 60
                val secs = seconds % 60
                recordingTimer.text = String.format("%d:%02d", minutes, secs)
            }
            start()
        }
    }

    private fun stopTimerAnimation() {
        timerAnimator?.cancel()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        backButton = findViewById(R.id.backButton)
        voiceRecordingOverlay = findViewById(R.id.voiceRecordingOverlay)
        waveBar1 = findViewById(R.id.waveBar1)
        waveBar2 = findViewById(R.id.waveBar2)
        waveBar3 = findViewById(R.id.waveBar3)
        waveBar4 = findViewById(R.id.waveBar4)
        waveBar5 = findViewById(R.id.waveBar5)
        recordingText = findViewById(R.id.recordingText)
        recordingTimer = findViewById(R.id.recordingTimer)
        slideToCancel = findViewById(R.id.slideToCancel)

        chatAdapter = ChatAdapter(chatList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter

        voiceRecordingOverlay.visibility = View.GONE
    }

    private fun sendMessage(userText: String) {
        chatList.add(ChatMessage(userText, true))
        chatAdapter.notifyItemInserted(chatList.size - 1)
        recyclerView.scrollToPosition(chatList.size - 1)

        messageInput.text.clear()
        askGeminiAI(userText)
    }

    private fun addBotMessage(message: String) {
        chatList.add(ChatMessage(message, false))
        chatAdapter.notifyItemInserted(chatList.size - 1)
        recyclerView.scrollToPosition(chatList.size - 1)
    }

    private fun askGeminiAI(userMessage: String) {
        chatList.add(ChatMessage("⏳ Bot sedang mengetik...", false))
        chatAdapter.notifyItemInserted(chatList.size - 1)
        recyclerView.scrollToPosition(chatList.size - 1)

        lifecycleScope.launch {
            try {
                val prompt = """
                    Kamu adalah asisten pertanian kelapa sawit bernama SawitMaju Bot.
                    Jawablah dengan bahasa sederhana, singkat, dan ramah petani.

                    Pertanyaan:
                    $userMessage
                """.trimIndent()

                val request = GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(Part(text = prompt))
                        )
                    )
                )

                val response = GeminiApiClient.apiService.generateContent(apiKey, request)

                if (response.error != null) {
                    throw Exception("API Error: ${response.error.message}")
                }

                val reply = response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()
                    ?.text?.trim()
                    ?: "🤖 Maaf, saya belum bisa menjawab."

                chatList.removeLast()
                chatAdapter.notifyItemRemoved(chatList.size)
                addBotMessage(reply)

            } catch (e: retrofit2.HttpException) {
                chatList.removeLast()
                chatAdapter.notifyItemRemoved(chatList.size)
                addBotMessage("❌ Error ${e.code()}: ${e.message()}")
                e.printStackTrace()
            } catch (e: Exception) {
                chatList.removeLast()
                chatAdapter.notifyItemRemoved(chatList.size)
                addBotMessage("❌ Terjadi kesalahan: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        waveAnimator?.cancel()
        timerAnimator?.cancel()
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
    }
}