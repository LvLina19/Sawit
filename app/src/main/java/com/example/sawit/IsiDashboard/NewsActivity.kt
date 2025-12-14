package com.example.sawit.IsiDashboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sawit.adapter.NewsAdapter
import com.example.sawit.model.NewsResponse
import com.example.sawit.model.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.example.sawit.R

class NewsActivity : AppCompatActivity() {

    private lateinit var rvNews: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var btnBack: ImageView
    private lateinit var newsAdapter: NewsAdapter

    // GANTI DENGAN API KEY ANDA dari https://newsapi.org/
    private val NEWS_API_KEY = "e902f10a1a8542b3b575f5b9d642d804"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news)

        initViews()
        setupRecyclerView()
        loadNews()
    }

    private fun initViews() {
        rvNews = findViewById(R.id.rvNews)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvErrorNews)
        btnBack = findViewById(R.id.btnBack)

        btnBack.setOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter(emptyList()) { newsUrl ->
            openNewsInBrowser(newsUrl)
        }

        rvNews.apply {
            layoutManager = LinearLayoutManager(this@NewsActivity)
            adapter = newsAdapter
        }
    }

    private fun loadNews() {
        showLoading(true)

        // Query untuk berita tentang sawit/kelapa sawit
        RetrofitClient.newsApi.getNews(
            query = "kelapa sawit OR palm oil OR sawit Indonesia",
            language = "id",
            apiKey = NEWS_API_KEY
        ).enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                showLoading(false)

                if (response.isSuccessful) {
                    response.body()?.let { newsResponse ->
                        if (newsResponse.articles.isNotEmpty()) {
                            newsAdapter.updateNews(newsResponse.articles)
                            showError(false)
                        } else {
                            showError(true, "Tidak ada berita ditemukan")
                        }
                    }
                } else {
                    val errorMsg = when (response.code()) {
                        401 -> "API Key tidak valid"
                        426 -> "Upgrade API Key diperlukan"
                        429 -> "Terlalu banyak request, coba lagi nanti"
                        else -> "Error: ${response.code()}"
                    }
                    showError(true, errorMsg)
                    Toast.makeText(this@NewsActivity, errorMsg, Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                showLoading(false)
                showError(true, "Gagal memuat berita: ${t.message}")
                Toast.makeText(
                    this@NewsActivity,
                    "Koneksi gagal: ${t.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun openNewsInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        rvNews.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showError(show: Boolean, message: String = "Gagal memuat berita") {
        tvError.visibility = if (show) View.VISIBLE else View.GONE
        tvError.text = message
        rvNews.visibility = if (show) View.GONE else View.VISIBLE
    }
}