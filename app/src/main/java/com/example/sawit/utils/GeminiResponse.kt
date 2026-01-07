package com.example.sawit.utils

import com.google.gson.annotations.SerializedName

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: ContentResponse
)

data class ContentResponse(
    val parts: List<PartResponse>
)

data class PartResponse(
    val text: String
)

