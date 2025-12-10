package com.example.sawit

data class YoutubeResponse(
    val items: List<YoutubeItem>
)

    data class YoutubeItem(
    val snippet: Snippet
)

data class Snippet(
    val title: String,
    val thumbnails: Thumbnails,
    val resourceId: ResourceId
)

data class Thumbnails(
    val medium: Thumbnail
)

data class Thumbnail(
    val url: String
)

data class ResourceId(
    val videoId: String
)
