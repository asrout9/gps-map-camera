package com.offline.maptimestampcamera.di

import android.content.Context
import androidx.room.Room
import com.offline.maptimestampcamera.data.local.AppDatabase
import com.offline.maptimestampcamera.data.preferences.AppPreferencesRepository
import com.offline.maptimestampcamera.data.repository.LocationRepository
import com.offline.maptimestampcamera.data.repository.OverlayPresetRepository
import com.offline.maptimestampcamera.data.repository.PhotoRepository
import com.offline.maptimestampcamera.data.repository.SessionRepository

class AppContainer(private val context: Context) {

    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "map_timestamp_camera.db"
        ).fallbackToDestructiveMigration()
         .build()
    }

    val appPreferencesRepository: AppPreferencesRepository by lazy {
        AppPreferencesRepository(context.applicationContext)
    }

    val locationRepository: LocationRepository by lazy {
        LocationRepository(context.applicationContext, database.searchHistoryDao())
    }

    val photoRepository: PhotoRepository by lazy {
        PhotoRepository(context.applicationContext, database.photoDao())
    }

    val overlayPresetRepository: OverlayPresetRepository by lazy {
        OverlayPresetRepository(database.overlayPresetDao())
    }

    val sessionRepository: SessionRepository by lazy {
        SessionRepository()
    }
}
