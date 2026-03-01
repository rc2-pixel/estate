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
    val sessionId: String = "",
    val isMainPhoto: Boolean = true,
    // 土地情報
    val area: String = "",
    val landCategory: String = "",
    val frontage: String = "",
    val roadWidth: String = "",
    val roadDirection: String = "",
    // 建物情報
    val structure: String = "",
    val builtYear: String = "",
    val floors: String = "",
    val layout: String = "",
    val parking: String = "",
    val waterSupply: String = "",
    val sewage: String = "",
    // メモ
    val memo: String = "",
    val timestamp: Long
)
