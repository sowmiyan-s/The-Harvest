package com.example.data.model

// ASSUMPTION: 4 standardized scales cover urban balcony up to commercial grain
enum class FarmScale(val displayName: String, val acreageDisplay: String) {
    MICRO_POT("Micro Pot / Balcony", "0.001 Acre (Containers)"),
    SMALL_1ACRE("Market Garden", "1.0 Acre"),
    MEDIUM_10ACRE("Diversified Orchard", "10.0 Acres"),
    COMMERCIAL_500ACRE("Industrial Broadacre", "500.0 Acres")
}

enum class CropLifecycle(val displayName: String) {
    ANNUAL("Annual (Single Season)"),
    PERENNIAL("Perennial (Multi-Year)"),
    BIENNIAL("Biennial (Two Season)")
}

enum class CopyTone(val title: String) {
    PLAYFUL_EDUCATIONAL("Playful & Educational"),
    PRACTICAL_ARTISAN("Practical & Artisan"),
    COMMERCIAL_AGRONOMIC("High-Density Agronomic")
}

data class LocationCoords(
    val latitude: Double,
    val longitude: Double,
    val elevationMeters: Double? = null
)

data class FarmProfile(
    val id: String,
    val name: String,
    val scale: FarmScale,
    val cropName: String,
    val cropScientificName: String,
    val lifecycle: CropLifecycle,
    val isOrganic: Boolean,
    val areaAcres: Double,
    val location: LocationCoords,
    val locationName: String,
    val timezone: String = "UTC",
    val language: String = "en",
    val soilType: String = "Silty Loam",
    val sowingDate: String? = null
)

data class ThemeSpec(
    val id: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val backgroundColorHex: String,
    val surfaceColorHex: String,
    val accentColorHex: String,
    val darkMode: Boolean,
    val density: String // "compact", "comfortable", "data_dense"
)

enum class WidgetType(val label: String) {
    WATERING_CUP_LOG("Cup-by-Cup Watering Log"),
    KITCHEN_COMPOST_HELPER("Kitchen Scrap Compost Helper"),
    MILESTONE_BADGES("Seedling Milestone Badges"),
    WEATHER_SOIL_CARD("Live Weather & Soil Infiltration"),
    BULK_INPUT_CALCULATOR("Bulk Inputs & Amendments Calculator"),
    SATELLITE_NDVI_MAP("Sentinel-2 NDVI Polygon Map"),
    CAPEX_OPEX_LEDGER("CapEx & OpEx Expense Ledger"),
    WINTER_CHILLING_GAUGE("Winter Chilling Hours Accumulator"),
    MULTI_YEAR_ROI_TRACKER("Multi-Year Perennial ROI Tracker"),
    ANNUAL_GROWTH_TIMELINE("Single-Season Sowing Timeline"),
    COPILOT_QUICK_ACTIONS("Copilot AI Quick Actions")
}

data class WidgetSpecItem(
    val id: String,
    val widgetType: WidgetType,
    val title: String,
    val priorityOrder: Int,
    val gridSpan: Int, // 1 or 2 in mobile column
    val params: Map<String, String> = emptyMap()
)

data class QuickActionItem(
    val id: String,
    val label: String,
    val iconName: String,
    val actionType: String
)

data class DashboardSpec(
    val schemaVersion: String = "1.0.0",
    val scale: FarmScale,
    val crop: String,
    val lifecycle: CropLifecycle,
    val isOrganic: Boolean,
    val language: String = "en",
    val copyTone: CopyTone,
    val theme: ThemeSpec,
    val quickActions: List<QuickActionItem>,
    val widgets: List<WidgetSpecItem>
)

data class CalculationAssumption(
    val formulaName: String,
    val baseRequirement: String,
    val creditSubtraction: String,
    val efficiencyFactor: Double,
    val disclaimer: String
)
