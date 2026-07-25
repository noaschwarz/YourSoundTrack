package com.example.yoursoundtrack.dataModel

data class Artist(
    val id: String = "",
    val name: String = "",
    val genre: String = "",
    val imageUrl: String = "",
    val albumIds: List<String> = emptyList()
)
