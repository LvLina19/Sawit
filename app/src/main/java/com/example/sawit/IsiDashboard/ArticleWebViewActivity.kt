package com.example.sawit.IsiDashboard

import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.sawit.R
class ArticleWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView
    private lateinit var tvTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_article_webview)

        initViews()
        setupWebView()
        loadArticle()
    }

    private fun initViews() {
        webView = findViewById(R.id.webViewArticle)
        progressBar = findViewById(R.id.progressBar)
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvArticleTitle)

        btnBack.setOnClickListener { finish() }
    }

    private fun setupWebView() {
        webView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                builtInZoomControls = true
                displayZoomControls = false
                loadWithOverviewMode = true
                useWideViewPort = true
            }

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = View.GONE
                    tvTitle.text = view?.title ?: "Artikel"
                }
            }
        }
    }

    private fun loadArticle() {
        val articleUrl = intent.getStringExtra("ARTICLE_URL")
        articleUrl?.let {
            progressBar.visibility = View.VISIBLE
            webView.loadUrl(it)
        } ?: finish()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
