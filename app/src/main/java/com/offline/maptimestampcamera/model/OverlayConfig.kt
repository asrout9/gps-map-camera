package com.offline.maptimestampcamera.model

import kotlinx.serialization.Serializable

enum class WidgetType(val displayName: String) {
    DATE("Date"),
    TIME("Time"),
    COORDINATES("Coordinates"),
    ADDRESS("Address"),
    MAP_THUMBNAIL("Map Thumbnail"),
    LOGO("App Logo"),
    CUSTOM_NOTE("Custom Note"),
    COMPASS("Compass Direction"),
    COUNTRY_FLAG("Country Flag"),
    WEATHER("Weather Placeholder"),
    BATTERY("Battery Level"),
    ALTITUDE("Altitude"),
    DEVICE_MODEL("Device Model"),
    WATERMARK("Watermark")
}

@Serializable
data class WidgetConfig(
    val id: String,
    val type: WidgetType,
    val enabled: Boolean = true,
    val xPercent: Float = 0.05f, // % from left (0.0 to 1.0)
    val yPercent: Float = 0.70f, // % from top (0.0 to 1.0)
    val widthPercent: Float = 0.90f,
    val heightPercent: Float = 0.25f,
    val rotationDegrees: Float = 0f,
    val opacity: Float = 0.9f,
    val fontSizeSp: Float = 14f,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val textColorHex: String = "#FFFFFF",
    val strokeColorHex: String = "#000000",
    val strokeWidthDp: Float = 1f,
    val shadowColorHex: String = "#80000000",
    val shadowRadiusDp: Float = 2f,
    val backgroundColorHex: String = "#CC000000", // Semi-transparent black
    val cornerRadiusDp: Float = 12f,
    val paddingDp: Float = 8f,
    val marginDp: Float = 4f,
    val zIndex: Int = 0,
    val customText: String = "",
    val isLocked: Boolean = false
)

@Serializable
data class OverlayConfig(
    val id: String = "default_preset",
    val name: String = "Standard GPS Overlay",
    val isDefault: Boolean = true,
    val backgroundColorHex: String = "#99000000",
    val snapToGrid: Boolean = true,
    val gridSpacingDp: Float = 10f,
    val widgets: List<WidgetConfig> = defaultWidgets()
) {
    companion object {
        fun defaultWidgets(): List<WidgetConfig> {
            return listOf(
                WidgetConfig(
                    id = "map_thumbnail",
                    type = WidgetType.MAP_THUMBNAIL,
                    xPercent = 0.04f,
                    yPercent = 0.72f,
                    widthPercent = 0.28f,
                    heightPercent = 0.24f,
                    cornerRadiusDp = 8f,
                    backgroundColorHex = "#00000000"
                ),
                WidgetConfig(
                    id = "main_info",
                    type = WidgetType.ADDRESS,
                    xPercent = 0.34f,
                    yPercent = 0.72f,
                    widthPercent = 0.62f,
                    heightPercent = 0.24f,
                    fontSizeSp = 12f,
                    isBold = false,
                    backgroundColorHex = "#B3000000",
                    textColorHex = "#FFFFFF",
                    cornerRadiusDp = 8f
                )
            )
        }

        fun presetCardOverlay(): OverlayConfig {
            return OverlayConfig(
                id = "full_card",
                name = "Full Stamp Card",
                isDefault = false,
                widgets = listOf(
                    WidgetConfig(
                        id = "map_thumbnail",
                        type = WidgetType.MAP_THUMBNAIL,
                        xPercent = 0.05f,
                        yPercent = 0.68f,
                        widthPercent = 0.30f,
                        heightPercent = 0.28f,
                        cornerRadiusDp = 10f
                    ),
                    WidgetConfig(
                        id = "date_time",
                        type = WidgetType.DATE,
                        xPercent = 0.38f,
                        yPercent = 0.68f,
                        widthPercent = 0.57f,
                        heightPercent = 0.08f,
                        fontSizeSp = 13f,
                        isBold = true,
                        backgroundColorHex = "#E61A1A1A",
                        textColorHex = "#FFD700"
                    ),
                    WidgetConfig(
                        id = "location_details",
                        type = WidgetType.COORDINATES,
                        xPercent = 0.38f,
                        yPercent = 0.77f,
                        widthPercent = 0.57f,
                        heightPercent = 0.19f,
                        fontSizeSp = 11f,
                        backgroundColorHex = "#CC000000",
                        textColorHex = "#FFFFFF"
                    )
                )
            )
        }
    }
}
