package com.example.yoursoundtrack.managers

import android.content.Context
import android.util.Log
import com.example.yoursoundtrack.dataModel.Album
import com.example.yoursoundtrack.dataModel.Artist
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.InputStreamReader

private data class AlbumResponse(
    val albums: List<Album> = emptyList()
)

private data class ArtistResponse(
    val artists: List<Artist> = emptyList()
)

object DataLoader {

    private const val TAG = "DataLoader"

     //read albums json and update records in firebase
    suspend fun syncAlbumsFromAssetsToFirestore(
        context: Context,
        fileName: String = "albums.json",
        onComplete: ((successCount: Int, failureCount: Int) -> Unit)? = null
    ) = withContext(Dispatchers.IO) { //use dispatchers to not use the main thread
        val db = FirebaseFirestore.getInstance()
        val albumsCollection = db.collection("albums")

        try {
            val albumResponse = context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    Gson().fromJson(reader, AlbumResponse::class.java)
                }
            }

            val albums = albumResponse.albums
            if (albums.isEmpty()) {
                Log.w(TAG, "No albums found in $fileName")
                withContext(Dispatchers.Main) { onComplete?.invoke(0, 0) }
                return@withContext
            }

            Log.d(TAG, "Starting sync for ${albums.size} albums from assets...")

            var totalSuccess = 0
            var totalFailure = 0

            val chunks = albums.chunked(400)
            for (chunk in chunks) {
                val batch = db.batch()
                chunk.forEach { album ->
                    val docRef = if (album.id.isNotBlank()) {
                        albumsCollection.document(album.id)
                    } else {
                        albumsCollection.document()
                    }
                    batch.set(docRef, album, SetOptions.merge())
                }

                try {
                    batch.commit().await()
                    totalSuccess += chunk.size
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to commit batch: ${e.message}", e)
                    totalFailure += chunk.size
                }
            }

            withContext(Dispatchers.Main) {
                onComplete?.invoke(totalSuccess, totalFailure)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error reading or parsing $fileName from assets: ${e.message}", e)
            withContext(Dispatchers.Main) {
                onComplete?.invoke(0, 0)
            }
        }
    }

    //updates the artist collection
    suspend fun syncArtistsFromAssetsToFirestore(
        context: Context,
        fileName: String = "artists.json",
        onComplete: ((successCount: Int, failureCount: Int) -> Unit)? = null
    ) = withContext(Dispatchers.IO) {
        val db = FirebaseFirestore.getInstance()
        val artistsCollection = db.collection("artists")

        try {
            val artistResponse = context.assets.open(fileName).use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    Gson().fromJson(reader, ArtistResponse::class.java)
                }
            }

            val artists = artistResponse.artists
            if (artists.isEmpty()) {
                Log.w(TAG, "No artists found in $fileName")
                withContext(Dispatchers.Main) { onComplete?.invoke(0, 0) }
                return@withContext
            }

            Log.d(TAG, "Starting sync for ${artists.size} artists from $fileName...")

            var totalSuccess = 0
            var totalFailure = 0

            val chunks = artists.chunked(400)
            for (chunk in chunks) {
                val batch = db.batch()
                chunk.forEach { artist ->
                    val docRef = if (artist.id.isNotBlank()) {
                        artistsCollection.document(artist.id)
                    } else {
                        artistsCollection.document()
                    }
                    batch.set(docRef, artist, SetOptions.merge())
                }

                try {
                    batch.commit().await()
                    totalSuccess += chunk.size
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to commit artists batch: ${e.message}", e)
                    totalFailure += chunk.size
                }
            }

            withContext(Dispatchers.Main) {
                onComplete?.invoke(totalSuccess, totalFailure)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error reading or parsing $fileName from assets: ${e.message}", e)
            withContext(Dispatchers.Main) {
                onComplete?.invoke(0, 0)
            }
        }
    }
}