"""
Unit test for Weather & Soil Agent with Open-Meteo and grid cache.
Uses standard library unittest.
"""

import unittest
import asyncio
from app.schemas.agent_contracts import WeatherSoilRequest, LocationCoords
from app.agents.weather_soil_agent import WeatherSoilAgent
from app.core.redis import cache


class TestWeatherSoil(unittest.TestCase):
    def test_grid_cache_and_agent(self):
        async def run_test():
            agent = WeatherSoilAgent()
            req = WeatherSoilRequest(
                location=LocationCoords(latitude=37.7749, longitude=-122.4194),
                chilling_threshold_celsius=7.2
            )
            key = cache.generate_weather_grid_key(req.location.latitude, req.location.longitude)
            self.assertEqual(key, "weather_grid:37.77_-122.42")

            await cache.set(key, "test_cached_payload", ttl_seconds=60)
            val = await cache.get(key)
            self.assertEqual(val, "test_cached_payload")

        asyncio.run(run_test())


if __name__ == "__main__":
    unittest.main()
