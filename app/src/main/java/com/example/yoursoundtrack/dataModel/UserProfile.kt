package com.example.yoursoundtrack.dataModel

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val profilePictureUrl: String = "",
    val topAlbumIds: List<String> = emptyList(),
    val favoriteArtistIds: List<String> = emptyList(),
    val friendIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
