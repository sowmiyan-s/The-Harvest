# The Harvest - Backend Multi-Agent Service

## Overview
This service powers the multi-agent agronomy brain for **The Harvest (Farmers AI Assistant)**.
It orchestrates 8 specialized agents behind a unified gateway, providing typed JSON responses,
deterministic fallback modes, caching, and dynamic dashboard specifications for mobile clients.

## Architecture
- **Framework**: FastAPI (Python 3.11+) with asynchronous execution
- **Agents**: Orchestrator, Persona/UI, Weather & Soil, Crop Knowledge (RAG), Inputs & Cost, Satellite/Vision, Safety/Guardrail, Memory
- **Database**: PostgreSQL 16 + PostGIS for spatial polygon field boundaries
- **Cache & Queues**: Redis (rounded geohash grid caching, Celery/Arq job queues)
- **Validation**: Strict Pydantic v2 schemas and JSON Schema Draft-07

## Implemented Agents & Endpoints
- **1. Orchestrator Agent** (`POST /api/v1/agents/orchestrator`): Central router coordinating specialist agents, synthesizing responses in persona tone, logging tool latency and token cost audits.
- **2. Persona & UI Agent** (`POST /api/v1/agents/persona-ui`): Compiles server-driven JSON DashboardSpec based on farm scale, crop lifecycle, and organic constraint.
- **3. Weather & Soil Agent** (`POST /api/v1/agents/weather-soil`): Live Open-Meteo telemetry computing soil moisture, daily ETc, chilling accumulation (<7.2°C), and 1.1km grid caching.
- **4. Crop Knowledge Agent** (`POST /api/v1/agents/crop-knowledge`): RAG over agronomy extension publications (FAO, UC Davis, WSU, K-State). Never guesses; cites scientific sources or returns 'unknown'.
- **5. Safety & Guardrail Agent** (`POST /api/v1/agents/guardrail-check`): Zero-trust chemical compliance filter. Strictly blocks prohibited synthetic chemicals (glyphosate, chlorpyrifos, synthetic nitrogen) on organic farms (USDA NOP §205.105), provides OMRI-listed alternatives, and enforces PPE advisories.

## Core Infrastructure
- `app/core/config.py`: Environment configuration and TTL settings
- `app/core/security.py`: JWT token generation, verification, and password hashing
- `app/core/redis.py`: Rounded grid cell caching (`weather_grid:{lat_2dp}_{lon_2dp}`)
- `app/api/v1/auth.py`: `/register`, `/login`, and `/me` JWT endpoints
- `app/main.py`: FastAPI application entrypoint with CORS and OpenAPI docs
