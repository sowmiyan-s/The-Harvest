package com.example.data.repository

import com.example.data.model.*

class HarvestRepository {

    val availableFarms: List<FarmProfile> = listOf(
        FarmProfile(
            id = "farm_micro_01",
            name = "Sunny Balcony Garden",
            scale = FarmScale.MICRO_POT,
            cropName = "Sweet Basil & Cherry Tomato",
            cropScientificName = "Ocimum basilicum / Solanum lycopersicum",
            lifecycle = CropLifecycle.ANNUAL,
            isOrganic = true,
            areaAcres = 0.001,
            location = LocationCoords(37.7749, -122.4194, 15.0),
            locationName = "San Francisco, CA (Zone 10a)",
            soilType = "Organic Potting Mix with Perlite",
            sowingDate = "2026-03-15"
        ),
        FarmProfile(
            id = "farm_small_02",
            name = "Willow Creek Market Garden",
            scale = FarmScale.SMALL_1ACRE,
            cropName = "Heirloom Tomatoes & Leafy Greens",
            cropScientificName = "Solanum lycopersicum / Brassica oleracea",
            lifecycle = CropLifecycle.ANNUAL,
            isOrganic = true,
            areaAcres = 1.0,
            location = LocationCoords(45.5152, -122.6784, 60.0),
            locationName = "Willamette Valley, OR (Zone 8b)",
            soilType = "Rich River Silt Loam",
            sowingDate = "2026-04-10"
        ),
        FarmProfile(
            id = "farm_medium_03",
            name = "Highland Crest Orchard",
            scale = FarmScale.MEDIUM_10ACRE,
            cropName = "Honeycrisp Apples",
            cropScientificName = "Malus domestica 'Honeycrisp'",
            lifecycle = CropLifecycle.PERENNIAL,
            isOrganic = false,
            areaAcres = 10.0,
            location = LocationCoords(46.6021, -120.5059, 320.0),
            locationName = "Yakima Valley, WA (Zone 6b)",
            soilType = "Volcanic Ash Sandy Loam",
            sowingDate = null
        ),
        FarmProfile(
            id = "farm_commercial_04",
            name = "Prairie Horizon Grain Co.",
            scale = FarmScale.COMMERCIAL_500ACRE,
            cropName = "Hard Red Winter Wheat",
            cropScientificName = "Triticum aestivum",
            lifecycle = CropLifecycle.ANNUAL,
            isOrganic = false,
            areaAcres = 500.0,
            location = LocationCoords(38.5266, -98.7645, 510.0),
            locationName = "Barton County, KS (Zone 6a)",
            soilType = "Harney Silt Loam",
            sowingDate = "2025-10-02"
        )
    )

