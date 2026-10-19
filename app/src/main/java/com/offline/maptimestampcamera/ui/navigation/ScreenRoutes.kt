package com.offline.maptimestampcamera.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object LocationPicker : Screen("location_picker")
    object DateTimePicker : Screen("datetime_picker")
    object Camera : Screen("camera")
    object OverlayEditor : Screen("overlay_editor")
    object Gallery : Screen("gallery")
    object PhotoDetail : Screen("photo_detail/{photoId}") {
        fun createRoute(photoId: Long) = "photo_detail/$photoId"
    }
    object Settings : Screen("settings")
}
