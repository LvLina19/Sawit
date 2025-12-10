package com.example.sawit.model

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("playlistItems")
    fun getPlaylistVideos(
        @Query("part") part: String = "snippet,contentDetails",
        @Query("maxResults") maxResults: Int = 10,
        @Query("playlistId") playlistId: String,
        @Query("key") apiKey: String
    ): Call<YouTubePlaylistResponse>
}