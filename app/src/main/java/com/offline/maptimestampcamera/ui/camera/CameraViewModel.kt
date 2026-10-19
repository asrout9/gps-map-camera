package com.offline.maptimestampcamera.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.offline.maptimestampcamera.data.preferences.AppPreferencesRepository
import com.offline.maptimestampcamera.data.preferences.UserPreferences
import com.offline.maptimestampcamera.data.repository.PhotoRepository
import com.offline.maptimestampcamera.data.repository.SessionRepository
import com.offline.maptimestampcamera.model.DateTimeData
import com.offline.maptimestampcamera.model.LocationData
import com.offline.maptimestampcamera.model.OverlayConfig
import com.offline.maptimestampcamera.ui.overlay.render.OverlayBitmapRenderer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CameraViewModel(
    private val context: Context,
    private val sessionRepository: SessionRepository,
    private val photoRepository: PhotoRepository,
    private val appPreferencesRepository: AppPreferencesRepository
) : ViewModel() {

    val selectedLocation: StateFlow<LocationData> = sessionRepository.selectedLocation
    val selectedDateTime: StateFlow<DateTimeData> = sessionRepository.selectedDateTime
    val mapSnapshotPath: StateFlow<String?> = sessionRepository.mapSnapshotPath

    val userPreferences: StateFlow<UserPreferences> = appPreferencesRepository.preferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    val activeOverlayConfig: StateFlow<OverlayConfig> = sessionRepository.activeOverlayConfig

    private val _flashMode = MutableStateFlow(ImageCapture.FLASH_MODE_OFF)
    val flashMode: StateFlow<Int> = _flashMode.asStateFlow()

    private val _isLensFacingBack = MutableStateFlow(true)
    val isLensFacingBack: StateFlow<Boolean> = _isLensFacingBack.asStateFlow()

    private val _gridMode = MutableStateFlow(false)
    val gridMode: StateFlow<Boolean> = _gridMode.asStateFlow()

    private val _zoomRatio = MutableStateFlow(1.0f)
    val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

    private val _deviceOrientation = MutableStateFlow(0)
    val deviceOrientation: StateFlow<Int> = _deviceOrientation.asStateFlow()

    private val _isCapturing = MutableStateFlow(false)
    val isCapturing: StateFlow<Boolean> = _isCapturing.asStateFlow()

    fun toggleFlash() {
        _flashMode.value = when (_flashMode.value) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
    }

    fun toggleCamera() {
        _isLensFacingBack.value = !_isLensFacingBack.value
    }

    fun toggleCameraLens() = toggleCamera()

    fun toggleGrid() {
        _gridMode.value = !_gridMode.value
    }

    fun cycleGridMode() = toggleGrid()

    fun setZoomRatio(ratio: Float) {
        _zoomRatio.value = ratio
    }

    fun updateDeviceOrientation(orientationDegrees: Int) {
        _deviceOrientation.value = orientationDegrees
    }

    fun processCapturedImage(imageProxy: ImageProxy, onPhotoSaved: (Long) -> Unit) {
        _isCapturing.value = true
        val isBackCamera = _isLensFacingBack.value
        val orientation = _deviceOrientation.value

        viewModelScope.launch {
            try {
                val buffer = imageProxy.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)

                var baseBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                val matrix = Matrix()

                // 1. First rotate by EXIF sensor rotation degrees to achieve upright orientation
                if (rotationDegrees != 0) {
                    matrix.postRotate(rotationDegrees.toFloat())
                }

                // 2. For front camera selfies, mirror horizontally AFTER rotation (in upright space)
                if (!isBackCamera) {
                    matrix.postScale(-1f, 1f)
                }

                if (!matrix.isIdentity) {
                    baseBitmap = Bitmap.createBitmap(baseBitmap, 0, 0, baseBitmap.width, baseBitmap.height, matrix, true)
                }

                // 3. Handle device landscape rotation
                if ((orientation == 90 || orientation == 270) && baseBitmap.height > baseBitmap.width) {
                    val rotMatrix = Matrix()
                    rotMatrix.postRotate(if (orientation == 90) 90f else 270f)
                    baseBitmap = Bitmap.createBitmap(baseBitmap, 0, 0, baseBitmap.width, baseBitmap.height, rotMatrix, true)
                }

                val mergedBitmap = OverlayBitmapRenderer.renderOverlayOnBitmap(
                    context = context,
                    basePhoto = baseBitmap,
                    config = activeOverlayConfig.value,
                    locationData = selectedLocation.value,
                    dateTimeData = selectedDateTime.value,
                    mapSnapshotPath = mapSnapshotPath.value,
                    preferences = userPreferences.value,
                    deviceOrientation = orientation
                )

                val savedPhoto = photoRepository.savePhoto(
                    mergedBitmap = mergedBitmap,
                    locationData = selectedLocation.value,
                    dateTimeData = selectedDateTime.value,
                    mapSnapshotPath = mapSnapshotPath.value ?: "",
                    presetName = activeOverlayConfig.value.name,
                    jpegQuality = userPreferences.value.jpegQuality
                )

                _isCapturing.value = false
                onPhotoSaved(savedPhoto.id)
            } catch (e: Exception) {
                e.printStackTrace()
                _isCapturing.value = false
            } finally {
                imageProxy.close()
            }
        }
    }

    class Factory(
        private val context: Context,
        private val sessionRepository: SessionRepository,
        private val photoRepository: PhotoRepository,
        private val appPreferencesRepository: AppPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CameraViewModel(context, sessionRepository, photoRepository, appPreferencesRepository) as T
        }
    }
}
