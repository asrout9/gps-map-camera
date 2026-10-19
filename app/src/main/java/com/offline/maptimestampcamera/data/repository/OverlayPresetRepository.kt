package com.offline.maptimestampcamera.data.repository

import com.offline.maptimestampcamera.data.local.dao.OverlayPresetDao
import com.offline.maptimestampcamera.data.local.entity.OverlayPresetEntity
import com.offline.maptimestampcamera.model.OverlayConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class OverlayPresetRepository(
    private val presetDao: OverlayPresetDao
) {
    private val json = Json { ignoreUnknownKeys = true }

    val presets: Flow<List<OverlayConfig>> = presetDao.getAllPresets().map { entities ->
        if (entities.isEmpty()) {
            listOf(OverlayConfig.defaultWidgets().let { OverlayConfig() }, OverlayConfig.presetCardOverlay())
        } else {
            entities.mapNotNull { deserializeConfig(it.configJson) }
        }
    }

    suspend fun savePreset(config: OverlayConfig) = withContext(Dispatchers.IO) {
        val jsonStr = json.encodeToString(OverlayConfig.serializer(), config)
        val entity = OverlayPresetEntity(
            id = config.id,
            name = config.name,
            configJson = jsonStr,
            isDefault = config.isDefault,
            updatedAt = System.currentTimeMillis()
        )
        presetDao.insertPreset(entity)
    }

    suspend fun deletePreset(id: String) = withContext(Dispatchers.IO) {
        presetDao.deletePresetById(id)
    }

    private fun deserializeConfig(jsonStr: String): OverlayConfig? {
        return try {
            json.decodeFromString(OverlayConfig.serializer(), jsonStr)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
