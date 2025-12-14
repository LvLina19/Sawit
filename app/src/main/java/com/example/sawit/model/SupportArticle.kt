package com.example.sawit.model
data class SupportArticle(
    val id: String = "",
    val category: String = "",
    val title: String = "",
    val content: String = "",
    val tags: List<String> = listOf(),
    val viewCount: Int = 0,
    val helpfulCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)