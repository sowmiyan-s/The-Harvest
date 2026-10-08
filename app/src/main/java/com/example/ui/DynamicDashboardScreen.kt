package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*

@Composable
fun DynamicDashboardScreen(
    state: HarvestUiState,
    viewModel: HarvestViewModel,
    modifier: Modifier = Modifier
) {
    val spec = state.dashboardSpec
    val isDark = spec.theme.darkMode

    // Color definitions driven by active ThemeSpec
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val onSurfaceColor = if (isDark) Color(0xFFECEFF1) else Color(0xFF1C1B1F)
    val primaryColor = if (isDark) Color(0xFF81C784) else Color(0xFF1B5E20)
    val accentColor = if (isDark) Color(0xFF00E676) else Color(0xFFE65100)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("dynamic_dashboard_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Premium Hero Banner with Drone Farm Photography & Status
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .testTag("hero_farm_banner"),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_farm_hero_1791454586207),
                        contentDescription = "Farm Aerial Overview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.2f),
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    // Text and badges
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFF14532D).copy(alpha = 0.85f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF4ADE80))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4ADE80))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = state.selectedFarm.scale.displayName,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "GPS: ${"%.3f".format(state.selectedFarm.location.latitude)}, ${"%.3f".format(state.selectedFarm.location.longitude)}",
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.selectedFarm.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "${state.selectedFarm.cropName} • ${state.liveWeatherCondition} • ${"%.1f".format(state.liveTempCelsius)}°C",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFF1F5F9),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Farm Header & Dynamic Persona Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("farm_persona_header_card"),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = state.selectedFarm.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor
                            )
                            Text(
                                text = "${state.selectedFarm.cropName} • ${state.selectedFarm.locationName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = onSurfaceColor.copy(alpha = 0.7f)
                            )
                        }

                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    if (state.selectedFarm.isOrganic) "100% Organic" else "Conventional",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    if (state.selectedFarm.isOrganic) Icons.Filled.Eco else Icons.Filled.Agriculture,
                                    contentDescription = null,
                                    tint = if (state.selectedFarm.isOrganic) Color(0xFF2E7D32) else Color(0xFF1976D2),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Persona & Tone Indicator
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = primaryColor.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Psychology,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Persona Tone: ${spec.copyTone.title} • Theme: ${spec.theme.id}",
                                style = MaterialTheme.typography.labelMedium,
                                color = primaryColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Row (Server-driven actions)
        item {
            Text(
                text = "Dynamic Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = onSurfaceColor
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                spec.quickActions.forEach { action ->
                    ElevatedButton(
                        onClick = {
                            when (action.id) {
                                "act_water" -> viewModel.logWaterCup()
                                "act_scrap" -> viewModel.addKitchenScrap()
                                "act_spray" -> viewModel.testGuardrailSubstance("Glyphosate 41% SL")
                                else -> viewModel.testGuardrailSubstance("Organic Neem Oil Extract")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("action_${action.id}"),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                getQuickActionIcon(action.iconName),
                                contentDescription = action.label,
                                modifier = Modifier.size(20.dp),
                                tint = primaryColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = action.label,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Guardrail alert banner if triggered
        if (state.lastGuardrailWarning != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.organicGuardrailBlocked) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (state.organicGuardrailBlocked) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (state.organicGuardrailBlocked) Color(0xFFC62828) else Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.lastGuardrailWarning ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (state.organicGuardrailBlocked) Color(0xFFB71C1C) else Color(0xFF1B5E20)
                        )
                    }
                }
            }
        }

        // Real-Time Atmospheric Spray & Rotation Intelligence Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("banner_agronomy_shortcuts"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF263238) else Color(0xFFE8F5E9)
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Air,
                                contentDescription = null,
                                tint = Color(0xFF0288D1),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Spray Window: ${state.sprayAdvisory.rating.label}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall,
                                color = onSurfaceColor
                            )
                        }

                        TextButton(
                            onClick = { viewModel.selectTab(AppViewTab.SPRAY_ADVISORY) },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("View Advisory ▶", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Wind: ${"%.1f".format(state.sprayAdvisory.windSpeedKph)} km/h • Delta T: ${"%.1f".format(state.sprayAdvisory.deltaTCelsius)}°C • ${state.sprayAdvisory.recommendedNozzle}",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceColor.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AssistChip(
                            onClick = { viewModel.selectTab(AppViewTab.FIELD_SCOUT) },
                            label = { Text("Field Scout (${state.scoutLogs.size} logs)", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Filled.AssignmentTurnedIn, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            modifier = Modifier.weight(1f)
                        )
                        AssistChip(
                            onClick = { viewModel.selectTab(AppViewTab.CROP_ROTATION) },
                            label = { Text("Rotation Plan", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(Icons.Filled.Autorenew, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Widgets (Strictly rendered in priority order from DashboardSpec)
        items(spec.widgets.sortedBy { it.priorityOrder }) { widgetItem ->
            when (widgetItem.widgetType) {
                WidgetType.WATERING_CUP_LOG -> WateringCupLogWidget(
                    cupsLogged = state.microWaterCupsLogged,
                    onAddCup = { viewModel.logWaterCup() },
                    isDark = isDark
                )
                WidgetType.KITCHEN_COMPOST_HELPER -> KitchenCompostWidget(
                    compostKg = state.scrapCompostKg,
                    onAddScrap = { viewModel.addKitchenScrap() },
                    isDark = isDark
                )
                WidgetType.WEATHER_SOIL_CARD -> WeatherSoilWidget(
                    farm = state.selectedFarm,
                    state = state,
                    onRefresh = { viewModel.refreshLiveTelemetry() },
                    isDark = isDark
                )
                WidgetType.MILESTONE_BADGES -> MilestoneBadgesWidget(
                    cupsLogged = state.microWaterCupsLogged,
                    isDark = isDark
                )
                WidgetType.ANNUAL_GROWTH_TIMELINE -> GrowthTimelineWidget(
                    sowingDate = state.selectedFarm.sowingDate ?: "2026-03-01",
                    crop = state.selectedFarm.cropName,
                    isDark = isDark
                )
                WidgetType.WINTER_CHILLING_GAUGE -> WinterChillingGaugeWidget(
                    accumulated = state.chillingHoursAccumulated,
                    required = state.chillingHoursRequired,
                    isDark = isDark
                )
                WidgetType.MULTI_YEAR_ROI_TRACKER -> MultiYearRoiWidget(
                    acres = state.selectedFarm.areaAcres,
                    isDark = isDark
                )
                WidgetType.BULK_INPUT_CALCULATOR -> BulkInputCalculatorWidget(
                    acres = state.bulkAcreageInput,
                    resultKg = state.bulkNPKResultKg,
                    isOrganic = state.selectedFarm.isOrganic,
                    onAcreageChange = { viewModel.updateAcreageInput(it) },
                    isDark = isDark
                )
                WidgetType.SATELLITE_NDVI_MAP -> SatelliteNdviWidget(
                    ndviScore = state.ndviScore,
                    farmName = state.selectedFarm.name,
                    acres = state.selectedFarm.areaAcres,
                    isDark = isDark
                )
                WidgetType.CAPEX_OPEX_LEDGER -> LedgerWidget(
                    scale = state.selectedFarm.scale,
                    isDark = isDark
                )
                WidgetType.COPILOT_QUICK_ACTIONS -> CopilotWidget(
                    isOrganic = state.selectedFarm.isOrganic,
                    onTestSafety = { viewModel.testGuardrailSubstance("Synthetic Glyphosate Herbicide") },
                    isDark = isDark
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Individual Dynamic Widgets
// -----------------------------------------------------------------------------

@Composable
fun WateringCupLogWidget(cupsLogged: Int, onAddCup: () -> Unit, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("widget_watering_cup"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.WaterDrop, contentDescription = null, tint = Color(0xFF0288D1))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cup-by-Cup Watering Log", fontWeight = FontWeight.Bold)
                }
                Text("$cupsLogged Cups Logged", fontWeight = FontWeight.SemiBold, color = Color(0xFF0288D1))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Tailored for window box & balcony herbs. Tracks hydration volume without needing an irrigation flow meter.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            // ASSUMPTION: Standard urban container cup volume is 240ml
            Text(
                "// ASSUMPTION: 1 cup = 240 mL. Target: ~2 cups/day based on current container potting soil volume.",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onAddCup,
                modifier = Modifier.fillMaxWidth().testTag("btn_log_water_cup"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Log 1 Cup (240 mL)")
            }
        }
    }
}

@Composable
fun KitchenCompostWidget(compostKg: Double, onAddScrap: () -> Unit, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("widget_kitchen_compost"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Recycling, contentDescription = null, tint = Color(0xFF558B2F))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kitchen Scrap Compost Helper", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Diverted from landfill: ${"%.1f".format(compostKg)} kg. Ideal for small apartment compost bins (Bokashi / Worm Vermicompost).",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "// ASSUMPTION: Target brown:green ratio is 2:1 carbon:nitrogen scraps for balcony odor control.",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onAddScrap,
                modifier = Modifier.fillMaxWidth().testTag("btn_add_compost_scrap"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Egg, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add +0.5 kg Kitchen Scraps")
            }
        }
    }
}

@Composable
fun WeatherSoilWidget(
    farm: FarmProfile,
    state: HarvestUiState,
    onRefresh: () -> Unit,
    isDark: Boolean
) {
    val deltaTRating = when {
        state.liveDeltaT in 2.0..8.0 -> "Optimal (2-8°C)"
        state.liveDeltaT < 2.0 -> "Low Inversion Risk"
        else -> "High Evap Risk (>8°C)"
    }
    val deltaTColor = when {
        state.liveDeltaT in 2.0..8.0 -> Color(0xFF16A34A)
        state.liveDeltaT < 2.0 -> Color(0xFFD97706)
        else -> Color(0xFFDC2626)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("widget_weather_soil"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF16241D) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (isDark) Color(0xFF233E30) else Color(0xFFE2EBE6)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Title + Live Pulse + Refresh Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.WbSunny,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            "Realtime Meteorological & Soil Telemetry",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "● LIVE OPEN-METEO FEED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "• ${state.lastTelemetryUpdated}",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("btn_refresh_weather_widget")
                ) {
                    if (state.isWeatherLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF16A34A)
                        )
                    } else {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = "Refresh Telemetry",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weather Condition Highlight Banner
            Surface(
                color = if (isDark) Color(0xFF1B3327) else Color(0xFFF0FDF4),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF2A523F) else Color(0xFFDCFCE7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.CloudQueue,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = state.liveWeatherCondition,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = if (isDark) Color(0xFF7DD3FC) else Color(0xFF0369A1)
                        )
                    }
                    Text(
                        text = "Feels like ${"%.1f".format(state.liveApparentTempCelsius)}°C • UV ${"%.1f".format(state.liveUvIndex)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 6-Point Agronomic Telemetry Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TelemetryStatCard(
                    title = "Air Temp",
                    value = "${"%.1f".format(state.liveTempCelsius)}°C",
                    sub = "Baro ${"%.0f".format(state.livePressureHpa)} hPa",
                    accentColor = Color(0xFFE11D48),
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
                TelemetryStatCard(
                    title = "Delta T (Evap)",
                    value = "${"%.1f".format(state.liveDeltaT)}°C",
                    sub = deltaTRating,
                    accentColor = deltaTColor,
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
                TelemetryStatCard(
                    title = "Wind Velocity",
                    value = "${"%.1f".format(state.liveWindSpeedKph)} kph",
                    sub = "Gusts ${"%.1f".format(state.liveWindGustsKph)} kph",
                    accentColor = Color(0xFF0284C7),
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TelemetryStatCard(
                    title = "Soil Moisture",
                    value = "${"%.1f".format(state.liveSoilMoisturePct)}%",
                    sub = "0-1cm Volumetric",
                    accentColor = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
                TelemetryStatCard(
                    title = "Daily ETc",
                    value = "${"%.1f".format(state.liveDailyEtcMm)} mm",
                    sub = "Crop Water Demand",
                    accentColor = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
                TelemetryStatCard(
                    title = "Humidity",
                    value = "${"%.0f".format(state.liveHumidityPct)}%",
                    sub = "Dew ${"%.1f".format(state.liveDewPointCelsius)}°C",
                    accentColor = Color(0xFF0D9488),
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
            }

            // 12-Hour Hourly Agrometeorological Forecast Carousel
            if (state.hourlyForecast.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Next 12-Hour Spray & Delta T Forecast",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.hourlyForecast.forEach { hour ->
                        val badgeColor = when (hour.condition) {
                            SprayConditionRating.OPTIMAL -> Color(0xFF16A34A)
                            SprayConditionRating.CAUTION -> Color(0xFFD97706)
                            SprayConditionRating.HAZARDOUS -> Color(0xFFDC2626)
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDark) Color(0xFF1E2D25) else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f)),
                            modifier = Modifier.width(82.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    hour.timeLabel,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "${"%.0f".format(hour.tempCelsius)}°C",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    "${"%.0f".format(hour.windSpeedKph)} kph",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = badgeColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "ΔT ${"%.1f".format(hour.deltaTCelsius)}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = badgeColor,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Source: ${state.weatherDataSource}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF16A34A),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Grid: ${roundCoord(farm.location.latitude)}, ${roundCoord(farm.location.longitude)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TelemetryStatCard(
    title: String,
    value: String,
    sub: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isDark: Boolean
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color(0xFF1E2E25) else Color(0xFFF8FAF9),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                title,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                value,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                sub,
                fontSize = 9.sp,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MilestoneBadgesWidget(cupsLogged: Int, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("widget_milestone_badges"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.EmojiEvents, contentDescription = null, tint = Color(0xFFFBC02D))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Seedling Milestones & Badges", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                MilestoneItem("First Sprout", true, Icons.Filled.Spa)
                MilestoneItem("True Leaves", cupsLogged >= 3, Icons.Filled.Eco)
                MilestoneItem("Flower Bud", cupsLogged >= 6, Icons.Filled.LocalFlorist)
                MilestoneItem("First Harvest", cupsLogged >= 10, Icons.Filled.Celebration)
            }
        }
    }
}

@Composable
fun MilestoneItem(label: String, achieved: Boolean, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (achieved) Color(0xFFE8F5E9) else Color(0xFFEEEEEE)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = if (achieved) Color(0xFF2E7D32) else Color(0xFF9E9E9E),
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, fontSize = 10.sp, fontWeight = if (achieved) FontWeight.Bold else FontWeight.Normal)
    }
}

@Composable
fun GrowthTimelineWidget(sowingDate: String, crop: String, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("widget_growth_timeline"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Timeline, contentDescription = null, tint = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Single-Season Crop Timeline", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Sown: $sowingDate • Day 28 of 120 Days to Maturity", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { 28f / 120f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF2E7D32),
                trackColor = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Vegetative Stage (V4)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32))
                Text("Flowering in ~22 days", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun WinterChillingGaugeWidget(accumulated: Int, required: Int, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("widget_chilling_gauge"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AcUnit, contentDescription = null, tint = Color(0xFF00ACC1))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Winter Chilling Hours (<7.2°C)", fontWeight = FontWeight.Bold)
                }
                Text("$accumulated / $required hrs", fontWeight = FontWeight.Bold, color = Color(0xFF00ACC1))
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (accumulated.toFloat() / required.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                color = Color(0xFF00ACC1),
                trackColor = Color(0xFFE0E0E0)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "85% of dormant chill requirement met. Vernalization sufficient for normal spring budbreak.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                "// ASSUMPTION: Threshold is 7.2°C (Weinberger Model). Hours logged via hourly Open-Meteo temps.",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
fun MultiYearRoiWidget(acres: Double, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("widget_multi_year_roi"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.TrendingUp, contentDescription = null, tint = Color(0xFFF57F17))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Perennial Orchard Multi-Year ROI", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text("Acreage: $acres Acres • Establishment CapEx: $45,000", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Year 1-2 (Dormant)", fontSize = 11.sp)
                    Text("-$18,000/yr", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                }
                Column {
                    Text("Year 3 (First Crop)", fontSize = 11.sp)
                    Text("+$8,400", fontWeight = FontWeight.Bold, color = Color(0xFFF57F17))
                }
                Column {
                    Text("Year 5 (Full Yield)", fontSize = 11.sp)
                    Text("+$32,000/yr", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "// ASSUMPTION: Break-even projected at Year 4.2 with commercial Honeycrisp yield models.",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
fun BulkInputCalculatorWidget(
    acres: Double,
    resultKg: Double,
    isOrganic: Boolean,
    onAcreageChange: (Double) -> Unit,
    isDark: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("widget_bulk_calculator"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Calculate, contentDescription = null, tint = Color(0xFF1B5E20))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Bulk Fertilizer & Soil Amendments", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                if (isOrganic) "Organic Blood Meal / Compost Manure Calculator" else "Commercial Bulk N-P-K (46-0-0 Urea Equivalent)",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Acreage: ${"%.1f".format(acres)} Acres", fontWeight = FontWeight.Medium)
                Text("Requirement: ${"%.0f".format(resultKg)} kg N", fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "// ASSUMPTION: 80 kg Nitrogen per acre base rate. Adjusted by soil test nitrate credit.",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
fun SatelliteNdviWidget(ndviScore: Double, farmName: String, acres: Double, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("widget_satellite_ndvi"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.SatelliteAlt, contentDescription = null, tint = Color(0xFF00E676))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sentinel-2 NDVI Polygon", fontWeight = FontWeight.Bold)
                }
                Text("L2A Level", style = MaterialTheme.typography.labelSmall, color = Color(0xFF00E676))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDark) Color(0xFF2C3E50) else Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Field Polygon: ${"%.0f".format(acres)} Acres", fontWeight = FontWeight.Bold)
                    Text("Mean NDVI: $ndviScore (Vigorous Canopy)", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                    Text("Acquisition: 2 days ago • Cloud cover: 0.2%", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun LedgerWidget(scale: FarmScale, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("widget_ledger"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = Color(0xFF455A64))
                Spacer(modifier = Modifier.width(8.dp))
                Text("CapEx & OpEx Farm Ledger", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Operational (OpEx) MTD", fontSize = 12.sp)
                Text(if (scale == FarmScale.COMMERCIAL_500ACRE) "$14,200 USD" else "$320 USD", fontWeight = FontWeight.Bold)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Capital Investments (CapEx)", fontSize = 12.sp)
                Text(if (scale == FarmScale.COMMERCIAL_500ACRE) "$85,000 USD" else "$1,200 USD", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "// ASSUMPTION: CapEx includes tractors, irrigation infrastructure; OpEx includes seed, fuel, labor.",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
fun CopilotWidget(isOrganic: Boolean, onTestSafety: () -> Unit, isDark: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("widget_copilot"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = Color(0xFF7B1FA2))
                Spacer(modifier = Modifier.width(8.dp))
                Text("The Harvest Copilot Engine", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Multi-agent router standing by. Orchestrator coordinates 8 specialist agents behind typed JSON contracts.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onTestSafety,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simulate Guardrail Check (Test Organic Rule)")
            }
        }
    }
}

private fun roundCoord(value: Double): String = "%.2f".format(value)

private fun getQuickActionIcon(iconName: String): ImageVector {
    return when (iconName) {
        "WaterDrop" -> Icons.Filled.WaterDrop
        "EggAlt", "Egg" -> Icons.Filled.Egg
        "PhotoCamera" -> Icons.Filled.PhotoCamera
        "AutoAwesome" -> Icons.Filled.AutoAwesome
        "Science" -> Icons.Filled.Science
        "BugReport" -> Icons.Filled.BugReport
        "Grass" -> Icons.Filled.Grass
        "AcUnit" -> Icons.Filled.AcUnit
        "TrendingUp" -> Icons.Filled.TrendingUp
        "Shield" -> Icons.Filled.Shield
        "Thermostat" -> Icons.Filled.Thermostat
        "SatelliteAlt" -> Icons.Filled.SatelliteAlt
        "LocalShipping" -> Icons.Filled.LocalShipping
        "PrecisionManufacturing" -> Icons.Filled.PrecisionManufacturing
        else -> Icons.Filled.Star
    }
}
