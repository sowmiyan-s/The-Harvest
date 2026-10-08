package com.example.data.model

import java.util.UUID

enum class SprayConditionRating(val label: String) {
    OPTIMAL("Optimal Window"),
    CAUTION("Marginal Caution"),
    HAZARDOUS("Do Not Spray")
}

data class SprayAdvisory(
    val rating: SprayConditionRating,
    val summary: String,
    val windSpeedKph: Double,
    val maxWindThresholdKph: Double = 15.0,
    val rainRiskPct: Int,
    val tempCelsius: Double,
    val inversionRisk: Boolean,
    val deltaTCelsius: Double, // Evaporation/droplet lifetime indicator (wet bulb depression proxy)
    val recommendedNozzle: String,
    val legalNotice: String
)

data class FieldScoutLog(
    val id: String = UUID.randomUUID().toString(),
    val farmId: String,
    val title: String,
    val cropStage: String,
    val pestOrIssue: String,
    val severityLevel: String, // "Low", "Moderate", "Critical"
    val actionTaken: String,
    val dateIso: String,
    val photoCount: Int = 1,
    val syncStatus: String = "Synced Local"
)

data class HourlyAgroForecast(
    val timeLabel: String,
    val tempCelsius: Double,
    val humidityPct: Double,
    val windSpeedKph: Double,
    val rainProbPct: Int,
    val deltaTCelsius: Double,
    val condition: SprayConditionRating
)

data class CropRotationalRecommendation(
    val season: String,
    val suggestedCrop: String,
    val family: String,
    val agronomicBenefit: String,
    val breakPestCycle: String,
    val nitrogenImpactKgPerAcre: Double
)
