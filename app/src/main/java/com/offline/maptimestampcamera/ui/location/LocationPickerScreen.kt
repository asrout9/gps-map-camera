package com.offline.maptimestampcamera.ui.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import com.offline.maptimestampcamera.data.repository.SearchResultItem
import com.offline.maptimestampcamera.map.MapTileSources
import com.offline.maptimestampcamera.model.CoordinateFormat
import com.offline.maptimestampcamera.utils.LocationFormatter
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

enum class MapTileType(val displayName: String, val tileSource: OnlineTileSourceBase) {
    SATELLITE("Satellite View (Default)", MapTileSources.ESRI_SATELLITE),
    VOYAGER("Street View (Voyager)", MapTileSources.CARTO_VOYAGER),
    POSITRON("Street View (Light)", MapTileSources.CARTO_POSITRON),
    DARK_MATTER("Street View (Dark)", MapTileSources.CARTO_DARK_MATTER),
    OSM_DE("OpenStreetMap Standard", MapTileSources.OSM_GERMANY)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerScreen(
    onNavigateBack: () -> Unit,
    onNavigateNext: () -> Unit,
    viewModel: LocationViewModel
) {
    val context = LocalContext.current
    val locationData by viewModel.currentLocationData.collectAsState()
    val isGeocoding by viewModel.isGeocoding.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var isFormatDropdownExpanded by remember { mutableStateOf(false) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var isMapStyleExpanded by remember { mutableStateOf(false) }
    var currentMapTileType by remember { mutableStateOf(MapTileType.SATELLITE) }

    fun fetchCurrentLocation() {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            viewModel.useCurrentDeviceLocation { lat, lon ->
                mapViewRef?.controller?.animateTo(GeoPoint(lat, lon))
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            fetchCurrentLocation()
        }
    }

    fun handleUseCurrentLocationClick() {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            fetchCurrentLocation()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Photo Location", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isMapStyleExpanded = true }) {
                        Icon(Icons.Default.Layers, contentDescription = "Map Style")
                    }
                    DropdownMenu(
                        expanded = isMapStyleExpanded,
                        onDismissRequest = { isMapStyleExpanded = false }
                    ) {
                        MapTileType.values().forEach { tileType ->
                            DropdownMenuItem(
                                text = { Text(tileType.displayName) },
                                onClick = {
                                    currentMapTileType = tileType
                                    mapViewRef?.setTileSource(tileType.tileSource)
                                    isMapStyleExpanded = false
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Interactive Satellite / Street Map View
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val osmConfig = Configuration.getInstance()
                    osmConfig.userAgentValue = "Mozilla/5.0 (Android; Mobile) OfflineMapTimestampCamera/1.0 (contact: admin@offlinemaptimestampcamera.org)"
                    osmConfig.load(ctx, ctx.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

                    MapView(ctx).apply {
                        setTileSource(currentMapTileType.tileSource)
                        setMultiTouchControls(true)
                        controller.setZoom(16.0)
                        val startPoint = GeoPoint(locationData.latitude, locationData.longitude)
                        controller.setCenter(startPoint)

                        addMapListener(object : MapListener {
                            override fun onScroll(event: ScrollEvent?): Boolean {
                                val center = mapCenter
                                viewModel.onMapCenterChanged(center.latitude, center.longitude)
                                return true
                            }

                            override fun onZoom(event: ZoomEvent?): Boolean {
                                val center = mapCenter
                                viewModel.onMapCenterChanged(center.latitude, center.longitude)
                                return true
                            }
                        })
                        mapViewRef = this
                    }
                },
                update = { mapView ->
                    mapViewRef = mapView
                }
            )

            // Center Pin Crosshair
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Center Pin",
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    )
                }
            }

            // Top Location Search Bar & Suggestions Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.onSearchQueryChanged(it) },
                            placeholder = { Text("Search village, city, landmark or lat,lon...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (it.isFocused) isSearchExpanded = true }
                        )

                        if (isSearching) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                // Autocomplete Suggestions List
                if (isSearchExpanded && (searchResults.isNotEmpty() || recentSearches.isNotEmpty())) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        LazyColumn(modifier = Modifier.height((220.dp).coerceAtMost(300.dp))) {
                            if (searchQuery.isEmpty() && recentSearches.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Recent Searches", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                        Text(
                                            "Clear All",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.clickable { viewModel.clearSearchHistory() }
                                        )
                                    }
                                }

                                items(recentSearches) { recent ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                isSearchExpanded = false
                                                viewModel.selectSearchResult(
                                                    SearchResultItem(recent.address, recent.latitude, recent.longitude),
                                                    onMapMove = { lat, lon ->
                                                        mapViewRef?.controller?.animateTo(GeoPoint(lat, lon))
                                                    }
                                                )
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(recent.address, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp)
                                    }
                                }
                            }

                            if (searchResults.isNotEmpty()) {
                                items(searchResults) { result ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                isSearchExpanded = false
                                                viewModel.selectSearchResult(
                                                    result,
                                                    onMapMove = { lat, lon ->
                                                        mapViewRef?.controller?.animateTo(GeoPoint(lat, lon))
                                                    }
                                                )
                                            }
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(result.displayName, maxLines = 2, overflow = TextOverflow.Ellipsis, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Location Info Sheet (compact card)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = locationData.flagEmoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (locationData.country.isNotEmpty()) locationData.country else "Selected Location",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Box {
                            Button(
                                onClick = { isFormatDropdownExpanded = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(userPreferences.coordinateFormat.name, fontSize = 11.sp)
                            }
                            DropdownMenu(
                                expanded = isFormatDropdownExpanded,
                                onDismissRequest = { isFormatDropdownExpanded = false }
                            ) {
                                CoordinateFormat.values().forEach { fmt ->
                                    DropdownMenuItem(
                                        text = { Text(fmt.displayName) },
                                        onClick = {
                                            viewModel.setCoordinateFormat(fmt)
                                            isFormatDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    val formattedCoords = LocationFormatter.formatCoordinates(
                        locationData.latitude,
                        locationData.longitude,
                        userPreferences.coordinateFormat
                    )
                    Text(
                        text = "${formattedCoords.first} | ${formattedCoords.second}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isGeocoding) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Updating address...", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            Text(
                                text = locationData.address.ifEmpty { "Address loading..." },
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }

                    if (locationData.plusCode.isNotEmpty()) {
                        Text(
                            text = "Plus Code: ${locationData.plusCode}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                isSearchExpanded = false
                                mapViewRef?.let { map ->
                                    val bitmap = map.drawToBitmap()
                                    viewModel.finalizeLocationAndCaptureMap(bitmap) {
                                        onNavigateNext()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Next Step")
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                        }
                    }
                }
            }

            // Map Floating Controls (Anchored cleanly above the bottom sheet)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 210.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Current Location GPS Button
                FloatingActionButton(
                    onClick = { handleUseCurrentLocationClick() },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Use Current Location"
                    )
                }

                FloatingActionButton(
                    onClick = { mapViewRef?.controller?.zoomIn() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(42.dp)
                ) {
                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                FloatingActionButton(
                    onClick = { mapViewRef?.controller?.zoomOut() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(42.dp)
                ) {
                    Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                FloatingActionButton(
                    onClick = { mapViewRef?.controller?.setZoom(16.0) },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(42.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset Zoom")
                }
            }
        }
    }
}
