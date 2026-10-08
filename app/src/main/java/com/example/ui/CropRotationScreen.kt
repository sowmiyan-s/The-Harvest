package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CropRotationalRecommendation

@Composable
fun CropRotationScreen(
    state: HarvestUiState,
    viewModel: HarvestViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = state.dashboardSpec.theme.darkMode
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val onSurfaceColor = if (isDark) Color(0xFFECEFF1) else Color(0xFF1C1B1F)

    val recommendations = state.rotationalPlan

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("crop_rotation_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_rotation_hero"),
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
                                color = Color(0xFF1B5E20).copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.Autorenew,
                                        contentDescription = null,
                                        tint = Color(0xFF1B5E20),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Biological Crop Rotation Planner",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                                Text(
                                    text = "Current: ${state.selectedFarm.cropName} (${state.selectedFarm.scale.displayName})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onSurfaceColor.copy(alpha = 0.7f)
                                )
                            }
                        }

                        AssistChip(
                            onClick = {},
                            label = { Text("Pest Disruption", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Filled.Shield, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Designed by the Crop Knowledge & Guardrail Agents to interrupt soil-borne fungal pathogens, cycle nutrient demands, and maximize biological atmospheric nitrogen credits without heavy synthetic inputs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceColor.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Succession Recommendations
        items(recommendations) { plan ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rotation_item_${plan.suggestedCrop}"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = plan.suggestedCrop,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor
                            )
                            Text(
                                text = "${plan.season} • Botanical Family: ${plan.family}",
                                style = MaterialTheme.typography.labelSmall,
                                color = onSurfaceColor.copy(alpha = 0.7f)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (plan.nitrogenImpactKgPerAcre >= 0) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                        ) {
                            Text(
                                text = if (plan.nitrogenImpactKgPerAcre >= 0) "+${plan.nitrogenImpactKgPerAcre.toInt()} kg N/ac Credit" else "${plan.nitrogenImpactKgPerAcre.toInt()} kg N Demand",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (plan.nitrogenImpactKgPerAcre >= 0) Color(0xFF1B5E20) else Color(0xFFE65100),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) Color(0xFF263238) else Color(0xFFECEFF1),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Eco, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Agronomic Benefit:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = plan.agronomicBenefit, fontSize = 12.sp, color = onSurfaceColor.copy(alpha = 0.9f))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) Color(0xFF3E2723) else Color(0xFFFFF3E0),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.BugReport, contentDescription = null, tint = Color(0xFFBF360C), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pathogen / Pest Interruption:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBF360C))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = plan.breakPestCycle, fontSize = 12.sp, color = Color(0xFFD84315))
                        }
                    }
                }
            }
        }
    }
}
