package com.offline.maptimestampcamera.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.offline.maptimestampcamera.data.local.entity.OverlayPresetEntity
import com.offline.maptimestampcamera.data.local.entity.PhotoEntity
import com.offline.maptimestampcamera.data.local.entity.SearchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM photos ORDER BY timestampMs DESC")
    fun getAllPhotos(): Flow<List<PhotoEntity>>

    @Query("SELECT * FROM photos WHERE id = :id")
    suspend fun getPhotoById(id: Long): PhotoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PhotoEntity): Long

    @Delete
    suspend fun deletePhoto(photo: PhotoEntity)

    @Query("DELETE FROM photos WHERE id = :id")
    suspend fun deletePhotoById(id: Long)
}

@Dao
interface OverlayPresetDao {
    @Query("SELECT * FROM overlay_presets ORDER BY updatedAt DESC")
    fun getAllPresets(): Flow<List<OverlayPresetEntity>>

    @Query("SELECT * FROM overlay_presets WHERE id = :id")
    suspend fun getPresetById(id: String): OverlayPresetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: OverlayPresetEntity)

    @Query("DELETE FROM overlay_presets WHERE id = :id")
    suspend fun deletePresetById(id: String)
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY timestampMs DESC LIMIT 20")
    fun getRecentSearches(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearch(search: SearchHistoryEntity)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()
}
