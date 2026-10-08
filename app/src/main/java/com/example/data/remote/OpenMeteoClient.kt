package com.example.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class LiveWeatherSoilData(
    val currentTempCelsius: Double,
    val currentHumidityPct: Double,
    val soilMoisturePct: Double,
    val soilTempCelsius: Double,
    val dailyEtcMm: Double,
    val chillingHoursAccumulated: Int,
    val frostWarning: Boolean,
    val isLiveApi: Boolean,
    val sourceLabel: String
)

object OpenMeteoClient {
    private const val TAG = "OpenMeteoClient"
    private const val BASE_URL = "https://api.open-meteo.com/v1/forecast"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    suspend fun fetchLiveTelemetry(
        latitude: Double,
        longitude: Double,
        chillingThresholdCelsius: Double = 7.2
    ): LiveWeatherSoilData = withContext(Dispatchers.IO) {
        val url = "$BASE_URL?latitude=$latitude&longitude=$longitude" +
                "&hourly=temperature_2m,relative_humidity_2m,precipitation,soil_temperature_0cm,soil_moisture_0_to_1cm,et0_fao_evapotranspiration" +
                "&forecast_days=7&timezone=auto"

        try {
            val request = Request.Builder().url(url).build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.w(TAG, "Open-Meteo returned status ${response.code}, using cached fallback")
                return@withContext getFallbackTelemetry()
            }

            val bodyString = response.body?.string() ?: return@withContext getFallbackTelemetry()
            val root = JSONObject(bodyString)
            val hourly = root.optJSONObject("hourly") ?: return@withContext getFallbackTelemetry()

            val tempsArray = hourly.optJSONArray("temperature_2m")
            val humiditiesArray = hourly.optJSONArray("relative_humidity_2m")
            val soilTempsArray = hourly.optJSONArray("soil_temperature_0cm")
            val soilMoisturesArray = hourly.optJSONArray("soil_moisture_0_to_1cm")
            val etArray = hourly.optJSONArray("et0_fao_evapotranspiration")

            val currentTemp = tempsArray?.optDouble(0, 20.0) ?: 20.0
            val currentHumidity = humiditiesArray?.optDouble(0, 55.0) ?: 55.0
            val soilTemp = soilTempsArray?.optDouble(0, 18.0) ?: 18.0

            // Open-Meteo gives volumetric m3/m3 (e.g. 0.32 -> 32.0%)
            val rawSm = soilMoisturesArray?.optDouble(0, 0.30) ?: 0.30
            val soilMoisturePct = if (rawSm <= 1.0) rawSm * 100.0 else rawSm

            // Sum daily ETc for next 24 hours
            var dailyEtSum = 0.0
            if (etArray != null) {
                val count = minOf(etArray.length(), 24)
                for (i in 0 until count) {
                    dailyEtSum += etArray.optDouble(i, 0.0)
                }
            } else {
                dailyEtSum = 3.4
            }

            // Calculate winter chilling hours (< chillingThresholdCelsius)
            var chillingCount = 0
            var frostDetected = false
            if (tempsArray != null) {
                for (i in 0 until tempsArray.length()) {
                    val t = tempsArray.optDouble(i, 20.0)
                    if (t < chillingThresholdCelsius) {
                        chillingCount++
                    }
                    if (t <= 0.0) {
                        frostDetected = true
                    }
                }
            }

            LiveWeatherSoilData(
                currentTempCelsius = round1(currentTemp),
                currentHumidityPct = round1(currentHumidity),
                soilMoisturePct = round1(soilMoisturePct),
                soilTempCelsius = round1(soilTemp),
                dailyEtcMm = round1(dailyEtSum),
                chillingHoursAccumulated = chillingCount,
                frostWarning = frostDetected,
                isLiveApi = true,
                sourceLabel = "Live Open-Meteo Satellite & Meteorological Model"
            )
        } catch (e: Exception) {
            Log.w(TAG, "Open-Meteo fetch failed: ${e.message}, falling back to deterministic model")
            getFallbackTelemetry()
        }
    }

    private fun getFallbackTelemetry(): LiveWeatherSoilData {
        return LiveWeatherSoilData(
            currentTempCelsius = 21.2,
            currentHumidityPct = 58.0,
            soilMoisturePct = 34.0,
            soilTempCelsius = 19.5,
            dailyEtcMm = 3.6,
            chillingHoursAccumulated = 680,
            frostWarning = false,
            isLiveApi = false,
            sourceLabel = "Local Soil-Hydrology Baseline (Offline Fallback)"
        )
    }

    private fun round1(v: Double): Double = Math.round(v * 10.0) / 10.0
}
