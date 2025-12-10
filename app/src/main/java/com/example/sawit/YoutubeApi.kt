package com.example.sawit

import retrofit2.http.GET
import retrofit2.http.Query

interface YoutubeApi {
    @GET("playlistItems")
    suspend fun getPlaylistItems(
        @Query("part") part: String = "snippet",
        @Query("playlistId") playlistId: String,
        @Query("maxResults") max: Int = 20,
        @Query("key") apiKey: String
    ): YoutubeResponse
}
