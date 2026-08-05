package com.example.yoursoundtrack.managers

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yoursoundtrack.dataModel.Album
import com.example.yoursoundtrack.dataModel.Artist
import com.example.yoursoundtrack.dataModel.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
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

    //real-time friends updates
    val friendsAllActivityState: StateFlow<List<Review>> = repository.getFriendsActivityFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val friendsListensState: StateFlow<List<Review>> = friendsAllActivityState
        .map { reviews -> reviews.filter { it.textReview.isBlank() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val friendsReviewsState: StateFlow<List<Review>> = friendsAllActivityState
        .map { reviews -> reviews.filter { it.textReview.isNotBlank() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleWantToListen(albumId: String, onResult: (Boolean) -> Unit) {
        repository.toggleWantToListen(albumId, onResult)
    }

    //save section
    fun saveReview(
        album: Album,
        rating: Float,
        textReview: String,
        isFavorited: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        repository.saveReview(album, rating, textReview, isFavorited, onComplete)
    }

    val lastListensState: StateFlow<List<Review>> = repository.getUserReviewsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteReviewsState: StateFlow<List<Review>> = repository.getUserFavoriteReviewsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _userReviews = MutableLiveData<List<Review>>()
    val userReviews: LiveData<List<Review>> = _userReviews

    fun loadUserReviews(userId: String) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("listens")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("MusicViewModel", "Error fetching listens", error)
                    return@addSnapshotListener
                }

                val reviewsList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Review::class.java)
                } ?: emptyList()

                _userReviews.value = reviewsList
            }
    }

    private val _favoriteArtists = MutableLiveData<List<Artist>>()
    val favoriteArtists: LiveData<List<Artist>> = _favoriteArtists

    fun loadFavoriteArtists(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val artistNamesOrIds = snapshot.get("favoriteArtists") as? List<String> ?: emptyList()
                if (artistNamesOrIds.isEmpty()) {
                    _favoriteArtists.value = emptyList()
                    return@addSnapshotListener
                }

                db.collection("artists")
                    .whereIn(FieldPath.documentId(), artistNamesOrIds.take(10))
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        val foundArtists = querySnapshot.documents.mapNotNull { doc ->
                            doc.toObject(Artist::class.java)
                        }

                        if (foundArtists.isEmpty()) {
                            _favoriteArtists.value = artistNamesOrIds.map { name ->
                                Artist(id = name, name = name)
                            }
                        } else {
                            _favoriteArtists.value = foundArtists
                        }
                    }
                    .addOnFailureListener {
                        _favoriteArtists.value = artistNamesOrIds.map { name ->
                            Artist(id = name, name = name)
                        }
                    }
            }
    }

    fun toggleFavoriteArtist(artistNameOrId: String, onResult: (Boolean) -> Unit = {}) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        userRef.get().addOnSuccessListener { snapshot ->
            val currentFavs = snapshot.get("favoriteArtists") as? List<String> ?: emptyList()
            val isFav = currentFavs.contains(artistNameOrId)

            val update = if (isFav) {
                FieldValue.arrayRemove(artistNameOrId)
            } else {
                FieldValue.arrayUnion(artistNameOrId)
            }

            userRef.update("favoriteArtists", update)
                .addOnSuccessListener {
                    loadFavoriteArtists(userId)
                    onResult(!isFav)
                }
                .addOnFailureListener {
                    val data = mapOf("favoriteArtists" to listOf(artistNameOrId))
                    userRef.set(data, SetOptions.merge())
                        .addOnSuccessListener {
                            loadFavoriteArtists(userId)
                            onResult(true)
                        }
                        .addOnFailureListener { onResult(false) }
                }
        }
    }

    private val _topFiveAlbums = MutableLiveData<List<Album>>()
    val topFiveAlbums: LiveData<List<Album>> = _topFiveAlbums

    fun loadTopFiveAlbums(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val savedIdsOrTitles = snapshot.get("topAlbumIds") as? List<String> ?: emptyList()
                if (savedIdsOrTitles.isEmpty()) {
                    _topFiveAlbums.value = emptyList()
                    return@addSnapshotListener
                }

                val allAlbums = allAlbumsState.value
                val matchedAlbums = savedIdsOrTitles.mapNotNull { query ->
                    allAlbums.find { album ->
                        album.id == query || album.title.equals(query, ignoreCase = true)
                    }
                }

                _topFiveAlbums.value = matchedAlbums
            }
    }
}