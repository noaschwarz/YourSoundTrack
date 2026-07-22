package com.example.yoursoundtrack.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yoursoundtrack.dataModel.Album
import com.example.yoursoundtrack.managers.MusicRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MusicViewModel(
    private val repository: MusicRepository = MusicRepository()
) : ViewModel() {

    // Base flow of all albums from repository
    val allAlbumsState: StateFlow<List<Album>> = repository.getAlbumsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered flow: 2026 releases only (top 6)
    val upcomingReleasesState: StateFlow<List<Album>> = repository.getAlbumsFlow()
        .map { albums ->
            albums.filter { it.releaseYear == 2026 }.take(6)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Sorted flow: Popular this week sorted by avgRating (top 6)
    val popularThisWeekState: StateFlow<List<Album>> = repository.getAlbumsFlow()
        .map { albums ->
            albums.sortedByDescending { it.avgRating }.take(6)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}