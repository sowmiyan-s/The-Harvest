package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LocationCoords

enum class MapLayerType(val label: String) {
    NDVI_HEATMAP("Sentinel-2 NDVI"),
    TRUE_COLOR("Optical RGB"),
    VIGOR_ZONES("Stress Zones")
}

@Composable
fun FieldMapScreen(
    state: HarvestUiState,
    viewModel: HarvestViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = state.dashboardSpec.theme.darkMode
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val onSurfaceColor = if (isDark) Color(0xFFECEFF1) else Color(0xFF1C1B1F)

    var selectedLayer by remember { mutableStateOf(MapLayerType.NDVI_HEATMAP) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("field_map_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Map Title & Layer Selector Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_map_controls"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Field Polygon & Satellite Telemetry",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor
                            )
                            Text(
                                text = "${state.selectedFarm.name} • ${state.selectedFarm.areaAcres} Acres",
                                style = MaterialTheme.typography.labelSmall,
                                color = onSurfaceColor.copy(alpha = 0.7f)
                            )
                        }

                        AssistChip(
                            onClick = {},
                            label = { Text("Sentinel-2 L2A", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Filled.SatelliteAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFFE3F2FD),
                                labelColor = Color(0xFF0D47A1),
                                leadingIconContentColor = Color(0xFF0D47A1)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Layer Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MapLayerType.values().forEach { layer ->
                            FilterChip(
                                selected = selectedLayer == layer,
                                onClick = { selectedLayer = layer },
                                label = { Text(layer.label, fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(
                                        when (layer) {
                                            MapLayerType.NDVI_HEATMAP -> Icons.Filled.Layers
                                            MapLayerType.TRUE_COLOR -> Icons.Filled.Image
                                            MapLayerType.VIGOR_ZONES -> Icons.Filled.Warning
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        // Visual Field Canvas (Polygon Rendering + NDVI Spectral Shading)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_field_canvas"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0xFF141E18) else Color(0xFFE8ECE9))
                            .border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    ) {
                        // Custom Canvas for Field Polygon and NDVI Grid
                        FieldPolygonCanvas(
                            polygon = state.selectedFarm.polygon,
                            layer = selectedLayer,
                            ndviScore = state.ndviScore,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top Overlay: Centroid GPS Coordinates
                        Surface(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Place, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Lat ${String.format("%.4f", state.selectedFarm.location.latitude)}, Lon ${String.format("%.4f", state.selectedFarm.location.longitude)}",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        // Bottom Overlay: Live Mean NDVI
                        Surface(
                            color = Color(0xFF1B5E20).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Eco, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Mean NDVI: ${state.ndviScore}",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Satellite Metadata Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        MetaItem(label = "Resolution", value = "10m GSD")
                        MetaItem(label = "Cloud Mask", value = "1.8% Valid")
                        MetaItem(label = "Spectral Band", value = "B8 NIR / B4 Red")
                        MetaItem(label = "Pass Time", value = "10:42 UTC")
                    }
                }
            }
        }

        // NDVI Health Legend Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_ndvi_legend"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Normalized Difference Vegetation Index (NDVI) Scale:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                    ) {
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFF8D6E63))) // Bare soil <0.3
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFE53935))) // Stressed 0.3-0.5
                        Box(modifier = Modifier.weight(1f).fillMaxHeight().background(Color(0xFFFDD835))) // Moderate 0.5-0.7
                        Box(modifier = Modifier.weight(1.5f).fillMaxHeight().background(Color(0xFF2E7D32))) // Vigorous >0.7
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.0 Bare Soil", fontSize = 10.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                        Text("0.4 Stressed", fontSize = 10.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                        Text("0.6 Moderate", fontSize = 10.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                        Text("0.9 Dense Canopy", fontSize = 10.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                    }
                }
            }
        }

        // Formula & Labeled Assumption Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_satellite_assumptions"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Calculate, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Scientific Formula & Data Provenance",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Formula: NDVI = (NIR - Red) / (NIR + Red)\n" +
                                "Source: European Space Agency (ESA) Sentinel-2 Level-2A BOA Reflectance.\n" +
                                "// ASSUMPTION: Pixel values filtered using Scene Classification Layer (SCL) cloud shadow probability < 10%.",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = onSurfaceColor.copy(alpha = 0.75f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldPolygonCanvas(
    polygon: List<LocationCoords>,
    layer: MapLayerType,
    ndviScore: Double,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Define field polygon path inside canvas bounds
        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.20f)
            lineTo(w * 0.75f, h * 0.15f)
            lineTo(w * 0.88f, h * 0.70f)
            lineTo(w * 0.40f, h * 0.85f)
            lineTo(w * 0.12f, h * 0.65f)
            close()
        }

        // Fill based on active layer
        val fillColor = when (layer) {
            MapLayerType.NDVI_HEATMAP -> {
                if (ndviScore >= 0.7) Color(0xFF2E7D32).copy(alpha = 0.65f)
                else if (ndviScore >= 0.5) Color(0xFFFBC02D).copy(alpha = 0.65f)
                else Color(0xFFD32F2F).copy(alpha = 0.65f)
            }
            MapLayerType.TRUE_COLOR -> Color(0xFF388E3C).copy(alpha = 0.50f)
            MapLayerType.VIGOR_ZONES -> Color(0xFF00796B).copy(alpha = 0.60f)
        }

        drawPath(path = path, color = fillColor, style = Fill)
        drawPath(
            path = path,
            color = Color(0xFF1B5E20),
            style = Stroke(width = 4.dp.toPx())
        )

        // Draw internal stress / vigor zones if NDVI or Vigor is selected
        if (layer == MapLayerType.NDVI_HEATMAP || layer == MapLayerType.VIGOR_ZONES) {
            drawCircle(
                color = Color(0xFF81C784).copy(alpha = 0.7f),
                radius = 35.dp.toPx(),
                center = Offset(w * 0.45f, h * 0.45f)
            )
            // Simulated minor stress pocket in southeastern corner
            drawCircle(
                color = Color(0xFFFFA000).copy(alpha = 0.8f),
                radius = 22.dp.toPx(),
                center = Offset(w * 0.68f, h * 0.60f)
            )
        }

        // Draw corner boundary anchor points
        val anchorPoints = listOf(
            Offset(w * 0.15f, h * 0.20f),
            Offset(w * 0.75f, h * 0.15f),
            Offset(w * 0.88f, h * 0.70f),
            Offset(w * 0.40f, h * 0.85f),
            Offset(w * 0.12f, h * 0.65f)
        )
        anchorPoints.forEach { pt ->
            drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
            drawCircle(color = Color(0xFF1B5E20), radius = 3.dp.toPx(), center = pt)
        }
    }
}

@Composable
private fun MetaItem(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
