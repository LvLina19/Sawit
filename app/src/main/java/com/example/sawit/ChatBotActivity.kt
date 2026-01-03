package com.example.sawit

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.model.ChatMessage
import com.example.sawit.model.GeminiApiClient
import com.example.sawit.utils.GeminiRequest
import com.example.sawit.utils.Content
import com.example.sawit.utils.Part
import kotlinx.coroutines.launch

class ChatBotActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var micButton: ImageButton

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var messageInput: EditText
    private lateinit var sendButton: ImageButton

    private val chatList = mutableListOf<ChatMessage>()
    private val apiKey = "AIzaSyCX8AblhVcpRofRnuUWyBR4MgHXrsLw1hE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.chatbot)

        // Atur soft input mode
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        // Setup Speech Recognizer
        setupSpeechRecognizer()

        // Inisialisasi Views
        setupViews()

        // Greeting awal bot
        addBotMessage("Halo 🌴 Saya SawitMaju Bot.\nSilakan tanya soal harga sawit, panen, atau kebun.")

        // Setup Send Button
        sendButton.setOnClickListener {
            val userText = messageInput.text.toString().trim()
            if (userText.isNotEmpty()) {
                sendMessage(userText)
            }
        }
    }

    private fun setupSpeechRecognizer() {
        micButton = findViewById(R.id.micButton)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(
                    SpeechRecognizer.RESULTS_RECOGNITION
                )
                if (!matches.isNullOrEmpty()) {
                    messageInput.setText(matches[0])
                }
            }

            override fun onError(error: Int) {
                Toast.makeText(
                    this@ChatBotActivity,
                    "Mic error: $error",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID")
        }

        micButton.setOnClickListener {
            speechRecognizer.startListening(speechIntent)
        }
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)

        chatAdapter = ChatAdapter(chatList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = chatAdapter
    }

    private fun sendMessage(userText: String) {
        // Tambah pesan user
        chatList.add(ChatMessage(userText, true))
        chatAdapter.notifyItemInserted(chatList.size - 1)
        recyclerView.scrollToPosition(chatList.size - 1)

        // Clear input
        messageInput.text.clear()

        // Kirim ke AI
        askGeminiAI(userText)
    }

    private fun addBotMessage(message: String) {
        chatList.add(ChatMessage(message, false))
        chatAdapter.notifyItemInserted(chatList.size - 1)
        recyclerView.scrollToPosition(chatList.size - 1)
    }

    private fun askGeminiAI(userMessage: String) {
        // Tampilkan "bot sedang mengetik…"
        chatList.add(ChatMessage("⏳ Bot sedang mengetik...", false))
        chatAdapter.notifyItemInserted(chatList.size - 1)
        recyclerView.scrollToPosition(chatList.size - 1)

        lifecycleScope.launch {
            try {
                // Buat prompt
                val prompt = """
                    Kamu adalah asisten pertanian kelapa sawit bernama SawitMaju Bot.
                    Jawablah dengan bahasa sederhana, singkat, dan ramah petani.

                    Pertanyaan:
                    $userMessage
                """.trimIndent()

                // Buat request body
                val request = GeminiRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(Part(text = prompt))
                        )
                    )
                )

                // Panggil API
                val response = GeminiApiClient.apiService.generateContent(apiKey, request)

                // Cek error dari API
                if (response.error != null) {
                    throw Exception("API Error: ${response.error.message}")
                }

                // Ambil jawaban
                val reply = response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()
                    ?.text?.trim()
                    ?: "🤖 Maaf, saya belum bisa menjawab."

                // Hapus "bot sedang mengetik…"
                chatList.removeLast()
                chatAdapter.notifyItemRemoved(chatList.size)

                // Tampilkan jawaban bot
                addBotMessage(reply)

            } catch (e: retrofit2.HttpException) {
                // HTTP Error (404, 500, dll)
                chatList.removeLast()
                chatAdapter.notifyItemRemoved(chatList.size)
                addBotMessage("❌ Error ${e.code()}: ${e.message()}")
                e.printStackTrace()
            } catch (e: Exception) {
                // Error lainnya
                chatList.removeLast()
                chatAdapter.notifyItemRemoved(chatList.size)
                addBotMessage("❌ Terjadi kesalahan: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}