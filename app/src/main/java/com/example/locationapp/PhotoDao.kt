package com.example.locationapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Insert
    suspend fun insert(photo: Photo)

    @Insert
    suspend fun insertAll(photos: List<Photo>)

    @Delete
    suspend fun delete(photo: Photo)

    @Query("SELECT * FROM photos WHERE isMainPhoto = 1 ORDER BY timestamp DESC")
    fun getMainPhotos(): Flow<List<Photo>>

    @Query("SELECT * FROM photos WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getPhotosBySession(sessionId: String): List<Photo>

    @Query("SELECT * FROM photos ORDER BY timestamp DESC")
    fun getAllPhotos(): Flow<List<Photo>>
}
