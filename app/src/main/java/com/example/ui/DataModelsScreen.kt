package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DataModelsScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("data_models_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Production Relational & Spatial Data Architecture",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Dual-layer persistence: PostgreSQL 16 + PostGIS on server side, paired with reactive SQLite Room Database on Android for offline-first resilience.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            TableSpecCard(
                tableName = "farms (Spatial Polygons)",
                description = "Stores farm boundary polygons, acreage, location centroid, scale, timezone, and organic certification flag.",
                attributes = listOf(
                    "id: UUID (PK)",
                    "user_id: UUID (FK -> users.id)",
                    "scale: ENUM ('micro_pot', 'small_1acre', 'medium_10acre', 'commercial_500acre')",
                    "boundary: GEOGRAPHY(POLYGON, 4326) [PostGIS GIST indexed]",
                    "centroid: GEOGRAPHY(POINT, 4326) [PostGIS GIST indexed]",
                    "is_organic: BOOLEAN",
                    "area_sq_meters: NUMERIC(14, 2)"
                )
            )
        }

        item {
            TableSpecCard(
                tableName = "crop_profiles (Dynamic Catalog)",
                description = "Reference catalog of agronomic standards (not hardcoded). Contains base chilling requirements, water requirements, and FAO growth stages.",
                attributes = listOf(
                    "id: UUID (PK)",
                    "scientific_name: VARCHAR(200)",
                    "common_name: VARCHAR(100)",
                    "lifecycle: ENUM ('annual', 'perennial', 'biennial')",
                    "chilling_hours_required: INTEGER",
                    "water_req_mm_per_season: NUMERIC(8, 2)",
                    "recommended_n_kg_ha: NUMERIC(6, 2)",
                    "source_citation: TEXT ('FAO Database 2024')"
                )
            )
        }

        item {
            TableSpecCard(
                tableName = "observations (Time-Series Telemetry)",
                description = "Weather, soil moisture, NDVI snapshots, and scouting records timestamped for audit and analysis.",
                attributes = listOf(
                    "id: UUID (PK)",
                    "farm_id: UUID (FK -> farms.id)",
                    "obs_type: ENUM ('weather_soil', 'satellite_ndvi', 'soil_sensor')",
                    "timestamp: TIMESTAMPTZ",
                    "soil_moisture_pct: NUMERIC(5, 2)",
                    "evapotranspiration_mm: NUMERIC(6, 2)",
                    "mean_ndvi: NUMERIC(4, 3) [-1.000 to +1.000]",
                    "raw_payload: JSONB"
                )
            )
        }

        item {
            TableSpecCard(
                tableName = "log_entries (Activities & Ledger)",
                description = "Cup-by-cup watering logs, scrap additions, bulk fertilizers, CapEx investments, and OpEx costs.",
                attributes = listOf(
                    "id: UUID (PK)",
                    "farm_id: UUID (FK -> farms.id)",
                    "category: ENUM ('watering', 'fertilizer', 'compost', 'expense_capex', 'expense_opex')",
                    "quantity: NUMERIC(10, 3)",
                    "unit: VARCHAR(30) ('cups', 'liters', 'kg', 'metric_tons')",
                    "cost_amount: NUMERIC(12, 2)",
                    "input_formula: TEXT (Audit trace for organic verification)"
                )
            )
        }

        item {
            TableSpecCard(
                tableName = "agent_runs (Observability & Cost Audit)",
                description = "Distributed trace records for every agent invocation, tracking tokens, latencies, USD cost, and fallback triggers.",
                attributes = listOf(
                    "id: UUID (PK)",
                    "trace_id: VARCHAR(64)",
                    "agent_name: VARCHAR(50)",
                    "latency_ms: INTEGER",
                    "tokens_used: INTEGER",
                    "estimated_cost_usd: NUMERIC(8, 6)",
                    "is_fallback_triggered: BOOLEAN",
                    "guardrail_passed: BOOLEAN"
                )
            )
        }
    }
}

@Composable
fun TableSpecCard(tableName: String, description: String, attributes: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(tableName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    attributes.forEach { attr ->
                        Text(attr, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
