package com.example.data.remote

import android.util.Log
import com.example.data.model.HourlyAgroForecast
import com.example.data.model.SprayConditionRating
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class LiveWeatherSoilData(
    val currentTempCelsius: Double,
    val apparentTempCelsius: Double,
    val currentHumidityPct: Double,
    val dewPointCelsius: Double,
    val windSpeedKph: Double,
    val windDirectionDeg: Int,
    val windGustsKph: Double,
    val precipitationMm: Double,
    val rainProbabilityPct: Int,
    val weatherCode: Int,
    val weatherDescription: String,
    val surfacePressureHpa: Double,
    val uvIndex: Double,
    val soilMoisturePct: Double,
    val soilTempCelsius: Double,
    val dailyEtcMm: Double,
    val deltaTCelsius: Double,
    val chillingHoursAccumulated: Int,
    val frostWarning: Boolean,
    val isLiveApi: Boolean,
    val sourceLabel: String,
    val lastUpdatedIso: String,
    val hourlyForecast: List<HourlyAgroForecast> = emptyList()
)

object OpenMeteoClient {
    private const val TAG = "OpenMeteoClient"
    private const val BASE_URL = "https://api.open-meteo.com/v1/forecast"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(9, TimeUnit.SECONDS)
        .readTimeout(9, TimeUnit.SECONDS)
        .build()

    /**
     * Stull (2011) psychrometric wet-bulb temperature formulation:
     * Tw = T * atan(0.151977 * sqrt(RH + 8.313659)) + atan(T + RH) - atan(RH - 1.676331)
     *      + 0.00391838 * RH^(3/2) * atan(0.023101 * RH) - 4.686035
     * Delta T = T - Tw
     */
    fun calculateDeltaT(tempCelsius: Double, relativeHumidityPct: Double): Double {
        val rh = relativeHumidityPct.coerceIn(1.0, 100.0)
        val t = tempCelsius
        val tw = t * atan(0.151977 * sqrt(rh + 8.313659)) +
                atan(t + rh) -
                atan(rh - 1.676331) +
                0.00391838 * rh.pow(1.5) * atan(0.023101 * rh) -
                4.686035
        val delta = t - tw
        return (round1(delta)).coerceAtLeast(0.0)
    }

