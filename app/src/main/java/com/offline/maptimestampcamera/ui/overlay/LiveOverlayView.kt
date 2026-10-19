package com.offline.maptimestampcamera.ui.overlay

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offline.maptimestampcamera.data.preferences.UserPreferences
import com.offline.maptimestampcamera.model.DateTimeData
import com.offline.maptimestampcamera.model.LocationData
import com.offline.maptimestampcamera.model.OverlayConfig
import com.offline.maptimestampcamera.utils.LocationFormatter
import java.io.File

@Composable
fun LiveOverlayView(
    modifier: Modifier = Modifier,
    config: OverlayConfig,
    locationData: LocationData,
    dateTimeData: DateTimeData,
    mapSnapshotPath: String?,
    preferences: UserPreferences,
    deviceOrientation: Int = 0,
    selectedWidgetId: String? = null,
    onWidgetClick: ((String) -> Unit)? = null
) {
    val mapBitmap = remember(mapSnapshotPath) {
        if (!mapSnapshotPath.isNullOrEmpty() && File(mapSnapshotPath).exists()) {
            BitmapFactory.decodeFile(mapSnapshotPath)
        } else {
            null
        }
    }

    val normalizedOrientation = when (deviceOrientation) {
        in 45..134 -> 90
        in 225..314 -> 270
        else -> 0
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val cardHeight = 100.dp
        val cardWidth = 345.dp

        val transX = with(density) { (cardWidth.toPx() - cardHeight.toPx()) / 2f }

        val headlineTitle = remember(locationData) {
            val parts = mutableListOf<String>()
            val mainLoc = locationData.locality.ifEmpty { locationData.village.ifEmpty { locationData.district } }
            if (mainLoc.isNotEmpty()) parts.add(mainLoc)
            if (locationData.state.isNotEmpty() && locationData.state != mainLoc) parts.add(locationData.state)
            if (locationData.country.isNotEmpty()) parts.add(locationData.country)
            val text = parts.joinToString(", ")
            val flagStr = if (locationData.flagEmoji.isNotEmpty()) " ${locationData.flagEmoji}" else ""
            if (text.isNotEmpty()) "$text$flagStr" else "Location Selected$flagStr"
        }

        val formattedCoords = LocationFormatter.formatCoordinates(
            locationData.latitude,
            locationData.longitude,
            preferences.coordinateFormat
        )

        val coordsText = if (formattedCoords.first.endsWith("°") || formattedCoords.first.endsWith("N") || formattedCoords.first.endsWith("S")) {
            "Lat ${formattedCoords.first} Long ${formattedCoords.second}"
        } else {
            "Lat ${formattedCoords.first}° Long ${formattedCoords.second}°"
        }

        val fullAddressLine = remember(locationData) {
            val plus = locationData.plusCode.ifEmpty { "" }
            if (plus.isNotEmpty()) "$plus, ${locationData.address}" else locationData.address
        }

        val fullDateLine = remember(dateTimeData) {
            "${dateTimeData.getDayOfWeekName()}, ${dateTimeData.getFormattedShortDate()} ${dateTimeData.getFormattedTimeShort()} ${dateTimeData.getGmtOffsetString()}"
        }

        when (normalizedOrientation) {
            90 -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .graphicsLayer {
                            rotationZ = -90f
                            translationX = transX
                        }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .width(cardWidth)
                            .height(cardHeight)
                    ) {
                        MapThumbnailBox(mapBitmap = mapBitmap, size = cardHeight)
                        TextCardBox(
                            headlineTitle = headlineTitle,
                            fullAddressLine = fullAddressLine,
                            coordsText = coordsText,
                            fullDateLine = fullDateLine,
                            preferences = preferences,
                            height = cardHeight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            270 -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                        .graphicsLayer {
                            rotationZ = 90f
                            translationX = -transX
                        }
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .width(cardWidth)
                            .height(cardHeight)
                    ) {
                        MapThumbnailBox(mapBitmap = mapBitmap, size = cardHeight)
                        TextCardBox(
                            headlineTitle = headlineTitle,
                            fullAddressLine = fullAddressLine,
                            coordsText = coordsText,
                            fullDateLine = fullDateLine,
                            preferences = preferences,
                            height = cardHeight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .fillMaxWidth(0.98f)
                            .height(cardHeight)
                    ) {
                        MapThumbnailBox(mapBitmap = mapBitmap, size = cardHeight)
                        TextCardBox(
                            headlineTitle = headlineTitle,
                            fullAddressLine = fullAddressLine,
                            coordsText = coordsText,
                            fullDateLine = fullDateLine,
                            preferences = preferences,
                            height = cardHeight,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MapThumbnailBox(mapBitmap: Bitmap?, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.6f))
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (mapBitmap != null) {
            Image(
                bitmap = mapBitmap.asImageBitmap(),
                contentDescription = "Map Satellite",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Map", color = Color.White, fontFamily = FontFamily.SansSerif, fontSize = 11.sp)
            }
        }

        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location Pin",
            tint = Color.Red,
            modifier = Modifier.size(24.dp)
        )

        Text(
            text = "Google",
            color = Color.White,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Bold,
            fontSize = 9.5.sp,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp, bottom = 2.dp)
        )
    }
}

@Composable
private fun TextCardBox(
    headlineTitle: String,
    fullAddressLine: String,
    coordsText: String,
    fullDateLine: String,
    preferences: UserPreferences,
    height: Dp,
    modifier: Modifier = Modifier
) {
    val robotoFont = FontFamily.SansSerif

    Box(modifier = modifier) {
        // Main Translucent Text Card Box with Roboto Font
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.72f))
                .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 6.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Line 1: Larger Bold Roboto Address Headline
                Text(
                    text = headlineTitle,
                    color = Color.White,
                    fontFamily = robotoFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Line 2: Full Address / Plus Code in Roboto
                Text(
                    text = fullAddressLine,
                    color = Color.White,
                    fontFamily = robotoFont,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 11.5.sp
                )
                // Line 3: Lat & Long Coordinates in Roboto
                Text(
                    text = coordsText,
                    color = Color.White,
                    fontFamily = robotoFont,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    lineHeight = 11.5.sp
                )
                // Line 4: Date, Time & Timezone in Roboto
                Text(
                    text = fullDateLine,
                    color = Color.White,
                    fontFamily = robotoFont,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    lineHeight = 11.5.sp
                )
            }
        }

        // Watermark Badge in Roboto Font
        if (preferences.showWatermark) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = (-28).dp, x = (-4).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.88f))
                    .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(13.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF00BCD4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = preferences.watermarkText.ifEmpty { "GPS Map Camera" },
                        color = Color.White,
                        fontFamily = robotoFont,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
