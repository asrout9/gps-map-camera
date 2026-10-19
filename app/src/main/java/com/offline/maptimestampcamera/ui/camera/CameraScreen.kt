package com.offline.maptimestampcamera.ui.camera

import android.hardware.SensorManager
import android.view.OrientationEventListener
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.offline.maptimestampcamera.ui.overlay.LiveOverlayView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit,
    onOpenOverlayEditor: () -> Unit,
    onOpenLocationPicker: () -> Unit,
    onPhotoCaptured: (Long) -> Unit,
    onOpenGallery: () -> Unit,
    viewModel: CameraViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val locationData by viewModel.selectedLocation.collectAsState()
    val dateTimeData by viewModel.selectedDateTime.collectAsState()
    val mapSnapshotPath by viewModel.mapSnapshotPath.collectAsState()
    val overlayConfig by viewModel.activeOverlayConfig.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()

    val flashMode by viewModel.flashMode.collectAsState()
    val isLensFacingBack by viewModel.isLensFacingBack.collectAsState()
    val gridMode by viewModel.gridMode.collectAsState()
    val zoomRatio by viewModel.zoomRatio.collectAsState()
    val deviceOrientation by viewModel.deviceOrientation.collectAsState()
    val isCapturing by viewModel.isCapturing.collectAsState()

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var activeCamera: Camera? by remember { mutableStateOf(null) }
    var previewViewRef by remember { mutableStateOf<PreviewView?>(null) }

    // Accelerometer Orientation Listener (Updates on 90-degree boundary threshold)
    DisposableEffect(context, lifecycleOwner) {
        val orientationEventListener = object : OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return
                val degrees = when (orientation) {
                    in 45..134 -> 90
                    in 135..224 -> 180
                    in 225..314 -> 270
                    else -> 0
                }
                if (degrees != viewModel.deviceOrientation.value) {
                    viewModel.updateDeviceOrientation(degrees)
                }
            }
        }
        if (orientationEventListener.canDetectOrientation()) {
            orientationEventListener.enable()
        }
        onDispose {
            orientationEventListener.disable()
        }
    }

    // Re-bind CameraX when front/back camera lens toggle changes
    LaunchedEffect(isLensFacingBack, previewViewRef) {
        val pView = previewViewRef ?: return@LaunchedEffect
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(pView.surfaceProvider)
                }

                val capture = ImageCapture.Builder()
                    .setFlashMode(flashMode)
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()
                imageCapture = capture

                val cameraSelector = if (isLensFacingBack) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                }

                cameraProvider.unbindAll()
                val boundCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    capture
                )
                activeCamera = boundCamera

                // Re-apply zoom ratio
                val zoomState = boundCamera.cameraInfo.zoomState.value
                val minZoom = zoomState?.minZoomRatio ?: 0.5f
                val maxZoom = zoomState?.maxZoomRatio ?: 10.0f
                val safeRatio = zoomRatio.coerceIn(minZoom, maxZoom)
                boundCamera.cameraControl.setZoomRatio(safeRatio)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Dynamically update Flash Mode on ImageCapture
    LaunchedEffect(flashMode, imageCapture) {
        imageCapture?.flashMode = flashMode
    }

    // Update Zoom Ratio on active camera
    LaunchedEffect(zoomRatio, activeCamera) {
        activeCamera?.let { camera ->
            try {
                val zoomState = camera.cameraInfo.zoomState.value
                val minZoom = zoomState?.minZoomRatio ?: 0.5f
                val maxZoom = zoomState?.maxZoomRatio ?: 10.0f
                val safeRatio = zoomRatio.coerceIn(minZoom, maxZoom)
                camera.cameraControl.setZoomRatio(safeRatio)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("GPS Map Camera", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Flash Mode Toggle Button
                    IconButton(onClick = { viewModel.toggleFlash() }) {
                        val icon = when (flashMode) {
                            ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                            ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto
                            else -> Icons.Default.FlashOff
                        }
                        val tint = if (flashMode != ImageCapture.FLASH_MODE_OFF) Color(0xFFFFC107) else Color.White
                        Icon(icon, contentDescription = "Flash Mode", tint = tint)
                    }
                    // Grid Mode Toggle Button
                    IconButton(onClick = { viewModel.cycleGridMode() }) {
                        val icon = if (gridMode) Icons.Default.GridOn else Icons.Default.GridOff
                        val tint = if (gridMode) Color(0xFFFFC107) else Color.White
                        Icon(icon, contentDescription = "Grid Lines", tint = tint)
                    }
                    // Front / Back Camera Lens Switch Button
                    IconButton(onClick = { viewModel.toggleCameraLens() }) {
                        Icon(Icons.Default.Cameraswitch, contentDescription = "Flip Camera", tint = Color.White)
                    }
                    // Settings / Overlay Editor Button
                    IconButton(onClick = onOpenOverlayEditor) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
        ) {
            // 1. Center Camera Viewfinder Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        PreviewView(ctx).also {
                            previewViewRef = it
                        }
                    },
                    update = { view ->
                        previewViewRef = view
                    }
                )

                // Grid Lines Overlay (Rule of Thirds)
                if (gridMode) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val lineColor = Color.White.copy(alpha = 0.45f)
                        val strokeWidth = 1.dp.toPx()

                        // Vertical lines
                        drawLine(lineColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth)
                        drawLine(lineColor, Offset(2 * w / 3f, 0f), Offset(2 * w / 3f, h), strokeWidth)

                        // Horizontal lines
                        drawLine(lineColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth)
                        drawLine(lineColor, Offset(0f, 2 * h / 3f), Offset(w, 2 * h / 3f), strokeWidth)
                    }
                }

                // GPS Map Camera Live Overlay
                LiveOverlayView(
                    modifier = Modifier.fillMaxSize(),
                    config = overlayConfig,
                    locationData = locationData,
                    dateTimeData = dateTimeData,
                    mapSnapshotPath = mapSnapshotPath,
                    preferences = userPreferences,
                    deviceOrientation = deviceOrientation
                )
            }

            // 2. Bottom Camera Controls Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Zoom Ratio Level Bar (0.5x Wide, 1.0x, 2.0x)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    val zoomLevels = listOf(0.5f to "0.5x", 1.0f to "1x", 2.0f to "2x")
                    zoomLevels.forEach { (ratio, label) ->
                        val isSelected = zoomRatio == ratio
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) Color(0xFFFFC107) else Color(0xFF333333))
                                .clickable { viewModel.setZoomRatio(ratio) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.Black else Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Shutter Button Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Gallery Button
                    IconButton(
                        onClick = onOpenGallery,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF222222), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            tint = Color.White
                        )
                    }

                    // Main Capture Button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(4.dp, Color(0xFFFFC107), CircleShape)
                            .clickable(enabled = !isCapturing) {
                                val capture = imageCapture ?: return@clickable
                                capture.takePicture(
                                    ContextCompat.getMainExecutor(context),
                                    object : ImageCapture.OnImageCapturedCallback() {
                                        override fun onCaptureSuccess(image: androidx.camera.core.ImageProxy) {
                                            viewModel.processCapturedImage(image) { photoId ->
                                                onPhotoCaptured(photoId)
                                            }
                                        }

                                        override fun onError(exception: ImageCaptureException) {
                                            exception.printStackTrace()
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCapturing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(36.dp),
                                color = Color.Black,
                                strokeWidth = 3.dp
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }

                    // Pick Location Button
                    IconButton(
                        onClick = onOpenLocationPicker,
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF222222), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location Selection",
                            tint = Color(0xFFFFC107)
                        )
                    }
                }
            }
        }
    }
}
