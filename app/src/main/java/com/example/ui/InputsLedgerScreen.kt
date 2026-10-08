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
import com.example.data.model.FarmScale

data class ExpenseItem(
    val id: String,
    val title: String,
    val category: String, // "CapEx" or "OpEx"
    val amountUsd: Double,
    val dateIso: String
)

@Composable
fun InputsLedgerScreen(
    state: HarvestUiState,
    viewModel: HarvestViewModel,
    modifier: Modifier = Modifier
) {
    val isDark = state.dashboardSpec.theme.darkMode
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val onSurfaceColor = if (isDark) Color(0xFFECEFF1) else Color(0xFF1C1B1F)

    var acreageInput by remember { mutableStateOf(state.selectedFarm.areaAcres.toString()) }
    var selectedCalcType by remember { mutableStateOf("Water Volume") }
    var soilNitrogenPpm by remember { mutableStateOf("12.0") }

    // Sample Ledger Entries
    var expenses by remember {
        mutableStateOf(
            listOf(
                ExpenseItem("exp_1", "Subsurface Drip Irrigation Kit", "CapEx", 2400.0, "2026-09-15"),
                ExpenseItem("exp_2", "OMRI Certified Compost (6 tons)", "OpEx", 270.0, "2026-09-28"),
                ExpenseItem("exp_3", "Crimson Clover Cover Crop Seed", "OpEx", 85.0, "2026-10-02"),
                ExpenseItem("exp_4", "Hand Pruners & Soil Core Sampler", "CapEx", 145.0, "2026-10-04")
            )
        )
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newAmount by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("OpEx") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("inputs_ledger_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Bulk Agronomic Input Calculators
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_bulk_calculator"),
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
                                text = "Acreage Input & Nutrient Calculators",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor
                            )
                            Text(
                                text = "Agent 5 • Transparent formulas with labeled assumptions",
                                style = MaterialTheme.typography.labelSmall,
                                color = onSurfaceColor.copy(alpha = 0.7f)
                            )
                        }
                        Icon(Icons.Filled.Calculate, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(24.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calculator Type Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Water Volume", "Compost/Manure", "N-P-K Nutrients").forEach { type ->
                            FilterChip(
                                selected = selectedCalcType == type,
                                onClick = { selectedCalcType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Acreage Input Field
                    OutlinedTextField(
                        value = acreageInput,
                        onValueChange = {
                            acreageInput = it
                            val parsed = it.toDoubleOrNull()
                            if (parsed != null && parsed > 0) {
                                viewModel.updateAcreageInput(parsed)
                            }
                        },
                        label = { Text("Field Area (Acres)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val acres = acreageInput.toDoubleOrNull() ?: 1.0

                    // Dynamic Formula Results Card
                    when (selectedCalcType) {
                        "Water Volume" -> {
                            val inches = 1.2
                            val gallons = acres * inches * 27154.0
                            val costMin = acres * inches * 4.50
                            val costMax = acres * inches * 7.00

                            Surface(
                                color = Color(0xFFE1F5FE),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Weekly Irrigation Volume:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF01579B)
                                    )
                                    Text(
                                        text = "${String.format("%,.0f", gallons)} Gallons / week",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0277BD)
                                    )
                                    Text(
                                        text = "Est. Pumping Energy Cost: $${String.format("%.2f", costMin)} - $${String.format("%.2f", costMax)}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF01579B)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "// ASSUMPTION: 1 acre-inch = 27,154 gallons. Drip irrigation delivery efficiency 90% at 1.2 inches/week crop ETc.",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF01579B).copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                        "Compost/Manure" -> {
                            val tonsPerAcre = if (acres >= 1.0) 3.0 else 0.5
                            val totalTons = acres * tonsPerAcre
                            val costMin = totalTons * 35.0
                            val costMax = totalTons * 55.0

                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Seasonal Organic Compost Requirement:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20)
                                    )
                                    Text(
                                        text = "${String.format("%.1f", totalTons)} Metric Tons",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Text(
                                        text = "Est. Delivered Bulk Cost: $${String.format("%.2f", costMin)} - $${String.format("%.2f", costMax)}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF1B5E20)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "// ASSUMPTION: 3 tons/acre finished compost provides ~15 kg/acre mineralized N and raises soil organic matter ~0.2%.",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFF1B5E20).copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                        "N-P-K Nutrients" -> {
                            val baseN = 80.0 * acres
                            val soilPpm = soilNitrogenPpm.toDoubleOrNull() ?: 12.0
                            val soilCredit = soilPpm * 4.0 * acres
                            val netN = maxOf(0.0, baseN - soilCredit)

                            Surface(
                                color = Color(0xFFFFF3E0),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "Target Nitrogen Balance (with Soil Test Credit):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFBF360C)
                                    )
                                    Text(
                                        text = "Net N Needed: ${String.format("%.1f", netN)} kg",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                    Text(
                                        text = "Gross Demand: ${String.format("%.0f", baseN)} kg | Soil Test Credit: -${String.format("%.1f", soilCredit)} kg N",
                                        fontSize = 12.sp,
                                        color = Color(0xFFBF360C)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "// ASSUMPTION: 80 kg N/acre baseline. Top 30cm soil nitrate credit = 4 kg N/acre per 1 ppm NO3-N.",
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = Color(0xFFBF360C).copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Farm Expense Ledger (CapEx / OpEx)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_expense_ledger"),
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
                                text = "CapEx & OpEx Farm Ledger",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor
                            )
                            val totalCapEx = expenses.filter { it.category == "CapEx" }.sumOf { it.amountUsd }
                            val totalOpEx = expenses.filter { it.category == "OpEx" }.sumOf { it.amountUsd }
                            Text(
                                text = "CapEx: $${String.format("%,.0f", totalCapEx)} • OpEx: $${String.format("%,.0f", totalOpEx)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier
                                .background(Color(0xFF2E7D32), CircleShape)
                                .size(36.dp)
                                .testTag("btn_add_expense")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Expense", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    expenses.forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) Color(0xFF262626) else Color(0xFFF5F5F5),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.title, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Text(text = "${item.category} • ${item.dateIso}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = "$${String.format("%.2f", item.amountUsd)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.category == "CapEx") Color(0xFF1565C0) else Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Memory Agent & GDPR Privacy Controls
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_gdpr_privacy"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Memory Agent & GDPR Data Rights",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Under EU GDPR Article 20 and CCPA, you retain 100% ownership of your farm telemetry, soil tests, and observation logs. Export or purge at any time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceColor.copy(alpha = 0.75f)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.exportFarmDataJson() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_export_gdpr")
                        ) {
                            Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Export JSON", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.clearHistoryGdpr() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_delete_gdpr")
                        ) {
                            Icon(Icons.Filled.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Delete Data", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Add Expense Dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Log Farm Expense") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Expense Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newAmount,
                        onValueChange = { newAmount = it },
                        label = { Text("Amount ($ USD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = newCategory == "OpEx",
                            onClick = { newCategory = "OpEx" },
                            label = { Text("OpEx (Operating)") }
                        )
                        FilterChip(
                            selected = newCategory == "CapEx",
                            onClick = { newCategory = "CapEx" },
                            label = { Text("CapEx (Capital)") }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = newAmount.toDoubleOrNull() ?: 0.0
                        if (newTitle.isNotBlank() && amt > 0) {
                            expenses = expenses + ExpenseItem(
                                id = "exp_${System.currentTimeMillis()}",
                                title = newTitle,
                                category = newCategory,
                                amountUsd = amt,
                                dateIso = "2026-10-08"
                            )
                            newTitle = ""
                            newAmount = ""
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Save Expense")
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
