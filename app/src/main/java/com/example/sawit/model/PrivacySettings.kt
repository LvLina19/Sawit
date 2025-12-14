package com.example.sawit.model

data class PrivacySettings(
    val userId: String = "",
    val profileVisibility: String = "public", // "public", "friends", "private"
    val showOnlineStatus: Boolean = true,
    val showActivityHistory: Boolean = true,
    val allowFriendRequests: Boolean = true,
    val dataSharing: Boolean = false,
    val analyticsEnabled: Boolean = true
)