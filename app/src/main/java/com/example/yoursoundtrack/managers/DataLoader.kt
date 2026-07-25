package com.example.yoursoundtrack.managers

import android.content.Context
import android.util.Log
import com.example.yoursoundtrack.dataModel.Album
import com.example.yoursoundtrack.dataModel.Artist
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.gson.Gson
import java.io.InputStreamReader
import java.util.Locale

private data class AlbumResponse(
    val albums: List<Album> = emptyList()
)

object DataLoader {

    private const val TAG = "DataLoader"

    /**
     * Reads a JSON file from the assets folder and updates the records in the Firestore "albums" collection.
     */
    fun syncAlbumsFromAssetsToFirestore(
        context: Context,
        fileName: String = "albums.json",
        onComplete: ((successCount: Int, failureCount: Int) -> Unit)? = null
    ) {
        val db = FirebaseFirestore.getInstance()
        val albumsCollection = db.collection("albums")

        try {
            val inputStream = context.assets.open(fileName)
            val reader = InputStreamReader(inputStream)
            val albumResponse = Gson().fromJson(reader, AlbumResponse::class.java)
            reader.close()

            val albums = albumResponse.albums
            if (albums.isEmpty()) {
                Log.w(TAG, "No albums found in $fileName")
                onComplete?.invoke(0, 0)
                return
            }

            Log.d(TAG, "Starting sync for ${albums.size} albums from assets...")

            var batch = db.batch()
            var operationCount = 0
            var totalSuccess = 0
            var totalFailure = 0

            albums.forEachIndexed { index, album ->
                val docRef = albumsCollection.document(album.id)

                batch.set(docRef, album, SetOptions.merge())
                operationCount++

                if (operationCount == 400 || index == albums.size - 1) {
                    val currentBatch = batch
                    val countInThisBatch = operationCount

                    currentBatch.commit()
                        .addOnSuccessListener {
                            totalSuccess += countInThisBatch
                            Log.d(TAG, "Batch committed. Processed: ${totalSuccess + totalFailure}/${albums.size}")
                            if (totalSuccess + totalFailure == albums.size) {
                                onComplete?.invoke(totalSuccess, totalFailure)
                            }
                        }
                        .addOnFailureListener { e ->
                            totalFailure += countInThisBatch
                            Log.e(TAG, "Failed to commit batch: ${e.message}", e)
                            if (totalSuccess + totalFailure == albums.size) {
                                onComplete?.invoke(totalSuccess, totalFailure)
                            }
                        }

                    batch = db.batch()
                    operationCount = 0
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error reading or parsing $fileName from assets: ${e.message}", e)
            onComplete?.invoke(0, 0)
        }
    }

     //updates the artist collection
    fun syncArtistsFromAlbumsToFirestore(
        context: Context,
        fileName: String = "albums.json",
        onComplete: ((successCount: Int, failureCount: Int) -> Unit)? = null
    ) {
        val db = FirebaseFirestore.getInstance()
        val artistsCollection = db.collection("artists")

        try {
            val inputStream = context.assets.open(fileName)
            val reader = InputStreamReader(inputStream)
            val albumResponse = Gson().fromJson(reader, AlbumResponse::class.java)
            reader.close()

            val albums = albumResponse.albums
            if (albums.isEmpty()) {
                Log.w(TAG, "No albums found in $fileName")
                onComplete?.invoke(0, 0)
                return
            }

            val artistMap = mutableMapOf<String, Artist>()

            albums.forEach { album ->
                val artistName = album.artist?.trim()
                if (!artistName.isNullOrEmpty()) {
                    val artistId = artistName.lowercase(Locale.ROOT)
                        .replace("[^a-z0-9_]".toRegex(), "_")

                    val existingArtist = artistMap[artistId]
                    val updatedAlbumIds = (existingArtist?.albumIds ?: emptyList()) + album.id

                    artistMap[artistId] = Artist(
                        id = artistId,
                        name = artistName,
                        genre = album.genre ?: "",
                        imageUrl = album.coverUrl ?: "",
                        albumIds = updatedAlbumIds.distinct()
                    )
                }
            }

            val artistsList = artistMap.values.toList()
            Log.d(TAG, "Extracted ${artistsList.size} unique artists from $fileName")

            var batch = db.batch()
            var operationCount = 0
            var totalSuccess = 0
            var totalFailure = 0

            artistsList.forEachIndexed { index, artist ->
                val docRef = artistsCollection.document(artist.id)

                batch.set(docRef, artist, SetOptions.merge())
                operationCount++

                if (operationCount == 400 || index == artistsList.size - 1) {
                    val currentBatch = batch
                    val countInThisBatch = operationCount

                    currentBatch.commit()
                        .addOnSuccessListener {
                            totalSuccess += countInThisBatch
                            Log.d(TAG, "Artists batch committed. Processed: ${totalSuccess + totalFailure}/${artistsList.size}")
                            if (totalSuccess + totalFailure == artistsList.size) {
                                onComplete?.invoke(totalSuccess, totalFailure)
                            }
                        }
                        .addOnFailureListener { e ->
                            totalFailure += countInThisBatch
                            Log.e(TAG, "Failed to commit artists batch: ${e.message}", e)
                            if (totalSuccess + totalFailure == artistsList.size) {
                                onComplete?.invoke(totalSuccess, totalFailure)
                            }
                        }

                    batch = db.batch()
                    operationCount = 0
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error syncing artists from $fileName: ${e.message}", e)
            onComplete?.invoke(0, 0)
        }
    }
}