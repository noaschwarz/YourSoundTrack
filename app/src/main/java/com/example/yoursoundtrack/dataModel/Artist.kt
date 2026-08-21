package com.example.yoursoundtrack.dataModel

data class Artist(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val bio: String = "",
    val genres: List<String> = emptyList(),
    val albumIds: List<String> = emptyList()
)