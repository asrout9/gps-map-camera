package com.offline.maptimestampcamera.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.offline.maptimestampcamera.data.local.dao.OverlayPresetDao
import com.offline.maptimestampcamera.data.local.dao.PhotoDao
import com.offline.maptimestampcamera.data.local.dao.SearchHistoryDao
import com.offline.maptimestampcamera.data.local.entity.OverlayPresetEntity
import com.offline.maptimestampcamera.data.local.entity.PhotoEntity
import com.offline.maptimestampcamera.data.local.entity.SearchHistoryEntity

@Database(
    entities = [
        PhotoEntity::class,
        OverlayPresetEntity::class,
        SearchHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
    abstract fun overlayPresetDao(): OverlayPresetDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}