    suspend fun fetchLiveTelemetry(
        latitude: Double,
        longitude: Double,
        chillingThresholdCelsius: Double = 7.2
    ): LiveWeatherSoilData = withContext(Dispatchers.IO) {
        val url = "$BASE_URL?latitude=$latitude&longitude=$longitude" +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,rain,weather_code,wind_speed_10m,wind_direction_10m,wind_gusts_10m,surface_pressure" +
                "&hourly=temperature_2m,relative_humidity_2m,dew_point_2m,precipitation_probability,wind_speed_10m,soil_temperature_0cm,soil_moisture_0_to_1cm,uv_index,et0_fao_evapotranspiration" +
                "&forecast_days=3&timezone=auto"

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "TheHarvestAgronomyApp/2.0")
                .build()
            val response = httpClient.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.w(TAG, "Open-Meteo returned status ${response.code}, using baseline")
                return@withContext getFallbackTelemetry()
            }

            val bodyString = response.body?.string() ?: return@withContext getFallbackTelemetry()
            val root = JSONObject(bodyString)

            val currentObj = root.optJSONObject("current")
            val hourlyObj = root.optJSONObject("hourly")

            val currentTemp = currentObj?.optDouble("temperature_2m", 21.5) ?: 21.5
            val currentApparent = currentObj?.optDouble("apparent_temperature", currentTemp) ?: currentTemp
            val currentHumidity = currentObj?.optDouble("relative_humidity_2m", 58.0) ?: 58.0
            val windSpeed = currentObj?.optDouble("wind_speed_10m", 11.2) ?: 11.2
            val windDirection = currentObj?.optInt("wind_direction_10m", 180) ?: 180
            val windGusts = currentObj?.optDouble("wind_gusts_10m", 14.5) ?: 14.5
            val precip = currentObj?.optDouble("precipitation", 0.0) ?: 0.0
            val weatherCode = currentObj?.optInt("weather_code", 1) ?: 1
            val surfacePressure = currentObj?.optDouble("surface_pressure", 1013.2) ?: 1013.2

            val deltaT = calculateDeltaT(currentTemp, currentHumidity)

            // Hourly arrays
            val tempsArray = hourlyObj?.optJSONArray("temperature_2m")
            val humiditiesArray = hourlyObj?.optJSONArray("relative_humidity_2m")
            val dewPointsArray = hourlyObj?.optJSONArray("dew_point_2m")
            val rainProbsArray = hourlyObj?.optJSONArray("precipitation_probability")
            val windSpeedsArray = hourlyObj?.optJSONArray("wind_speed_10m")
            val soilTempsArray = hourlyObj?.optJSONArray("soil_temperature_0cm")
            val soilMoisturesArray = hourlyObj?.optJSONArray("soil_moisture_0_to_1cm")
            val uvArray = hourlyObj?.optJSONArray("uv_index")
            val etArray = hourlyObj?.optJSONArray("et0_fao_evapotranspiration")
            val timesArray = hourlyObj?.optJSONArray("time")

            val dewPoint = dewPointsArray?.optDouble(0, 11.0) ?: 11.0
            val soilTemp = soilTempsArray?.optDouble(0, 18.5) ?: 18.5
            val rawSm = soilMoisturesArray?.optDouble(0, 0.32) ?: 0.32
            val soilMoisturePct = if (rawSm <= 1.0) rawSm * 100.0 else rawSm
            val uvIndex = uvArray?.optDouble(0, 4.2) ?: 4.2

            var dailyEtSum = 0.0
            if (etArray != null) {
                val count = minOf(etArray.length(), 24)
                for (i in 0 until count) {
                    dailyEtSum += etArray.optDouble(i, 0.0)
                }
            } else {
                dailyEtSum = 3.6
            }

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

            // Map next 12 hours forecast
            val hourlyForecastList = mutableListOf<HourlyAgroForecast>()
            if (timesArray != null) {
                val limit = minOf(timesArray.length(), 12)
                for (i in 0 until limit) {
                    val timeRaw = timesArray.optString(i, "")
                    val timeLabel = if (timeRaw.contains("T")) {
                        timeRaw.substringAfter("T").take(5)
                    } else {
                        "+${i}h"
                    }
                    val hTemp = tempsArray?.optDouble(i, currentTemp) ?: currentTemp
                    val hHumid = humiditiesArray?.optDouble(i, currentHumidity) ?: currentHumidity
                    val hWind = windSpeedsArray?.optDouble(i, windSpeed) ?: windSpeed
                    val hRainProb = rainProbsArray?.optInt(i, 10) ?: 10
                    val hDeltaT = calculateDeltaT(hTemp, hHumid)

                    val condition = when {
                        hWind > 18.0 || hRainProb >= 50 || (hWind < 3.0 && hTemp < 15.0) -> SprayConditionRating.HAZARDOUS
                        hDeltaT > 8.0 || hDeltaT < 2.0 || hTemp > 30.0 -> SprayConditionRating.CAUTION
                        else -> SprayConditionRating.OPTIMAL
                    }

                    hourlyForecastList.add(
                        HourlyAgroForecast(
                            timeLabel = timeLabel,
                            tempCelsius = round1(hTemp),
                            humidityPct = round1(hHumid),
                            windSpeedKph = round1(hWind),
                            rainProbPct = hRainProb,
                            deltaTCelsius = hDeltaT,
                            condition = condition
                        )
                    )
                }
            }

            val nowFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

            LiveWeatherSoilData(
                currentTempCelsius = round1(currentTemp),
                apparentTempCelsius = round1(currentApparent),
                currentHumidityPct = round1(currentHumidity),
                dewPointCelsius = round1(dewPoint),
                windSpeedKph = round1(windSpeed),
                windDirectionDeg = windDirection,
                windGustsKph = round1(windGusts),
                precipitationMm = round1(precip),
                rainProbabilityPct = rainProbsArray?.optInt(0, 5) ?: 5,
                weatherCode = weatherCode,
                weatherDescription = decodeWeatherCode(weatherCode),
                surfacePressureHpa = round1(surfacePressure),
                uvIndex = round1(uvIndex),
                soilMoisturePct = round1(soilMoisturePct),
                soilTempCelsius = round1(soilTemp),
                dailyEtcMm = round1(dailyEtSum),
                deltaTCelsius = deltaT,
                chillingHoursAccumulated = chillingCount,
                frostWarning = frostDetected,
                isLiveApi = true,
                sourceLabel = "Live Open-Meteo Satellite & Meteorological Model",
                lastUpdatedIso = nowFormatted,
                hourlyForecast = hourlyForecastList
            )
        } catch (e: Exception) {
            Log.w(TAG, "Open-Meteo fetch failed: ${e.message}, falling back to local model")
            getFallbackTelemetry()
        }
    }

    private fun decodeWeatherCode(code: Int): String {
        return when (code) {
            0 -> "Clear Sky"
            1 -> "Mainly Clear"
            2 -> "Partly Cloudy"
            3 -> "Overcast"
            45, 48 -> "Foggy / Rime Fog"
            51, 53, 55 -> "Drizzle"
            61, 63, 65 -> "Rain Showers"
            71, 73, 75 -> "Snow Fall"
            80, 81, 82 -> "Rain Showers"
            95, 96, 99 -> "Thunderstorm Alert"
            else -> "Atmospheric Haze / Clouds"
        }
    }

    private fun getFallbackTelemetry(): LiveWeatherSoilData {
        val fallbackTemp = 21.2
        val fallbackHumidity = 58.0
        val nowFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val deltaT = calculateDeltaT(fallbackTemp, fallbackHumidity)

        val fallbackHourly = listOf(
            HourlyAgroForecast("08:00", 18.2, 70.0, 6.2, 5, 2.8, SprayConditionRating.OPTIMAL),
            HourlyAgroForecast("10:00", 21.5, 58.0, 9.4, 10, 4.2, SprayConditionRating.OPTIMAL),
            HourlyAgroForecast("12:00", 24.8, 48.0, 12.1, 15, 6.1, SprayConditionRating.OPTIMAL),
            HourlyAgroForecast("14:00", 26.5, 42.0, 16.5, 20, 8.2, SprayConditionRating.CAUTION),
            HourlyAgroForecast("16:00", 25.1, 46.0, 19.2, 25, 7.4, SprayConditionRating.HAZARDOUS),
            HourlyAgroForecast("18:00", 22.0, 56.0, 11.0, 10, 4.5, SprayConditionRating.OPTIMAL)
        )

        return LiveWeatherSoilData(
            currentTempCelsius = fallbackTemp,
            apparentTempCelsius = 20.8,
            currentHumidityPct = fallbackHumidity,
            dewPointCelsius = 12.4,
            windSpeedKph = 9.8,
            windDirectionDeg = 195,
            windGustsKph = 14.0,
            precipitationMm = 0.0,
            rainProbabilityPct = 10,
            weatherCode = 1,
            weatherDescription = "Mainly Clear",
            surfacePressureHpa = 1014.5,
            uvIndex = 5.2,
            soilMoisturePct = 34.0,
            soilTempCelsius = 19.5,
            dailyEtcMm = 3.6,
            deltaTCelsius = deltaT,
            chillingHoursAccumulated = 680,
            frostWarning = false,
            isLiveApi = false,
            sourceLabel = "Local Soil-Hydrology Baseline (Offline Fallback)",
            lastUpdatedIso = nowFormatted,
            hourlyForecast = fallbackHourly
        )
    }

    private fun round1(v: Double): Double = (v * 10.0).roundToInt() / 10.0
}
