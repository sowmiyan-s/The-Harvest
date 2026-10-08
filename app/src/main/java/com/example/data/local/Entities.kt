package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farms")
data class FarmEntity(
    @PrimaryKey val id: String,
    val name: String,
    val scale: String, // micro_pot, small_1acre, medium_10acre, commercial_500acre
    val cropName: String,
    val cropScientificName: String,
    val lifecycle: String, // annual, perennial
    val isOrganic: Boolean,
    val areaAcres: Double,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val soilType: String,
    val language: String = "en",
    val timezone: String = "UTC",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "log_entries")
data class LogEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val farmId: String,
    val category: String, // watering, compost, fertilizer, harvest, expense_capex, expense_opex
    val quantity: Double,
    val unit: String,
    val costAmount: Double = 0.0,
    val currency: String = "USD",
    val notes: String = "",
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "observations")
data class ObservationEntity(
    @PrimaryKey val id: String,
    val farmId: String,
    val obsType: String, // weather_soil, satellite_ndvi, plant_diagnosis
    val tempCelsius: Double?,
    val humidityPct: Double?,
    val soilMoisturePct: Double?,
    val meanNdvi: Double?,
    val source: String,
    val recordedAt: Long = System.currentTimeMillis()
)
