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
import com.example.data.model.FieldScoutLog

@Composable
fun FieldScoutScreen(
    state: HarvestUiState,
    viewModel: HarvestViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = state.dashboardSpec.theme.darkMode
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val onSurfaceColor = if (isDark) Color(0xFFECEFF1) else Color(0xFF1C1B1F)

    var showAddDialog by remember { mutableStateOf(false) }
    var scoutTitle by remember { mutableStateOf("") }
    var scoutStage by remember { mutableStateOf("V4 Vegetative") }
    var scoutIssue by remember { mutableStateOf("Aphid colonies on lower leaves") }
    var scoutSeverity by remember { mutableStateOf("Moderate") }
    var scoutAction by remember { mutableStateOf("Release beneficial ladybeetles (Hippodamia convergens)") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("field_scout_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header & Action Button
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_scout_hero"),
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
                                color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.AssignmentTurnedIn,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Offline Field Scout Logs",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                                Text(
                                    text = "${state.scoutLogs.size} observations logged for ${state.selectedFarm.name}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onSurfaceColor.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_add_scout_log")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Log", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Scout records are persisted locally for no-connectivity conditions and auto-reconcile with cloud and agronomy multi-agents once in range.",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceColor.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // List of Scout Logs
        items(state.scoutLogs) { log ->
            val severityColor = when (log.severityLevel.lowercase()) {
                "critical" -> Color(0xFFC62828)
                "moderate" -> Color(0xFFF57F17)
                else -> Color(0xFF2E7D32)
            }
            val severityBg = when (log.severityLevel.lowercase()) {
                "critical" -> Color(0xFFFFEBEE)
                "moderate" -> Color(0xFFFFFDE7)
                else -> Color(0xFFE8F5E9)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("scout_item_${log.id}"),
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
                        Text(
                            text = log.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = severityBg
                        ) {
                            Text(
                                text = "${log.severityLevel} Severity",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = severityColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Stage: ${log.cropStage}",
                            fontSize = 11.sp,
                            color = onSurfaceColor.copy(alpha = 0.7f)
                        )
                        Text(
                            text = log.dateIso,
                            fontSize = 11.sp,
                            color = onSurfaceColor.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) Color(0xFF2B2B2B) else Color(0xFFF5F5F5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = "Observation: ${log.pestOrIssue}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = onSurfaceColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Action: ${log.actionTaken}",
                                fontSize = 12.sp,
                                color = Color(0xFF1B5E20),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.PhotoCamera, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${log.photoCount} photo attached", fontSize = 10.sp, color = Color.Gray)
                        }
                        Text(
                            text = "● ${log.syncStatus}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }

    // Dialog for logging a new observation
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Log Field Scouting Observation") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = scoutTitle,
                        onValueChange = { scoutTitle = it },
                        label = { Text("Observation Title (e.g., North Block Aphids)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = scoutStage,
                        onValueChange = { scoutStage = it },
                        label = { Text("Crop Growth Stage") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = scoutIssue,
                        onValueChange = { scoutIssue = it },
                        label = { Text("Observed Pest / Deficiency / Stress") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = scoutSeverity,
                        onValueChange = { scoutSeverity = it },
                        label = { Text("Severity (Low / Moderate / Critical)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = scoutAction,
                        onValueChange = { scoutAction = it },
                        label = { Text("Agronomic Action Taken") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (scoutTitle.isNotBlank()) {
                            val newEntry = FieldScoutLog(
                                farmId = state.selectedFarm.id,
                                title = scoutTitle,
                                cropStage = scoutStage,
                                pestOrIssue = scoutIssue,
                                severityLevel = scoutSeverity,
                                actionTaken = scoutAction,
                                dateIso = "2026-10-08"
                            )
                            viewModel.addScoutLog(newEntry)
                            showAddDialog = false
                            scoutTitle = ""
                        }
                    }
                ) {
                    Text("Save Observation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
