package com.example.sawit.model

import com.example.sawit.utils.GeminiRequest
import com.example.sawit.utils.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {

    // Gunakan gemini-pro yang lebih stabil
    @POST("v1beta/models/gemini-pro:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}