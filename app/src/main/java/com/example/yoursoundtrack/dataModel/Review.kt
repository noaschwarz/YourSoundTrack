package com.example.yoursoundtrack.dataModel

data class Review(
    val id: String = "",
    val userId: String = "",
    val albumId: String = "",
    val albumTitle: String = "",
    val albumArtist: String = "",
    val albumCoverUrl: String = "",
    val rating: Float = 0f,
    val textReview: String = "",
    val isFavorited: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
