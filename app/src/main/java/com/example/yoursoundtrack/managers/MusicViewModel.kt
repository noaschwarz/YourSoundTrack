package com.example.yoursoundtrack.managers

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.yoursoundtrack.dataModel.Album
import com.example.yoursoundtrack.dataModel.Artist
import com.example.yoursoundtrack.dataModel.Review
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MusicViewModel : ViewModel() { //brige from rep to ui

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

    //real-time friends updates
    val friendsAllActivityState: StateFlow<List<Review>> = repository.getFriendsActivityFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    //filter friends listens
    val friendsListensState: StateFlow<List<Review>> = friendsAllActivityState
        .map { reviews -> reviews.filter { it.textReview.isBlank() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    //filter friends reviews
    val friendsReviewsState: StateFlow<List<Review>> = friendsAllActivityState
        .map { reviews -> reviews.filter { it.textReview.isNotBlank() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    //last listens
    val lastListensState: StateFlow<List<Review>> = repository.getUserReviewsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    //fav reviewa
    val favoriteReviewsState: StateFlow<List<Review>> = repository.getFavoriteReviewsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val selectedUserId = MutableStateFlow("") //hold the user id

    //fetch reviews selected profile
    val userReviewsState: StateFlow<List<Review>> = selectedUserId
        .flatMapLatest { userId ->
            if (userId.isBlank()) MutableStateFlow(emptyList())
            else repository.getUserReviewsFlowByUserId(userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    //fetch fav artists selected profile
    val favoriteArtistsState: StateFlow<List<Artist>> = selectedUserId
        .flatMapLatest { userId ->
            if (userId.isBlank()) MutableStateFlow(emptyList())
            else repository.getFavoriteArtistsFlow(userId)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    // display top 5 albums
    val topFiveAlbumsState: StateFlow<List<Album>> = combine(
        allAlbumsState,
        selectedUserId.flatMapLatest { userId ->
            if (userId.isBlank()) MutableStateFlow(emptyList())
            else repository.getTopAlbumIdsFlow(userId)
        }
    ) { albums, savedQueries ->
        savedQueries.mapNotNull { query ->
            albums.find { album -> album.id == query || album.title.equals(query, ignoreCase = true) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Backwards Compatibility LiveData Bridges for Fragments ---
    val userReviews: LiveData<List<Review>> = userReviewsState.asLiveData()
    val favoriteArtists: LiveData<List<Artist>> = favoriteArtistsState.asLiveData()
    val topFiveAlbums: LiveData<List<Album>> = topFiveAlbumsState.asLiveData()

    fun loadUserReviews(userId: String) {
        selectedUserId.value = userId
    }

    fun loadFavoriteArtists(userId: String) {
        selectedUserId.value = userId
    }

    fun loadTopFiveAlbums(userId: String) {
        selectedUserId.value = userId
    }

    fun toggleWantToListen(albumId: String, onResult: (Boolean) -> Unit) {
        repository.toggleWantToListen(albumId, onResult)
    }

    //forward the review
    fun saveReview(
        album: Album,
        rating: Float,
        textReview: String,
        isFavorited: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        repository.saveReview(album, rating, textReview, isFavorited, onComplete)
    }

    fun toggleFavoriteArtist(artistNameOrId: String, onResult: (Boolean) -> Unit = {}) {
        repository.toggleFavoriteArtist(artistNameOrId, onResult)
    }

    val allArtistsState: StateFlow<List<Artist>> = repository.getArtistsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allArtists: LiveData<List<Artist>> = allArtistsState.asLiveData()

    //return the albums of an artist
    fun getAlbumsForArtist(
        artistId: String,
        artistName: String,
        albumIds: List<String> = emptyList()
    ): StateFlow<List<Album>> {
        return repository.getAlbumsByArtistFlow(artistId, artistName, albumIds)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }
}