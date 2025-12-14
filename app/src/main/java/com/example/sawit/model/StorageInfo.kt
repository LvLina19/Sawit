package com.example.sawit.model

data class StorageInfo(
    val userId: String = "",
    val cacheSize: Long = 0, // in bytes
    val imagesSize: Long = 0,
    val videosSize: Long = 0,
    val documentsSize: Long = 0,
    val totalSize: Long = 0,
    val lastCalculated: Long = System.currentTimeMillis()
) {
    fun getCacheSizeString(): String {
        return formatFileSize(cacheSize)
    }

    fun getTotalSizeString(): String {
        return formatFileSize(totalSize)
    }

    private fun formatFileSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format("%.2f GB", gb)
            mb >= 1 -> String.format("%.2f MB", mb)
            kb >= 1 -> String.format("%.2f KB", kb)
            else -> "$size B"
        }
    }
}