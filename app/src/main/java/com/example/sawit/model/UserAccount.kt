package com.example.sawit.model

data class UserAccount(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val isActive: Boolean = true,
    val isPrimary: Boolean = false,
    val lastLogin: Long = System.currentTimeMillis()
)