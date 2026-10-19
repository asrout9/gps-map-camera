package com.offline.maptimestampcamera.model

import kotlinx.serialization.Serializable

@Serializable
data class PhotoMetadata(
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
    val overlayPresetName: String = "Default"
)
