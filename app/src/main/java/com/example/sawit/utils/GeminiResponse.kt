package com.example.sawit.utils

import com.google.gson.annotations.SerializedName


// Response Models
data class GeminiResponse(
    @SerializedName("candidates")
    val candidates: List<Candidate>?,
    @SerializedName("error")
    val error: ErrorResponse?
)

data class Candidate(
    @SerializedName("content")
    val content: Content?,
    @SerializedName("finishReason")
    val finishReason: String?
)

data class ErrorResponse(
    @SerializedName("code")
    val code: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("status")
    val status: String
)
