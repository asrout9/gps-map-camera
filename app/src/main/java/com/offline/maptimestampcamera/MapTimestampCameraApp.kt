package com.offline.maptimestampcamera

import android.app.Application
import com.offline.maptimestampcamera.di.AppContainer
import org.osmdroid.config.Configuration
import java.io.File

class MapTimestampCameraApp : Application() {

    lateinit var appContainer: AppContainer

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)

        val osmConfig = Configuration.getInstance()
        val userAgent = "Mozilla/5.0 (Android; Mobile) OfflineMapTimestampCamera/1.0 (contact: admin@offlinemaptimestampcamera.org)"
        osmConfig.userAgentValue = userAgent
        osmConfig.osmdroidBasePath = File(cacheDir, "osmdroid")
        osmConfig.osmdroidTileCache = File(cacheDir, "osmdroid/tiles")
    }
}
