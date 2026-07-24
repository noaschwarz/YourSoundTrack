package com.example.yoursoundtrack.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yoursoundtrack.dataModel.Album
import com.example.yoursoundtrack.managers.MusicRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MusicViewModel : ViewModel() {

    private val repository = MusicRepository()

    val allAlbumsState: StateFlow<List<Album>> = repository.getAlbumsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // releases/ed in calander year
    val upcomingReleasesState: StateFlow<List<Album>> = allAlbumsState
        .map { albums ->
            albums.sortedByDescending { it.releaseYear }.take(6)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // top 6 sorted by rating
    val popularThisWeekState: StateFlow<List<Album>> = allAlbumsState
        .map { albums ->
            albums.sortedByDescending { it.avgRating }.take(6)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // see if in WTL
    val wantToListenIdsState: StateFlow<List<String>> = repository.getWantToListenAlbumIdsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // albums objects in WTL
    val wantToListenAlbumsState: StateFlow<List<Album>> = combine(
        allAlbumsState,
        wantToListenIdsState
    ) { albums, savedIds ->
        albums.filter { savedIds.contains(it.id) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // user rating
    val userRatingsState: StateFlow<Map<String, Float>> = repository.getUserRatingsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun toggleWantToListen(albumId: String, onResult: (Boolean) -> Unit) {
        repository.toggleWantToListen(albumId, onResult)
    }
}