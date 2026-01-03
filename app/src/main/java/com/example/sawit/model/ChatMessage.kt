package com.example.sawit.model

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val isTyping: Boolean = false

)
