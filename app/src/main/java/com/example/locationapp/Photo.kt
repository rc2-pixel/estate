package com.example.locationapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val filePath: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val memo: String = "",
    val timestamp: Long
)
