package com.example.sawit.utils

import com.google.gson.annotations.SerializedName

// Request Models
data class GeminiRequest(
    @SerializedName("contents")
    val contents: List<Content>
)

data class Content(
    @SerializedName("parts")
    val parts: List<Part>
)

data class Part(
    @SerializedName("text")
    val text: String
)