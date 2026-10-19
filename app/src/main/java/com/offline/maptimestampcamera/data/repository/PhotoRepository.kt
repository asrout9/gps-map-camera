package com.offline.maptimestampcamera.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Environment
import com.offline.maptimestampcamera.data.local.dao.PhotoDao
import com.offline.maptimestampcamera.data.local.entity.PhotoEntity
import com.offline.maptimestampcamera.model.DateTimeData
import com.offline.maptimestampcamera.model.LocationData
import com.offline.maptimestampcamera.model.PhotoMetadata
import com.offline.maptimestampcamera.utils.BitmapUtils
import com.offline.maptimestampcamera.utils.ExifUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

class PhotoRepository(
    private val context: Context,
    private val photoDao: PhotoDao
) {
    val allPhotos: Flow<List<PhotoMetadata>> = photoDao.getAllPhotos().map { entities ->
        entities.map { it.toDomainModel() }
    }

    suspend fun getPhotoById(id: Long): PhotoMetadata? = withContext(Dispatchers.IO) {
        photoDao.getPhotoById(id)?.toDomainModel()
    }

    suspend fun savePhoto(
        mergedBitmap: Bitmap,
        locationData: LocationData,
        dateTimeData: DateTimeData,
        mapSnapshotPath: String,
        presetName: String,
        jpegQuality: Int = 90
    ): PhotoMetadata = withContext(Dispatchers.IO) {
        // Save to public Pictures/GPS Map Camera folder so it appears in Samsung Gallery & Google Photos
        val publicPicturesDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "GPS Map Camera"
        )
        if (!publicPicturesDir.exists()) {
            publicPicturesDir.mkdirs()
        }

        val filename = "IMG_${System.currentTimeMillis()}.jpg"
        val photoFile = File(publicPicturesDir, filename)

        // Save high-res JPEG bitmap
        BitmapUtils.saveBitmapToFile(mergedBitmap, photoFile, Bitmap.CompressFormat.JPEG, jpegQuality)

        // Save thumbnail in cache dir
        val thumbFile = File(context.cacheDir, "THUMB_$filename")
        val thumbBitmap = Bitmap.createScaledBitmap(
            mergedBitmap,
            300,
            (300 * (mergedBitmap.height.toFloat() / mergedBitmap.width)).toInt().coerceAtLeast(1),
            true
        )
        BitmapUtils.saveBitmapToFile(thumbBitmap, thumbFile, Bitmap.CompressFormat.JPEG, 80)

        // Write EXIF Metadata
        ExifUtils.writeExifMetadata(
            file = photoFile,
            locationData = locationData,
            timestampMs = dateTimeData.timestampMs
        )

        // Trigger MediaScanner so photo immediately shows in device system Gallery
        try {
            MediaScannerConnection.scanFile(
                context,
                arrayOf(photoFile.absolutePath),
                arrayOf("image/jpeg"),
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val entity = PhotoEntity(
            filePath = photoFile.absolutePath,
            thumbnailPath = thumbFile.absolutePath,
            mapSnapshotPath = mapSnapshotPath,
            latitude = locationData.latitude,
            longitude = locationData.longitude,
            address = locationData.address,
            dateFormatted = dateTimeData.getFormattedDate(),
            timeFormatted = dateTimeData.getFormattedTime(),
            timestampMs = dateTimeData.timestampMs,
            cameraModel = android.os.Build.MODEL,
            width = mergedBitmap.width,
            height = mergedBitmap.height,
            overlayPresetName = presetName
        )

        val id = photoDao.insertPhoto(entity)
        entity.copy(id = id).toDomainModel()
    }

    suspend fun deletePhoto(photoId: Long) = withContext(Dispatchers.IO) {
        val entity = photoDao.getPhotoById(photoId) ?: return@withContext
        try {
            File(entity.filePath).delete()
            File(entity.thumbnailPath).delete()

            MediaScannerConnection.scanFile(
                context,
                arrayOf(entity.filePath),
                arrayOf("image/jpeg"),
                null
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        photoDao.deletePhotoById(photoId)
    }

    private fun PhotoEntity.toDomainModel(): PhotoMetadata {
        return PhotoMetadata(
            id = id,
            filePath = filePath,
            thumbnailPath = thumbnailPath,
            mapSnapshotPath = mapSnapshotPath,
            latitude = latitude,
            longitude = longitude,
            address = address,
            dateFormatted = dateFormatted,
            timeFormatted = timeFormatted,
            timestampMs = timestampMs,
            cameraModel = cameraModel,
            width = width,
            height = height,
            overlayPresetName = overlayPresetName
        )
    }
}
