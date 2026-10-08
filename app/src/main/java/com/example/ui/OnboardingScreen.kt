package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onDismiss: () -> Unit,
    onComplete: (FarmProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) } // 1: Scale, 2: Crop, 3: Location, 4: Organic/Soil, 5: Review & Morph

    // User input state
    var selectedScale by remember { mutableStateOf(FarmScale.MICRO_POT) }
    var cropName by remember { mutableStateOf("Sweet Basil & Cherry Tomato") }
    var cropScientificName by remember { mutableStateOf("Ocimum basilicum") }
    var cropLifecycle by remember { mutableStateOf(CropLifecycle.ANNUAL) }
    var farmName by remember { mutableStateOf("My Green Oasis") }
    var locationName by remember { mutableStateOf("San Francisco, CA (Zone 10a)") }
    var latitude by remember { mutableStateOf(37.7749) }
    var longitude by remember { mutableStateOf(-122.4194) }
    var isOrganic by remember { mutableStateOf(true) }
    var soilType by remember { mutableStateOf("Organic Potting Mix with Perlite") }
    var language by remember { mutableStateOf("en") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Onboard New Farm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Step $step of 5 • The Harvest Engine", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_onboarding")) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        OutlinedButton(onClick = { step-- }) {
                            Text("Back")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    if (step < 5) {
                        Button(
                            onClick = { step++ },
                            modifier = Modifier.testTag("btn_onboarding_next"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E))
                        ) {
                            Text("Next")
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    } else {
                        Button(
                            onClick = {
                                val area = when (selectedScale) {
                                    FarmScale.MICRO_POT -> 0.001
                                    FarmScale.SMALL_1ACRE -> 1.0
                                    FarmScale.MEDIUM_10ACRE -> 10.0
                                    FarmScale.COMMERCIAL_500ACRE -> 500.0
                                }
                                val newFarm = FarmProfile(
                                    id = "farm_${UUID.randomUUID().toString().take(8)}",
                                    name = farmName.ifBlank { "New ${selectedScale.displayName}" },
                                    scale = selectedScale,
                                    cropName = cropName.ifBlank { "Mixed Crops" },
                                    cropScientificName = cropScientificName,
                                    lifecycle = cropLifecycle,
                                    isOrganic = isOrganic,
                                    areaAcres = area,
                                    location = LocationCoords(latitude, longitude),
                                    locationName = locationName,
                                    soilType = soilType,
                                    language = language
                                )
                                onComplete(newFarm)
                            },
                            modifier = Modifier.testTag("btn_onboarding_finish"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Compile & Launch Dashboard", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step Progress Bar
            LinearProgressIndicator(
                progress = { step / 5f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF1B4D3E),
                trackColor = Color(0xFFE0E0E0)
            )

            when (step) {
                1 -> StepScaleSelection(
                    selectedScale = selectedScale,
                    onSelectScale = { scale ->
                        selectedScale = scale
                        // Set sensible crop and soil defaults matching scale
                        when (scale) {
                            FarmScale.MICRO_POT -> {
                                farmName = "Balcony Kitchen Herb Garden"
                                cropName = "Sweet Basil & Cherry Tomato"
                                cropScientificName = "Ocimum basilicum"
                                cropLifecycle = CropLifecycle.ANNUAL
                                soilType = "Organic Potting Mix with Perlite"
                            }
                            FarmScale.SMALL_1ACRE -> {
                                farmName = "Valley Meadow Market Garden"
                                cropName = "Heirloom Tomatoes & Leafy Greens"
                                cropScientificName = "Solanum lycopersicum"
                                cropLifecycle = CropLifecycle.ANNUAL
                                soilType = "River Silt Loam"
                            }
                            FarmScale.MEDIUM_10ACRE -> {
                                farmName = "Sunrise Ridge Orchard"
                                cropName = "Honeycrisp Apples"
                                cropScientificName = "Malus domestica 'Honeycrisp'"
                                cropLifecycle = CropLifecycle.PERENNIAL
                                soilType = "Volcanic Ash Sandy Loam"
                            }
                            FarmScale.COMMERCIAL_500ACRE -> {
                                farmName = "Horizon Broadacre Prairie Co."
                                cropName = "Hard Red Winter Wheat"
                                cropScientificName = "Triticum aestivum"
                                cropLifecycle = CropLifecycle.ANNUAL
                                soilType = "Harney Silt Loam"
                            }
                        }
                    }
                )

                2 -> StepCropAndLifecycle(
                    cropName = cropName,
                    onCropNameChange = { cropName = it },
                    cropScientificName = cropScientificName,
                    onScientificNameChange = { cropScientificName = it },
                    lifecycle = cropLifecycle,
                    onLifecycleChange = { cropLifecycle = it }
                )

                3 -> StepLocationSelection(
                    locationName = locationName,
                    onLocationNameChange = { locationName = it },
                    latitude = latitude,
                    onLatitudeChange = { latitude = it },
                    longitude = longitude,
                    onLongitudeChange = { longitude = it }
                )

                4 -> StepMethodologyAndSoil(
                    farmName = farmName,
                    onFarmNameChange = { farmName = it },
                    isOrganic = isOrganic,
                    onOrganicChange = { isOrganic = it },
                    soilType = soilType,
                    onSoilTypeChange = { soilType = it }
                )

                5 -> StepReviewAndMorph(
                    farmName = farmName,
                    scale = selectedScale,
                    cropName = cropName,
                    lifecycle = cropLifecycle,
                    isOrganic = isOrganic,
                    locationName = locationName,
                    soilType = soilType
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Onboarding Step 1: Scale Selection
// -----------------------------------------------------------------------------
@Composable
fun StepScaleSelection(
    selectedScale: FarmScale,
    onSelectScale: (FarmScale) -> Unit
) {
    Text("Select Your Farming Scale", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(
        "The Harvest adapts its UI theme, widgets, density, and advice based on your operating scale. A child growing herbs and a commercial grain producer experience two distinct apps built on one engine.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))

    val scales = listOf(
        Triple(FarmScale.MICRO_POT, "Containers, balcony herb boxes, indoor pots (0.001 Acre)", Icons.Filled.LocalFlorist),
        Triple(FarmScale.SMALL_1ACRE, "Direct-market vegetable plots, permaculture homestead (1.0 Acre)", Icons.Filled.Yard),
        Triple(FarmScale.MEDIUM_10ACRE, "Diversified fruit orchard, vineyard, commercial berries (10.0 Acres)", Icons.Filled.Park),
        Triple(FarmScale.COMMERCIAL_500ACRE, "Industrial row crops, grains, high-acreage broadacre (500.0 Acres)", Icons.Filled.Agriculture)
    )

    scales.forEach { (scale, description, icon) ->
        val isSelected = selectedScale == scale
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectScale(scale) }
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) Color(0xFF2E7D32) else Color(0xFFE0E0E0),
                    shape = RoundedCornerShape(14.dp)
                )
                .testTag("scale_option_${scale.name}"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) Color(0xFF2E7D32) else Color(0xFFEEEEEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isSelected) Color.White else Color(0xFF616161),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(scale.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                RadioButton(
                    selected = isSelected,
                    onClick = { onSelectScale(scale) }
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Onboarding Step 2: Crop & Lifecycle
// -----------------------------------------------------------------------------
@Composable
fun StepCropAndLifecycle(
    cropName: String,
    onCropNameChange: (String) -> Unit,
    cropScientificName: String,
    onScientificNameChange: (String) -> Unit,
    lifecycle: CropLifecycle,
    onLifecycleChange: (CropLifecycle) -> Unit
) {
    Text("Primary Crop & Botanical Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(
        "Agronomic calculations, winter chilling thresholds, and water requirements depend on crop species and lifecycle duration.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedTextField(
        value = cropName,
        onValueChange = onCropNameChange,
        label = { Text("Common Crop Name") },
        placeholder = { Text("e.g. Heirloom Tomatoes, Honeycrisp Apples") },
        modifier = Modifier.fillMaxWidth().testTag("input_crop_name"),
        shape = RoundedCornerShape(12.dp)
    )

    OutlinedTextField(
        value = cropScientificName,
        onValueChange = onScientificNameChange,
        label = { Text("Scientific Genus / Species (Optional)") },
        placeholder = { Text("e.g. Solanum lycopersicum, Malus domestica") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )

    Text("Crop Botanical Lifecycle:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = lifecycle == CropLifecycle.ANNUAL,
            onClick = { onLifecycleChange(CropLifecycle.ANNUAL) },
            label = { Text("Annual (Single Season)") },
            leadingIcon = { Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
        FilterChip(
            selected = lifecycle == CropLifecycle.PERENNIAL,
            onClick = { onLifecycleChange(CropLifecycle.PERENNIAL) },
            label = { Text("Perennial (Multi-Year)") },
            leadingIcon = { Icon(Icons.Filled.Forest, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
    }
}

// -----------------------------------------------------------------------------
// Onboarding Step 3: Location & Coordinates
// -----------------------------------------------------------------------------
@Composable
fun StepLocationSelection(
    locationName: String,
    onLocationNameChange: (String) -> Unit,
    latitude: Double,
    onLatitudeChange: (Double) -> Unit,
    longitude: Double,
    onLongitudeChange: (Double) -> Unit
) {
    Text("Farm Geography & Climate Grid", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(
        "Open-Meteo queries live weather, solar radiation, and soil temperature using these WGS84 coordinates (cached in 1.1km grid cells).",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    OutlinedTextField(
        value = locationName,
        onValueChange = onLocationNameChange,
        label = { Text("Location / Region Label") },
        placeholder = { Text("e.g. Willamette Valley, OR (Zone 8b)") },
        modifier = Modifier.fillMaxWidth().testTag("input_location_name"),
        shape = RoundedCornerShape(12.dp)
    )

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = latitude.toString(),
            onValueChange = { it.toDoubleOrNull()?.let { lat -> onLatitudeChange(lat) } },
            label = { Text("Latitude") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        )
        OutlinedTextField(
            value = longitude.toString(),
            onValueChange = { it.toDoubleOrNull()?.let { lon -> onLongitudeChange(lon) } },
            label = { Text("Longitude") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        )
    }

    Text("Quick Geographic Presets:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AssistChip(
            onClick = {
                onLocationNameChange("San Francisco, CA (Zone 10a)")
                onLatitudeChange(37.7749)
                onLongitudeChange(-122.4194)
            },
            label = { Text("Urban SF (37.77, -122.42)") }
        )
        AssistChip(
            onClick = {
                onLocationNameChange("Willamette Valley, OR (Zone 8b)")
                onLatitudeChange(45.5152)
                onLongitudeChange(-122.6784)
            },
            label = { Text("Pacific NW (45.51, -122.67)") }
        )
        AssistChip(
            onClick = {
                onLocationNameChange("Yakima Valley, WA (Zone 6b)")
                onLatitudeChange(46.6021)
                onLongitudeChange(-120.5059)
            },
            label = { Text("Yakima Orchard (46.60, -120.50)") }
        )
        AssistChip(
            onClick = {
                onLocationNameChange("Kansas Prairie, KS (Zone 6a)")
                onLatitudeChange(38.5266)
                onLongitudeChange(-98.7645)
            },
            label = { Text("Kansas Grain (38.52, -98.76)") }
        )
    }
}

// -----------------------------------------------------------------------------
// Onboarding Step 4: Methodology & Soil
// -----------------------------------------------------------------------------
@Composable
fun StepMethodologyAndSoil(
    farmName: String,
    onFarmNameChange: (String) -> Unit,
    isOrganic: Boolean,
    onOrganicChange: (Boolean) -> Unit,
    soilType: String,
    onSoilTypeChange: (String) -> Unit
) {
    Text("Methodology & Soil Profile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

    OutlinedTextField(
        value = farmName,
        onValueChange = onFarmNameChange,
        label = { Text("Farm / Holding Name") },
        placeholder = { Text("e.g. Willow Creek Homestead") },
        modifier = Modifier.fillMaxWidth().testTag("input_farm_name"),
        shape = RoundedCornerShape(12.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOrganic) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isOrganic) "100% Certified Organic Holding" else "Conventional Farming Program",
                    fontWeight = FontWeight.Bold,
                    color = if (isOrganic) Color(0xFF1B5E20) else Color.Black
                )
                Text(
                    if (isOrganic) "Guardrail Agent activates strict USDA NOP §205.105 rule filter, blocking prohibited synthetic chemicals."
                    else "Standard chemical extension guidelines applied with mandatory EPA worker protection standards.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(
                checked = isOrganic,
                onCheckedChange = onOrganicChange,
                modifier = Modifier.testTag("switch_organic")
            )
        }
    }

    OutlinedTextField(
        value = soilType,
        onValueChange = onSoilTypeChange,
        label = { Text("Soil Texture / Medium") },
        placeholder = { Text("e.g. Silt Loam, Sandy Loam, Peat Potting Mix") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    )
}

// -----------------------------------------------------------------------------
// Onboarding Step 5: Review & Morph Engine
// -----------------------------------------------------------------------------
@Composable
fun StepReviewAndMorph(
    farmName: String,
    scale: FarmScale,
    cropName: String,
    lifecycle: CropLifecycle,
    isOrganic: Boolean,
    locationName: String,
    soilType: String
) {
    Text("Compile Persona & Dashboard Spec", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(
        "Review your parameters. The Persona/UI Agent will compile a tailored DashboardSpec JSON and immediately morph the app shell.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Verified, contentDescription = null, tint = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.width(8.dp))
                Text(farmName.ifBlank { "New Farm" }, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            HorizontalDivider()
            Text("• Scale: ${scale.displayName} (${scale.acreageDisplay})", fontSize = 13.sp)
            Text("• Crop: $cropName (${lifecycle.displayName})", fontSize = 13.sp)
            Text("• Method: ${if (isOrganic) "Certified Organic (NOP Guardrail Active)" else "Conventional"}", fontSize = 13.sp)
            Text("• Location: $locationName", fontSize = 13.sp)
            Text("• Soil: $soilType", fontSize = 13.sp)
        }
    }

    Surface(
        color = Color(0xFF212121),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("SERVER-DRIVEN COMPILER PREVIEW:", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Theme: ${if (scale == FarmScale.COMMERCIAL_500ACRE) "industrial_dark" else if (scale == FarmScale.MEDIUM_10ACRE) "orchard_amber" else "earth_organic"}\n" +
                "Tone: ${if (scale == FarmScale.MICRO_POT) "playful_educational" else if (scale == FarmScale.SMALL_1ACRE) "practical_artisan" else "commercial_agronomic"}\n" +
                "Widgets: Automatic layout hierarchy generated from schema v1.0.0",
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                fontSize = 11.sp,
                color = Color.White
            )
        }
    }
}
