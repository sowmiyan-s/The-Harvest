package com.example.ui

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SprayConditionRating

@Composable
fun SprayAdvisoryScreen(
    state: HarvestUiState,
    viewModel: HarvestViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = state.dashboardSpec.theme.darkMode
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val onSurfaceColor = if (isDark) Color(0xFFECEFF1) else Color(0xFF1C1B1F)

    val advisory = state.sprayAdvisory

    val ratingColor = when (advisory.rating) {
        SprayConditionRating.OPTIMAL -> Color(0xFF2E7D32)
        SprayConditionRating.CAUTION -> Color(0xFFE65100)
        SprayConditionRating.HAZARDOUS -> Color(0xFFC62828)
    }

    val ratingBgColor = when (advisory.rating) {
        SprayConditionRating.OPTIMAL -> Color(0xFFE8F5E9)
        SprayConditionRating.CAUTION -> Color(0xFFFFF3E0)
        SprayConditionRating.HAZARDOUS -> Color(0xFFFFEBEE)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("spray_advisory_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Status Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_spray_status"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = ratingBgColor,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        when (advisory.rating) {
                                            SprayConditionRating.OPTIMAL -> Icons.Filled.CheckCircle
                                            SprayConditionRating.CAUTION -> Icons.Filled.Warning
                                            SprayConditionRating.HAZARDOUS -> Icons.Filled.Dangerous
                                        },
                                        contentDescription = null,
                                        tint = ratingColor,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Spray Window & Drift Risk",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                                Text(
                                    text = "${state.selectedFarm.name} • ${state.selectedFarm.cropName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onSurfaceColor.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = ratingBgColor
                        ) {
                            Text(
                                text = advisory.rating.label,
                                color = ratingColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = advisory.summary,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = onSurfaceColor
                    )
                }
            }
        }

        // Live Atmospheric Telemetry Grid
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_spray_telemetry"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Real-Time Microclimate Telemetry",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryParamBox(
                            label = "Wind Velocity",
                            value = "${"%.1f".format(advisory.windSpeedKph)} km/h",
                            subtext = "Threshold: <15 km/h",
                            statusGood = advisory.windSpeedKph <= 15.0,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TelemetryParamBox(
                            label = "Delta T (Evap.)",
                            value = "${"%.1f".format(advisory.deltaTCelsius)}°C",
                            subtext = "Optimal: 2°C - 8°C",
                            statusGood = advisory.deltaTCelsius in 2.0..8.0,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryParamBox(
                            label = "Rain Probability",
                            value = "${advisory.rainRiskPct}%",
                            subtext = "Rainfast: >2h dry",
                            statusGood = advisory.rainRiskPct < 30,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TelemetryParamBox(
                            label = "Thermal Inversion",
                            value = if (advisory.inversionRisk) "HIGH RISK" else "None Detected",
                            subtext = "Air stability check",
                            statusGood = !advisory.inversionRisk,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Nozzle Advisory Box
                    Surface(
                        color = Color(0xFFE3F2FD),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.WaterDamage,
                                contentDescription = null,
                                tint = Color(0xFF0288D1),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Recommended Nozzle Spec:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF01579B)
                                )
                                Text(
                                    text = advisory.recommendedNozzle,
                                    fontSize = 12.sp,
                                    color = Color(0xFF0277BD)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 12-Hour Spray Window Progression
        if (state.hourlyForecast.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_hourly_spray_timeline"),
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
                            Text(
                                text = "12-Hour Spray Window Forecast",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor
                            )
                            Surface(
                                color = Color(0xFF16A34A).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Open-Meteo",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

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
                                    color = if (isDark) Color(0xFF233028) else Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f)),
                                    modifier = Modifier.width(86.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(hour.timeLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text("${"%.0f".format(hour.tempCelsius)}°C", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                                        Text("${"%.0f".format(hour.windSpeedKph)} kph", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = badgeColor.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = hour.condition.label.take(7),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeColor,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Satellite Sync & Safety Stress Verification
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_drift_simulator"),
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
                        Text(
                            text = "Live Telemetry & Stress Validation",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
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
                                "Live Feed",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Real field sensor reading: Wind ${"%.1f".format(state.liveWindSpeedKph)} km/h • Delta T ${"%.1f".format(state.liveDeltaT)}°C",
                        fontSize = 11.sp,
                        color = onSurfaceColor.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FilledTonalButton(
                        onClick = { viewModel.resetSprayToLiveTelemetry() },
                        modifier = Modifier.fillMaxWidth().testTag("btn_reset_live_telemetry"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset to Live Satellite Telemetry", fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Verify nozzle safety cutoffs under hypothetical conditions:",
                        fontSize = 10.sp,
                        color = onSurfaceColor.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.simulateSprayConditions(windSpeed = 24.0, rainRisk = 20) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Wind >18", fontSize = 10.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.simulateSprayConditions(windSpeed = 1.8, rainRisk = 0) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Inversion <3", fontSize = 10.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.simulateSprayConditions(windSpeed = 9.0, rainRisk = 75) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Rain >60%", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Legal & EPA Safety Notice
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDark) Color(0xFF2C2C2C) else Color(0xFFF5F5F5),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Gavel,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Legal Regulatory Requirement",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = advisory.legalNotice,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryParamBox(
    label: String,
    value: String,
    subtext: String,
    statusGood: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (statusGood) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = if (statusGood) Color(0xFF1B5E20) else Color(0xFFB71C1C)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (statusGood) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtext,
                fontSize = 9.sp,
                color = if (statusGood) Color(0xFF388E3C) else Color(0xFFD32F2F)
            )
        }
    }
}