    fun getDashboardSpecForFarm(farm: FarmProfile): DashboardSpec {
        return when (farm.scale) {
            FarmScale.MICRO_POT -> DashboardSpec(
                schemaVersion = "1.0.0",
                scale = farm.scale,
                crop = farm.cropName,
                lifecycle = farm.lifecycle,
                isOrganic = farm.isOrganic,
                language = farm.language,
                copyTone = CopyTone.PLAYFUL_EDUCATIONAL,
                theme = ThemeSpec(
                    id = "pastel_nature",
                    primaryColorHex = "#2E7D32",
                    secondaryColorHex = "#81C784",
                    backgroundColorHex = "#F1F8E9",
                    surfaceColorHex = "#FFFFFF",
                    accentColorHex = "#FFB74D",
                    darkMode = false,
                    density = "comfortable"
                ),
                quickActions = listOf(
                    QuickActionItem("act_water", "Log 1 Cup Water", "WaterDrop", "log_watering"),
                    QuickActionItem("act_scrap", "Add Kitchen Scrap", "EggAlt", "log_compost"),
                    QuickActionItem("act_snap", "Diagnose Leaf", "PhotoCamera", "navigate_camera"),
                    QuickActionItem("act_ask", "Ask Copilot", "AutoAwesome", "ask_copilot")
                ),
                widgets = listOf(
                    WidgetSpecItem("w_cup_water", WidgetType.WATERING_CUP_LOG, "Cup-by-Cup Watering", 1, 1),
                    WidgetSpecItem("w_scrap_compost", WidgetType.KITCHEN_COMPOST_HELPER, "Scrap Compost Helper", 2, 1),
                    WidgetSpecItem("w_weather", WidgetType.WEATHER_SOIL_CARD, "Balcony Climate & Sun", 3, 2),
                    WidgetSpecItem("w_badges", WidgetType.MILESTONE_BADGES, "Seedling Milestones", 4, 2),
                    WidgetSpecItem("w_copilot", WidgetType.COPILOT_QUICK_ACTIONS, "Micro-Farm AI Assistant", 5, 2)
                )
            )

            FarmScale.SMALL_1ACRE -> DashboardSpec(
                schemaVersion = "1.0.0",
                scale = farm.scale,
                crop = farm.cropName,
                lifecycle = farm.lifecycle,
                isOrganic = farm.isOrganic,
                language = farm.language,
                copyTone = CopyTone.PRACTICAL_ARTISAN,
                theme = ThemeSpec(
                    id = "earth_organic",
                    primaryColorHex = "#1B5E20",
                    secondaryColorHex = "#4CAF50",
                    backgroundColorHex = "#FAFAFA",
                    surfaceColorHex = "#FFFFFF",
                    accentColorHex = "#F57F17",
                    darkMode = false,
                    density = "comfortable"
                ),
                quickActions = listOf(
                    QuickActionItem("act_irrigate", "Log Drip Cycle", "WaterDrop", "log_watering"),
                    QuickActionItem("act_amend", "Add Organic NPK", "Science", "open_calculator"),
                    QuickActionItem("act_pest", "Scout Pests", "BugReport", "navigate_camera"),
                    QuickActionItem("act_harvest", "Record Harvest", "Grass", "log_harvest")
                ),
                widgets = listOf(
                    WidgetSpecItem("w_weather_soil", WidgetType.WEATHER_SOIL_CARD, "Soil Moisture & ETc Infiltration", 1, 2),
                    WidgetSpecItem("w_timeline", WidgetType.ANNUAL_GROWTH_TIMELINE, "Season Growth Timeline", 2, 2),
                    WidgetSpecItem("w_input_calc", WidgetType.BULK_INPUT_CALCULATOR, "Compost & Organic Amendment Calculator", 3, 2),
                    WidgetSpecItem("w_ledger", WidgetType.CAPEX_OPEX_LEDGER, "Market Garden Crop Ledger", 4, 2),
                    WidgetSpecItem("w_copilot", WidgetType.COPILOT_QUICK_ACTIONS, "Agronomic Copilot", 5, 2)
                )
            )

            FarmScale.MEDIUM_10ACRE -> DashboardSpec(
                schemaVersion = "1.0.0",
                scale = farm.scale,
                crop = farm.cropName,
                lifecycle = farm.lifecycle,
                isOrganic = farm.isOrganic,
                language = farm.language,
                copyTone = CopyTone.COMMERCIAL_AGRONOMIC,
                theme = ThemeSpec(
                    id = "orchard_amber",
                    primaryColorHex = "#E65100",
                    secondaryColorHex = "#FF9800",
                    backgroundColorHex = "#FFF8E1",
                    surfaceColorHex = "#FFFFFF",
                    accentColorHex = "#2E7D32",
                    darkMode = false,
                    density = "comfortable"
                ),
                quickActions = listOf(
                    QuickActionItem("act_chill", "Check Chilling", "AcUnit", "open_chilling"),
                    QuickActionItem("act_roi", "Update CapEx", "TrendingUp", "open_roi"),
                    QuickActionItem("act_spray", "Spray Compliance", "Shield", "ask_guardrail"),
                    QuickActionItem("act_weather", "Frost Alert", "Thermostat", "view_weather")
                ),
                widgets = listOf(
                    WidgetSpecItem("w_chilling", WidgetType.WINTER_CHILLING_GAUGE, "Winter Chilling Hours Accumulator", 1, 1),
                    WidgetSpecItem("w_weather", WidgetType.WEATHER_SOIL_CARD, "Orchard Microclimate & ETc", 2, 1),
                    WidgetSpecItem("w_roi", WidgetType.MULTI_YEAR_ROI_TRACKER, "Multi-Year Orchard ROI Projection", 3, 2),
                    WidgetSpecItem("w_calc", WidgetType.BULK_INPUT_CALCULATOR, "Foliar Nutrition & Irrigation Sizing", 4, 2),
                    WidgetSpecItem("w_ledger", WidgetType.CAPEX_OPEX_LEDGER, "Orchard CapEx / OpEx Breakdown", 5, 2)
                )
            )

            FarmScale.COMMERCIAL_500ACRE -> DashboardSpec(
                schemaVersion = "1.0.0",
                scale = farm.scale,
                crop = farm.cropName,
                lifecycle = farm.lifecycle,
                isOrganic = farm.isOrganic,
                language = farm.language,
                copyTone = CopyTone.COMMERCIAL_AGRONOMIC,
                theme = ThemeSpec(
                    id = "industrial_dark",
                    primaryColorHex = "#4CAF50",
                    secondaryColorHex = "#81C784",
                    backgroundColorHex = "#121212",
                    surfaceColorHex = "#1E1E1E",
                    accentColorHex = "#00E676",
                    darkMode = true,
                    density = "data_dense"
                ),
                quickActions = listOf(
                    QuickActionItem("act_ndvi", "Sentinel-2 NDVI", "SatelliteAlt", "view_ndvi"),
                    QuickActionItem("act_bulk", "Bulk Fertilizer", "LocalShipping", "open_calculator"),
                    QuickActionItem("act_fuel", "Equipment Log", "PrecisionManufacturing", "log_fuel"),
                    QuickActionItem("act_copilot", "Enterprise Ag Copilot", "AutoAwesome", "ask_copilot")
                ),
                widgets = listOf(
                    WidgetSpecItem("w_ndvi", WidgetType.SATELLITE_NDVI_MAP, "Sentinel-2 Field Polygon NDVI", 1, 2),
                    WidgetSpecItem("w_weather_dense", WidgetType.WEATHER_SOIL_CARD, "Multi-Sensor Telemetry & Infiltration", 2, 2),
                    WidgetSpecItem("w_bulk_calc", WidgetType.BULK_INPUT_CALCULATOR, "500-Acre Bulk N-P-K & Anhydrous Ammonia", 3, 2),
                    WidgetSpecItem("w_dense_ledger", WidgetType.CAPEX_OPEX_LEDGER, "Industrial CapEx/OpEx Cost Centers", 4, 2),
                    WidgetSpecItem("w_copilot", WidgetType.COPILOT_QUICK_ACTIONS, "Agronomic Optimization Engine", 5, 2)
                )
            )
        }
    }
}
