"""
Core configuration settings for The Harvest backend.
Reads from environment variables with sensible production defaults.
"""

from typing import List
from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import Field


class Settings(BaseSettings):
    PROJECT_NAME: str = "The Harvest - Farmers AI Assistant"
    API_V1_STR: str = "/api/v1"
    SECRET_KEY: str = Field("production-secret-change-me-harvest-2026", description="JWT HMAC secret key")
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7  # 7 days

    # Database: PostgreSQL with PostGIS
    POSTGRES_SERVER: str = Field("localhost", alias="POSTGRES_HOST")
    POSTGRES_PORT: int = 5432
    POSTGRES_USER: str = "postgres"
    POSTGRES_PASSWORD: str = "postgres"
    POSTGRES_DB: str = "the_harvest"

    @property
    def SQLALCHEMY_DATABASE_URI(self) -> str:
        return f"postgresql+asyncpg://{self.POSTGRES_USER}:{self.POSTGRES_PASSWORD}@{self.POSTGRES_SERVER}:{self.POSTGRES_PORT}/{self.POSTGRES_DB}"

    # Redis Cache & Queue
    REDIS_HOST: str = "localhost"
    REDIS_PORT: int = 6379
    # ASSUMPTION: 1 hour (3600 seconds) TTL is optimal for weather grid caches
    WEATHER_CACHE_TTL_SECONDS: int = 3600

    # Open-Meteo Meteorological API
    OPEN_METEO_BASE_URL: str = "https://api.open-meteo.com/v1/forecast"

    # CORS configuration
    CORS_ORIGINS: List[str] = ["*"]

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")


settings = Settings()
