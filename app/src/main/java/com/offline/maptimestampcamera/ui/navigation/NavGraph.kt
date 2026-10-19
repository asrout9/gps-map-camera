package com.offline.maptimestampcamera.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.offline.maptimestampcamera.di.AppContainer
import com.offline.maptimestampcamera.ui.camera.CameraScreen
import com.offline.maptimestampcamera.ui.camera.CameraViewModel
import com.offline.maptimestampcamera.ui.datetime.DateTimePickerScreen
import com.offline.maptimestampcamera.ui.datetime.DateTimeViewModel
import com.offline.maptimestampcamera.ui.gallery.GalleryScreen
import com.offline.maptimestampcamera.ui.gallery.GalleryViewModel
import com.offline.maptimestampcamera.ui.gallery.PhotoDetailScreen
import com.offline.maptimestampcamera.ui.home.HomeScreen
import com.offline.maptimestampcamera.ui.location.LocationPickerScreen
import com.offline.maptimestampcamera.ui.location.LocationViewModel
import com.offline.maptimestampcamera.ui.overlay.OverlayEditorScreen
import com.offline.maptimestampcamera.ui.overlay.OverlayViewModel
import com.offline.maptimestampcamera.ui.settings.SettingsScreen
import com.offline.maptimestampcamera.ui.settings.SettingsViewModel

import com.offline.maptimestampcamera.ui.splash.SplashScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    appContainer: AppContainer
) {
    val context = LocalContext.current.applicationContext

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNewCameraClick = { navController.navigate(Screen.LocationPicker.route) },
                onGalleryClick = { navController.navigate(Screen.Gallery.route) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.LocationPicker.route) {
            val locationViewModel: LocationViewModel = viewModel(
                factory = LocationViewModel.Factory(
                    context,
                    appContainer.locationRepository,
                    appContainer.sessionRepository,
                    appContainer.appPreferencesRepository
                )
            )
            LocationPickerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateNext = { navController.navigate(Screen.DateTimePicker.route) },
                viewModel = locationViewModel
            )
        }

        composable(Screen.DateTimePicker.route) {
            val dateTimeViewModel: DateTimeViewModel = viewModel(
                factory = DateTimeViewModel.Factory(appContainer.sessionRepository)
            )
            DateTimePickerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateNext = { navController.navigate(Screen.Camera.route) },
                viewModel = dateTimeViewModel
            )
        }

        composable(Screen.Camera.route) {
            val cameraViewModel: CameraViewModel = viewModel(
                factory = CameraViewModel.Factory(
                    context,
                    appContainer.sessionRepository,
                    appContainer.photoRepository,
                    appContainer.appPreferencesRepository
                )
            )
            CameraScreen(
                onNavigateBack = { navController.popBackStack() },
                onOpenOverlayEditor = { navController.navigate(Screen.OverlayEditor.route) },
                onOpenLocationPicker = { navController.navigate(Screen.LocationPicker.route) },
                onPhotoCaptured = { photoId ->
                    navController.navigate(Screen.PhotoDetail.createRoute(photoId))
                },
                onOpenGallery = { navController.navigate(Screen.Gallery.route) },
                viewModel = cameraViewModel
            )
        }

        composable(Screen.OverlayEditor.route) {
            val overlayViewModel: OverlayViewModel = viewModel(
                factory = OverlayViewModel.Factory(
                    appContainer.sessionRepository,
                    appContainer.overlayPresetRepository,
                    appContainer.appPreferencesRepository
                )
            )
            OverlayEditorScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = overlayViewModel
            )
        }

        composable(Screen.Gallery.route) {
            val galleryViewModel: GalleryViewModel = viewModel(
                factory = GalleryViewModel.Factory(appContainer.photoRepository)
            )
            GalleryScreen(
                onNavigateBack = { navController.popBackStack() },
                onPhotoClick = { photoId ->
                    navController.navigate(Screen.PhotoDetail.createRoute(photoId))
                },
                viewModel = galleryViewModel
            )
        }

        composable(
            route = Screen.PhotoDetail.route,
            arguments = listOf(navArgument("photoId") { type = NavType.LongType })
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getLong("photoId") ?: 0L
            val galleryViewModel: GalleryViewModel = viewModel(
                factory = GalleryViewModel.Factory(appContainer.photoRepository)
            )
            PhotoDetailScreen(
                photoId = photoId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = galleryViewModel
            )
        }

        composable(Screen.Settings.route) {
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModel.Factory(appContainer.appPreferencesRepository)
            )
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = settingsViewModel
            )
        }
    }
}
