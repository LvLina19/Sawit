package com.example.sawit.model
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val YOUTUBE_BASE_URL = "https://www.googleapis.com/youtube/v3/"
    private const val NEWS_BASE_URL = "https://newsapi.org/"

    // YouTube API
    private val youtubeRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(YOUTUBE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val youtubeApi: YouTubeApiService by lazy {
        youtubeRetrofit.create(YouTubeApiService::class.java)
    }

    // News API
    private val newsRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(NEWS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val newsApi: NewsApiService by lazy {
        newsRetrofit.create(NewsApiService::class.java)
    }
}