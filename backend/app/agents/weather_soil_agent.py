"""
Weather & Soil Agent: Real-time telemetry via Open-Meteo API.
Computes soil moisture infiltration, daily ETc evapotranspiration,
and winter chilling accumulation with rounded grid cell caching.
"""

from datetime import datetime, timezone
from typing import List, Optional, Dict, Any
import httpx
import logging
from ..schemas.agent_contracts import (
    WeatherSoilRequest,
    WeatherSoilResponse,
    HourlyForecastItem,
)
from ..core.redis import cache
from ..core.config import settings

logger = logging.getLogger("the_harvest.weather_soil")


class WeatherSoilAgent:
    def __init__(self, http_client: Optional[httpx.AsyncClient] = None):
        self.client = http_client or httpx.AsyncClient(timeout=10.0)

    async def get_weather_and_soil(self, req: WeatherSoilRequest) -> WeatherSoilResponse:
        grid_key = cache.generate_weather_grid_key(req.location.latitude, req.location.longitude)

        # 1. Check Redis grid cache
        cached_json = await cache.get(grid_key)
        if cached_json:
            import json
            data = json.loads(cached_json)
            data["cache_hit"] = True
            return WeatherSoilResponse(**data)

        # 2. Query Live Open-Meteo Meteorological & Soil API
        params = {
            "latitude": round(req.location.latitude, 4),
            "longitude": round(req.location.longitude, 4),
            "hourly": [
                "temperature_2m",
                "relative_humidity_2m",
                "precipitation",
                "soil_temperature_0cm",
                "soil_moisture_0_to_1cm",
                "et0_fao_evapotranspiration",
            ],
            "forecast_days": 7,
            "timezone": "UTC",
        }

        try:
            resp = await self.client.get(settings.OPEN_METEO_BASE_URL, params=params)
            resp.raise_for_status()
            raw = resp.json()

            hourly = raw.get("hourly", {})
            times = hourly.get("time", [])
            temps = hourly.get("temperature_2m", [])
            humidities = hourly.get("relative_humidity_2m", [])
            precips = hourly.get("precipitation", [])
            soil_temps = hourly.get("soil_temperature_0cm", [])
            soil_moistures = hourly.get("soil_moisture_0_to_1cm", [])
            ets = hourly.get("et0_fao_evapotranspiration", [])

            # Compute current conditions (index 0 / first available)
            curr_temp = float(temps[0]) if temps else 20.0
            curr_humidity = float(humidities[0]) if humidities else 60.0
            # Open-Meteo returns m3/m3 (e.g. 0.342 = 34.2%)
            raw_sm = float(soil_moistures[0]) if soil_moistures else 0.30
            curr_soil_moisture_pct = round(raw_sm * 100.0 if raw_sm <= 1.0 else raw_sm, 1)
            curr_soil_temp = float(soil_temps[0]) if soil_temps else 18.0

            # Compute daily ETc sum for first 24 hours
            daily_et_sum = sum(float(e) for e in ets[:24]) if ets else 3.5

            # Compute accumulated chilling hours below threshold
            # ASSUMPTION: Weinberger model counts hours where hourly temp is strictly below threshold (7.2°C)
            threshold = req.chilling_threshold_celsius or 7.2
            chilling_hours = sum(1 for t in temps if float(t) < threshold)

            # Frost warning detection (any forecast temp <= 0.0°C)
            frost_detected = any(float(t) <= 0.0 for t in temps)

            # Parse 7-day hourly items (subsample every 3 hours for payload efficiency)
            forecast_items: List[HourlyForecastItem] = []
            for i in range(0, min(len(times), 168), 3):
                forecast_items.append(
                    HourlyForecastItem(
                        time_iso=times[i],
                        temp_celsius=float(temps[i]),
                        humidity_pct=float(humidities[i]),
                        precipitation_mm=float(precips[i]),
                        soil_moisture_volumetric_pct=round(float(soil_moistures[i]) * 100.0 if float(soil_moistures[i]) <= 1.0 else float(soil_moistures[i]), 1),
                        evapotranspiration_mm=float(ets[i]) if i < len(ets) else 0.0,
                    )
                )

            result = WeatherSoilResponse(
                source="open-meteo",
                cache_hit=False,
                grid_cell=grid_key,
                current_temp_celsius=curr_temp,
                current_humidity_pct=curr_humidity,
                soil_moisture_pct=curr_soil_moisture_pct,
                soil_temperature_celsius=curr_soil_temp,
                evapotranspiration_daily_mm=round(daily_et_sum, 2),
                accumulated_chilling_hours=chilling_hours,
                frost_warning=frost_detected,
                forecast_7d=forecast_items,
                recorded_at=datetime.now(timezone.utc),
            )

            # Save in Redis cache
            import json
            await cache.set(grid_key, result.model_dump_json(), ttl_seconds=settings.WEATHER_CACHE_TTL_SECONDS)
            return result

        except Exception as e:
            logger.warning(f"Open-Meteo API failed or offline: {e}. Engaging deterministic fallback.")
            # Deterministic Fallback Mode
            return WeatherSoilResponse(
                source="open-meteo-offline-fallback",
                cache_hit=False,
                grid_cell=grid_key,
                current_temp_celsius=20.5,
                current_humidity_pct=55.0,
                soil_moisture_pct=32.0,
                soil_temperature_celsius=19.0,
                evapotranspiration_daily_mm=3.2,
                accumulated_chilling_hours=450,
                frost_warning=False,
                forecast_7d=[],
                recorded_at=datetime.now(timezone.utc),
            )
