package com.example.yoursoundtrack.dataModel

data class Album(
    val id: String = "",
    val title: String = "",
    val artist: String = "",
    val releaseYear: Int = 0,
    val genre: String = "",
    val coverUrl: String = "",
    val avgRating: Double = 0.0,
    val tracks: List<String> = emptyList()
)
