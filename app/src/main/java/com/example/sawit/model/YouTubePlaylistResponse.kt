package com.example.sawit.model

// Response Models
data class YouTubePlaylistResponse(
    val items: List<PlaylistItem>
)

data class PlaylistItem(
    val snippet: Snippet,
    val contentDetails: ContentDetails
)

data class Snippet(
    val title: String,
    val description: String,
    val thumbnails: Thumbnails,
    val resourceId: ResourceId
)

data class Thumbnails(
    val default: Thumbnail,
    val medium: Thumbnail,
    val high: Thumbnail
)

data class Thumbnail(
    val url: String,
    val width: Int,
    val height: Int
)

data class ResourceId(
    val videoId: String
)

data class ContentDetails(
    val videoId: String
)