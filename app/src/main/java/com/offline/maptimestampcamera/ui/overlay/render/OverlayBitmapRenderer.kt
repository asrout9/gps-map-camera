package com.offline.maptimestampcamera.ui.overlay.render

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import com.offline.maptimestampcamera.data.preferences.UserPreferences
import com.offline.maptimestampcamera.model.DateTimeData
import com.offline.maptimestampcamera.model.LocationData
import com.offline.maptimestampcamera.model.OverlayConfig
import com.offline.maptimestampcamera.utils.BitmapUtils
import com.offline.maptimestampcamera.utils.LocationFormatter
import java.io.File

object OverlayBitmapRenderer {

    fun renderOverlayOnBitmap(
        context: Context,
        basePhoto: Bitmap,
        config: OverlayConfig,
        locationData: LocationData,
        dateTimeData: DateTimeData,
        mapSnapshotPath: String?,
        preferences: UserPreferences,
        deviceOrientation: Int = 0
    ): Bitmap {
        val width = basePhoto.width
        val height = basePhoto.height

        val merged = basePhoto.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(merged)

        val mapBitmap = if (!mapSnapshotPath.isNullOrEmpty() && File(mapSnapshotPath).exists()) {
            BitmapFactory.decodeFile(mapSnapshotPath)
        } else {
            BitmapUtils.createPlaceholderMapBitmap(300, 300, "Map Satellite")
        }

        val isLandscape = width > height || deviceOrientation == 90 || deviceOrientation == 270

        drawGpsMapCameraOverlay(
            canvas = canvas,
            canvasWidth = width,
            canvasHeight = height,
            isLandscape = isLandscape,
            locationData = locationData,
            dateTimeData = dateTimeData,
            mapBitmap = mapBitmap,
            preferences = preferences
        )

        return merged
    }

