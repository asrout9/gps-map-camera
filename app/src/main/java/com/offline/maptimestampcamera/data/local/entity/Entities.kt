package com.offline.maptimestampcamera.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val thumbnailPath: String,
    val mapSnapshotPath: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val dateFormatted: String,
    val timeFormatted: String,
    val timestampMs: Long,
    val cameraModel: String,
    val width: Int,
    val height: Int,
    val overlayPresetName: String
)

@Entity(tableName = "overlay_presets")
data class OverlayPresetEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val configJson: String,
    val isDefault: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val query: String,
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val timestampMs: Long = System.currentTimeMillis()
)
