package com.example.sawit.model

data class ProblemReport(
    val id: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val category: String = "", // "bug", "feature", "account", "other"
    val title: String = "",
    val description: String = "",
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val status: String = "pending", // "pending", "in_progress", "resolved", "closed"
    val screenshotUrls: List<String> = listOf(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
data class DeviceInfo(
    val model: String = "",
    val manufacturer: String = "",
    val osVersion: String = "",
    val appVersion: String = "",
    val screenResolution: String = ""
)