    private fun drawGpsMapCameraOverlay(
        canvas: Canvas,
        canvasWidth: Int,
        canvasHeight: Int,
        isLandscape: Boolean,
        locationData: LocationData,
        dateTimeData: DateTimeData,
        mapBitmap: Bitmap,
        preferences: UserPreferences
    ) {
        // Match the live preview proportions exactly.
        // Live preview uses a 360dp-wide viewport reference with:
        //   cardHeight = 100dp, map = 100dp square, spacing = 5dp, cornerRadius = 12dp
        //   headline = 14sp, body = 9.5sp
        //   card fills 98% of width, padding 10dp horizontal
        // We scale from the canvas width to match these dp proportions.
        val dpScale = canvasWidth / 360f  // 1dp in live preview = dpScale pixels in captured image

        val mapSize = 100f * dpScale
        val spacing = 5f * dpScale
        val cardHeight = mapSize  // Same as map: 100dp
        val cornerRadius = 12f * dpScale

        val bottomMargin = 10f * dpScale
        val overlayBottom = canvasHeight - bottomMargin
        val overlayTop = overlayBottom - cardHeight

        // Total overlay width = 98% of canvas width (matching Modifier.fillMaxWidth(0.98f))
        val totalWidth = canvasWidth * 0.98f
        val startX = (canvasWidth - totalWidth) / 2f
        val cardWidth = totalWidth - mapSize - spacing

        // Roboto Typefaces (same as live preview's FontFamily.SansSerif)
        val robotoBold = Typeface.create("sans-serif", Typeface.BOLD)
        val robotoRegular = Typeface.create("sans-serif", Typeface.NORMAL)

        // --- A. Draw Map Thumbnail ---
        val mapRect = RectF(startX, overlayTop, startX + mapSize, overlayBottom)

        canvas.save()
        val clipPath = Path().apply {
            addRoundRect(mapRect, cornerRadius, cornerRadius, Path.Direction.CW)
        }
        canvas.clipPath(clipPath)

        // Map background (60% black fallback, matching live preview)
        val mapBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#99000000")
            style = Paint.Style.FILL
        }
        canvas.drawRect(mapRect, mapBgPaint)
        canvas.drawBitmap(mapBitmap, null, mapRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))

        // "Google" label (9.5sp bold, bottom-left, matching live preview)
        val googlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 9.5f * dpScale
            typeface = robotoBold
        }
        canvas.drawText("Google", mapRect.left + 4f * dpScale, mapRect.bottom - 4f * dpScale - googlePaint.descent(), googlePaint)
        canvas.restore()

        // Map border (drawn after restore so it's not clipped, 35% white, 1dp stroke)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#59FFFFFF")
            style = Paint.Style.STROKE
            strokeWidth = 1f * dpScale
        }
        canvas.drawRoundRect(mapRect, cornerRadius, cornerRadius, borderPaint)

        // Location pin icon (teardrop shape matching Icons.Default.LocationOn, 24dp, centered)
        val pinSize = 24f * dpScale
        val centerX = mapRect.centerX()
        val centerY = mapRect.centerY()

        // Draw teardrop-shaped location pin
        val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
        val pinPath = Path()
        val pinTopRadius = pinSize * 0.38f  // Upper circle radius
        val pinTipY = centerY + pinSize * 0.48f  // Bottom point of teardrop
        val pinCenterY = centerY - pinSize * 0.08f  // Center of the circle part

        // Draw circle part of pin
        pinPath.addCircle(centerX, pinCenterY, pinTopRadius, Path.Direction.CW)

        // Draw triangle/point at bottom
        val trianglePath = Path()
        trianglePath.moveTo(centerX - pinTopRadius * 0.72f, pinCenterY + pinTopRadius * 0.55f)
        trianglePath.lineTo(centerX, pinTipY)
        trianglePath.lineTo(centerX + pinTopRadius * 0.72f, pinCenterY + pinTopRadius * 0.55f)
        trianglePath.close()
        pinPath.op(trianglePath, Path.Op.UNION)

        canvas.drawPath(pinPath, pinPaint)

        // White inner dot on pin (matching the LocationOn icon hole)
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(centerX, pinCenterY, pinTopRadius * 0.35f, dotPaint)

        // --- B. Draw Text Card Container ---
        val cardLeft = mapRect.right + spacing
        val cardRect = RectF(cardLeft, overlayTop, cardLeft + cardWidth, overlayBottom)

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B8000000")  // 72% black matching live preview
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(cardRect, cornerRadius, cornerRadius, bgPaint)

        // --- C. Draw Watermark Badge ---
        if (preferences.showWatermark) {
            val watermarkText = preferences.watermarkText.ifEmpty { "GPS Map Camera" }

            // Watermark text: 9sp bold (matching live preview)
            val wmTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 9f * dpScale
                typeface = robotoBold
            }
            val wmTextWidth = wmTextPaint.measureText(watermarkText)
            val iconBoxSize = 13f * dpScale  // 13dp icon box in live preview
            val wmPadX = 6f * dpScale  // 6dp horizontal padding in live preview
            val wmSpacing = 4f * dpScale  // 4dp spacer between icon and text
            val wmBadgeWidth = wmPadX + iconBoxSize + wmSpacing + wmTextWidth + wmPadX
            val wmBadgeHeight = (iconBoxSize + 2f * 2.5f * dpScale)  // padding top+bottom 2.5dp each

            // Position: badge bottom edge sits exactly at card top (matching reference image)
            val wmRight = cardRect.right - 4f * dpScale
            val wmTop = cardRect.top - wmBadgeHeight
            val wmLeft = wmRight - wmBadgeWidth
            val wmBottom = wmTop + wmBadgeHeight

            // Badge background: 88% black matching live preview Color.Black.copy(alpha = 0.88f)
            val wmBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E0000000")
                style = Paint.Style.FILL
            }
            val wmRect = RectF(wmLeft, wmTop, wmRight, wmBottom)
            canvas.drawRoundRect(wmRect, 6f * dpScale, 6f * dpScale, wmBgPaint)

            // Badge border: 20% white matching live preview
            val wmBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#33FFFFFF")
                style = Paint.Style.STROKE
                strokeWidth = 0.5f * dpScale
            }
            canvas.drawRoundRect(wmRect, 6f * dpScale, 6f * dpScale, wmBorderPaint)

            // Cyan icon box (13dp, cornerRadius 3dp, Color #00BCD4)
            val iconLeft = wmLeft + wmPadX
            val iconTop = wmTop + (wmBadgeHeight - iconBoxSize) / 2f
            val iconRect = RectF(iconLeft, iconTop, iconLeft + iconBoxSize, iconTop + iconBoxSize)
            val cyanPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00BCD4")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(iconRect, 3f * dpScale, 3f * dpScale, cyanPaint)

            // Camera icon inside cyan box (simplified)
            val cameraBodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            val camW = 9f * dpScale * 0.7f
            val camH = 6f * dpScale * 0.7f
            val camRect = RectF(
                iconRect.centerX() - camW / 2f,
                iconRect.centerY() - camH / 2f + 0.5f * dpScale,
                iconRect.centerX() + camW / 2f,
                iconRect.centerY() + camH / 2f + 0.5f * dpScale
            )
            canvas.drawRoundRect(camRect, 1f * dpScale, 1f * dpScale, cameraBodyPaint)
            val camLensPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#00BCD4")
                style = Paint.Style.FILL
            }
            canvas.drawCircle(iconRect.centerX(), iconRect.centerY() + 0.5f * dpScale, 1.5f * dpScale, camLensPaint)

            // Watermark text
            val textBaseline = wmBottom - (wmBadgeHeight - wmTextPaint.textSize) / 2f - wmTextPaint.descent()
            canvas.drawText(watermarkText, iconRect.right + wmSpacing, textBaseline, wmTextPaint)
        }

        // --- D. Render Text Lines (matching live preview exactly) ---
        val headlineParts = mutableListOf<String>()
        val mainLoc = locationData.locality.ifEmpty { locationData.village.ifEmpty { locationData.district } }
        if (mainLoc.isNotEmpty()) headlineParts.add(mainLoc)
        if (locationData.state.isNotEmpty() && locationData.state != mainLoc) headlineParts.add(locationData.state)
        if (locationData.country.isNotEmpty()) headlineParts.add(locationData.country)

        val headlineText = (if (headlineParts.isNotEmpty()) headlineParts.joinToString(", ") else "Location Selected") +
                (if (locationData.flagEmoji.isNotEmpty()) " ${locationData.flagEmoji}" else "")

        val plus = locationData.plusCode.ifEmpty { "" }
        val addressText = if (plus.isNotEmpty()) "$plus, ${locationData.address}" else locationData.address

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

        val dateTimeText = "${dateTimeData.getDayOfWeekName()}, ${dateTimeData.getFormattedShortDate()} ${dateTimeData.getFormattedTimeShort()} ${dateTimeData.getGmtOffsetString()}"

        // Text sizes matching live preview sp values
        // Live preview: headline = 14sp, body = 9.5sp
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 14f * dpScale
            typeface = robotoBold
        }

        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 9.5f * dpScale
            typeface = robotoRegular
        }

        // Padding matching live preview: start=10dp, end=10dp, top=6dp, bottom=6dp
        val paddingX = 10f * dpScale
        val paddingTop = 6f * dpScale

        // Line spacing matching live preview: verticalArrangement = spacedBy(1dp), lineHeight = 11.5sp for body
        val lineSpacing = 1f * dpScale
        val bodyLineHeight = 11.5f * dpScale

        var currentY = cardRect.top + paddingTop + titlePaint.textSize

        // Line 1: Bold headline (14sp, matching live preview)
        val maxTextWidth = cardRect.width() - 2 * paddingX
        canvas.drawText(truncateText(headlineText, titlePaint, maxTextWidth), cardRect.left + paddingX, currentY, titlePaint)
        currentY += lineSpacing + bodyLineHeight + (bodyPaint.textSize - bodyPaint.descent())

        // Line 2: Address (9.5sp regular, matching live preview, max 2 lines)
        val addressLines = wrapText(addressText, bodyPaint, maxTextWidth, maxLines = 2)
        for (line in addressLines) {
            canvas.drawText(line, cardRect.left + paddingX, currentY, bodyPaint)
            currentY += bodyLineHeight
        }

        // Line 3: Coordinates (9.5sp regular)
        canvas.drawText(truncateText(coordsText, bodyPaint, maxTextWidth), cardRect.left + paddingX, currentY, bodyPaint)
        currentY += bodyLineHeight

        // Line 4: Date/Time (9.5sp regular)
        canvas.drawText(truncateText(dateTimeText, bodyPaint, maxTextWidth), cardRect.left + paddingX, currentY, bodyPaint)
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float, maxLines: Int): List<String> {
        if (paint.measureText(text) <= maxWidth) return listOf(text)
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
                if (lines.size == maxLines - 1) break
            }
        }
        if (currentLine.isNotEmpty() && lines.size < maxLines) {
            val remainingWords = words.drop(lines.flatMap { it.split(" ") }.size).joinToString(" ")
            lines.add(truncateText(remainingWords, paint, maxWidth))
        }
        return lines.take(maxLines)
    }

    private fun truncateText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var truncated = text
        while (truncated.isNotEmpty() && paint.measureText("$truncated...") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return "$truncated..."
    }
}
