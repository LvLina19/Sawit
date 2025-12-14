package com.example.sawit.IsiDashboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.IsiDashboard.GameActivity
import com.example.sawit.R
import com.example.sawit.adapter.VideoAdapter
import com.example.sawit.model.RetrofitClient
import com.example.sawit.model.YouTubePlaylistResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EdukasiFragment : Fragment() {

    private lateinit var rvVideos: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvErrorMessage: TextView
    private lateinit var videoAdapter: VideoAdapter

    // GANTI DENGAN API KEY ANDA
    private val API_KEY = "AIzaSyBLZ3nCR8V8Di2hyvUSaDzKCcQgQoIWW_c"
    private val PLAYLIST_ID = "PLoIys-gfSLGEFi73_Ao1eVAwYCqlD9420"

    // Game URLs
    private val GAME1_URL = "https://html5.gamedistribution.com/a57ef0ff6ec44d9889500893ba619b56/?gd_sdk_referrer_url=https://www.example.com/games/smart-farmer"
    private val GAME2_URL = "https://html5.gamedistribution.com/96ed50ddb0684b8d974a6bb760b912b9/?gd_sdk_referrer_url=https://gamedistribution.com/games/real-tractor-farmer/"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_edukasi_sawit, container, false)

        initViews(view)
        setupRecyclerView()
        setupClickListeners(view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Load videos after view is created
        loadYouTubeVideos()
    }

    private fun initViews(view: View) {
        rvVideos = view.findViewById(R.id.rvVideos)
        progressBar = view.findViewById(R.id.progressBarVideos)
        tvErrorMessage = view.findViewById(R.id.tvErrorMessage)
    }

    private fun setupRecyclerView() {
        videoAdapter = VideoAdapter(emptyList()) { videoId ->
            openYouTubeVideo(videoId)
        }

        rvVideos.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = videoAdapter
            // Important for nested scrolling in ScrollView
            isNestedScrollingEnabled = false
        }
    }

    private fun setupClickListeners(view: View) {
        // Back button
        view.findViewById<ImageView>(R.id.btnBack)?.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Article cards

        // Article cards - Open specific article directly
        view.findViewById<CardView>(R.id.cardArticle1)?.setOnClickListener {
            // Artikel tentang Dampak Kebijakan Pemerintah terhadap Harga TBS
            openArticleDirectly("https://swa.co.id/read/467043/gapki-menjabarkan-peluang-dan-tantangan-bisnis-cpo-di-2026")
        }

        view.findViewById<CardView>(R.id.cardArticle2)?.setOnClickListener {
            // Artikel tentang Tips Jaga Kualitas TBS
            openArticleDirectly("https://gapki.id/news/2025/12/03/gapki-tegaskan-industri-sawit-semakin-ramah-perempuan-dan-bebas-pekerja-anak/")
        }


        // Game cards - Navigate to GameActivity
        view.findViewById<CardView>(R.id.cardGame1)?.setOnClickListener {
            openGame(GAME1_URL, "Smart Farmer: Petualangan Petani Sawit")
        }

        view.findViewById<CardView>(R.id.cardGame2)?.setOnClickListener {
            openGame(GAME2_URL, "Harvest Hero: Teka-teki Kebun Sawit")
        }


        //berita
        view.findViewById<TextView>(R.id.btnLihatSemuaArticle)?.setOnClickListener {
            openNewsActivity()
        }
    }
    private fun openArticleDirectly(url: String) {
        // Option 1: Buka di browser
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//        startActivity(intent)

//         Option 2: Buka di WebView Activity (uncomment jika ingin pakai WebView)
         val intent = Intent(requireContext(), ArticleWebViewActivity::class.java)
         intent.putExtra("ARTICLE_URL", url)
         startActivity(intent)
    }
    private fun openNewsActivity() {
        val intent = Intent(requireContext(), NewsActivity::class.java)
        startActivity(intent)
    }
    private fun openGame(gameUrl: String, gameTitle: String) {
        val intent = Intent(requireContext(), GameActivity::class.java).apply {
            putExtra("GAME_URL", gameUrl)
            putExtra("GAME_TITLE", gameTitle)
        }
        startActivity(intent)
    }

    private fun loadYouTubeVideos() {
        showLoading(true)

        RetrofitClient.youtubeApi.getPlaylistVideos(
            playlistId = PLAYLIST_ID,
            apiKey = API_KEY
        ).enqueue(object : Callback<YouTubePlaylistResponse> {
            override fun onResponse(
                call: Call<YouTubePlaylistResponse>,
                response: Response<YouTubePlaylistResponse>
            ) {
                // Check if fragment is still attached
                if (!isAdded) return

                showLoading(false)

                if (response.isSuccessful) {
                    response.body()?.let { playlistResponse ->
                        if (playlistResponse.items.isNotEmpty()) {
                            videoAdapter.updateVideos(playlistResponse.items)
                            showError(false)
                        } else {
                            showError(true, "Tidak ada video yang ditemukan")
                        }
                    } ?: run {
                        showError(true, "Gagal memuat data video")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        400 -> "API Key tidak valid atau request bermasalah"
                        403 -> "API Key tidak memiliki akses. Periksa quota dan restrictions"
                        404 -> "Playlist tidak ditemukan"
                        else -> "Error: ${response.code()}"
                    }
                    showError(true, errorMessage)
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<YouTubePlaylistResponse>, t: Throwable) {
                // Check if fragment is still attached
                if (!isAdded) return

                showLoading(false)
                showError(true, "Koneksi gagal: ${t.message}")
                Toast.makeText(
                    requireContext(),
                    "Gagal memuat video: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun openYouTubeVideo(videoId: String) {
        // Try to open in YouTube app first, fallback to browser
        val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId"))
        val webIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.youtube.com/watch?v=$videoId")
        )

        try {
            startActivity(appIntent)
        } catch (ex: Exception) {
            startActivity(webIntent)
        }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        rvVideos.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showError(show: Boolean, message: String = "Gagal memuat video. Silakan coba lagi.") {
        tvErrorMessage.visibility = if (show) View.VISIBLE else View.GONE
        tvErrorMessage.text = message
        rvVideos.visibility = if (show) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up to prevent memory leaks
    }
}