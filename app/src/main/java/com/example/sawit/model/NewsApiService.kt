package com.example.sawit.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {
    @GET("v2/everything")
    fun getNews(
        @Query("q") query: String,
        @Query("language") language: String = "id",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>
}