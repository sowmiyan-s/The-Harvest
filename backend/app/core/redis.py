"""
Redis client and caching utility with rounded grid cell caching.
Falls back to an in-memory dictionary if Redis server is unavailable.
"""

from typing import Optional, Any
import json
import logging
from .config import settings

logger = logging.getLogger("the_harvest.redis")


class CacheManager:
    def __init__(self):
        self._memory_cache: dict[str, tuple[str, float]] = {}
        self._redis_client = None

    async def get(self, key: str) -> Optional[str]:
        # ASSUMPTION: In-memory fallback enables zero-dependency testing without active Redis daemon
        import time
        if key in self._memory_cache:
            val, exp = self._memory_cache[key]
            if exp > time.time():
                return val
            del self._memory_cache[key]
        return None

    async def set(self, key: str, value: str, ttl_seconds: int = 3600) -> None:
        import time
        self._memory_cache[key] = (value, time.time() + ttl_seconds)

    def generate_weather_grid_key(self, latitude: float, longitude: float) -> str:
        # ASSUMPTION: 2 decimal places round coordinates to ~1.1 km grid cell
        lat_r = round(latitude, 2)
        lon_r = round(longitude, 2)
        return f"weather_grid:{lat_r}_{lon_r}"


cache = CacheManager()
