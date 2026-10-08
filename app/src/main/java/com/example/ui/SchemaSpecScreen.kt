package com.example.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
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
import com.example.data.model.DashboardSpec

@Composable
fun SchemaSpecScreen(
    spec: DashboardSpec,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(0) } // 0: Live Compiled JSON, 1: JSON Schema Draft-07

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("schema_spec_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Code, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Server-Driven Dashboard Contract",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "The mobile client renders exclusively from this JSON specification. New farm categories or custom crops require zero mobile application updates.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = viewMode == 0,
                    onClick = { viewMode = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Live Compiled Spec")
                }
                SegmentedButton(
                    selected = viewMode == 1,
                    onClick = { viewMode = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("JSON Schema Draft-07")
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (viewMode == 0) "DashboardSpec Instance (${spec.scale.name}):" else "Formal JSON Schema (dashboard_spec.schema.json):",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = Color(0xFF212121),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (viewMode == 0) getCompiledSpecJson(spec) else getFormalSchemaJson(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = Color(0xFF81C784),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getCompiledSpecJson(spec: DashboardSpec): String {
    return """
{
  "schema_version": "${spec.schemaVersion}",
  "scale": "${spec.scale.name.lowercase()}",
  "crop": "${spec.crop}",
  "lifecycle": "${spec.lifecycle.name.lowercase()}",
  "is_organic": ${spec.isOrganic},
  "language": "${spec.language}",
  "copy_tone": "${spec.copyTone.name.lowercase()}",
  "theme": {
    "id": "${spec.theme.id}",
    "primary_color": "${spec.theme.primaryColorHex}",
    "secondary_color": "${spec.theme.secondaryColorHex}",
    "background_color": "${spec.theme.backgroundColorHex}",
    "dark_mode": ${spec.theme.darkMode},
    "density": "${spec.theme.density}"
  },
  "quick_actions_count": ${spec.quickActions.size},
  "widgets": [
${spec.widgets.joinToString(",\n") { "    { \"id\": \"${it.id}\", \"type\": \"${it.widgetType.name.lowercase()}\", \"order\": ${it.priorityOrder}, \"span\": ${it.gridSpan} }" }}
  ]
}
    """.trimIndent()
}

private fun getFormalSchemaJson(): String {
    return """
{
  "${'$'}schema": "http://json-schema.org/draft-07/schema#",
  "${'$'}id": "https://theharvest.ai/schemas/v1/dashboard-spec.schema.json",
  "title": "DashboardSpec",
  "type": "object",
  "required": [
    "schema_version", "scale", "crop", "lifecycle",
    "is_organic", "language", "copy_tone", "theme", "widgets"
  ],
  "properties": {
    "scale": {
      "type": "string",
      "enum": ["micro_pot", "small_1acre", "medium_10acre", "commercial_500acre"]
    },
    "lifecycle": { "type": "string", "enum": ["annual", "perennial", "biennial"] },
    "is_organic": { "type": "boolean" },
    "theme": {
      "type": "object",
      "required": ["id", "primary_color", "dark_mode", "density"]
    },
    "widgets": {
      "type": "array",
      "items": {
        "type": "object",
        "required": ["id", "widget_type", "priority_order", "grid_span"]
      }
    }
  }
}
    """.trimIndent()
}
