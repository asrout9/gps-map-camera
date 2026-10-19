package com.offline.maptimestampcamera.ui.overlay

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.offline.maptimestampcamera.model.WidgetConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverlayEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: OverlayViewModel
) {
    val locationData by viewModel.selectedLocation.collectAsState()
    val dateTimeData by viewModel.selectedDateTime.collectAsState()
    val mapSnapshotPath by viewModel.mapSnapshotPath.collectAsState()
    val currentConfig by viewModel.currentConfig.collectAsState()
    val selectedWidgetId by viewModel.selectedWidgetId.collectAsState()
    val userPreferences by viewModel.userPreferences.collectAsState()
    val availablePresets by viewModel.availablePresets.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }

    val selectedWidget = currentConfig.widgets.find { it.id == selectedWidgetId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Overlay & Widget Editor", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showSaveDialog = true }) {
                        Icon(Icons.Default.Save, contentDescription = "Save Preset")
                    }
                    IconButton(onClick = { viewModel.applyAndProceed { onNavigateBack() } }) {
                        Icon(Icons.Default.Check, contentDescription = "Apply")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.55f)
                    .background(Color.Black)
            ) {
                LiveOverlayView(
                    modifier = Modifier.fillMaxSize(),
                    config = currentConfig,
                    locationData = locationData,
                    dateTimeData = dateTimeData,
                    mapSnapshotPath = mapSnapshotPath,
                    preferences = userPreferences,
                    selectedWidgetId = selectedWidgetId,
                    onWidgetClick = { id -> viewModel.selectWidget(id) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Presets") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Widgets") }
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = { selectedTabIndex = 2 },
                        text = { Text("Styling") }
                    )
                }

                when (selectedTabIndex) {
                    0 -> {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(availablePresets) { preset ->
                                Card(
                                    modifier = Modifier
                                        .size(width = 140.dp, height = 100.dp)
                                        .clickable { viewModel.loadPreset(preset) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (currentConfig.id == preset.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(preset.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("${preset.widgets.count { it.enabled }} Active Widgets", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            items(currentConfig.widgets) { widget ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.selectWidget(widget.id) }
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Checkbox(
                                            checked = widget.enabled,
                                            onCheckedChange = { viewModel.toggleWidgetEnabled(widget.id) }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(widget.type.displayName, fontWeight = FontWeight.SemiBold)
                                    }
                                    if (selectedWidgetId == widget.id) {
                                        Text("SELECTED", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        if (selectedWidget != null) {
                            WidgetStyleControls(
                                widget = selectedWidget,
                                onWidgetChange = { updated -> viewModel.updateWidgetConfig(updated) }
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Select a widget to edit style", color = Color.Gray)
                            }
                        }
                    }
                }
            }
        }

        if (showSaveDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDialog = false },
                title = { Text("Save Overlay Preset") },
                text = {
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text("Preset Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (presetNameInput.isNotBlank()) {
                                viewModel.savePreset(presetNameInput) {
                                    showSaveDialog = false
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
private fun WidgetStyleControls(
    widget: WidgetConfig,
    onWidgetChange: (WidgetConfig) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Font Size: ${widget.fontSizeSp.toInt()} sp", fontSize = 13.sp)
            Slider(
                value = widget.fontSizeSp,
                onValueChange = { onWidgetChange(widget.copy(fontSizeSp = it)) },
                valueRange = 8f..32f
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Bold")
                    Switch(
                        checked = widget.isBold,
                        onCheckedChange = { onWidgetChange(widget.copy(isBold = it)) }
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Italic")
                    Switch(
                        checked = widget.isItalic,
                        onCheckedChange = { onWidgetChange(widget.copy(isItalic = it)) }
                    )
                }
            }
        }

        item {
            Text("Opacity: ${(widget.opacity * 100).toInt()}%", fontSize = 13.sp)
            Slider(
                value = widget.opacity,
                onValueChange = { onWidgetChange(widget.copy(opacity = it)) },
                valueRange = 0.1f..1.0f
            )
        }

        item {
            Text("Corner Radius: ${widget.cornerRadiusDp.toInt()} dp", fontSize = 13.sp)
            Slider(
                value = widget.cornerRadiusDp,
                onValueChange = { onWidgetChange(widget.copy(cornerRadiusDp = it)) },
                valueRange = 0f..24f
            )
        }

        if (widget.type == com.offline.maptimestampcamera.model.WidgetType.CUSTOM_NOTE) {
            item {
                OutlinedTextField(
                    value = widget.customText,
                    onValueChange = { onWidgetChange(widget.copy(customText = it)) },
                    label = { Text("Custom Note Text") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
