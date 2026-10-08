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

## Package Contents
- `app/schemas/agent_contracts.py`: Typed Pydantic models for all 8 agents (inputs and outputs)
- `app/schemas/dashboard_spec.py`: Pydantic models for the dynamic dashboard specification
- `app/specs/dashboard_spec.schema.json`: Formal JSON Schema Draft-07 for client-server contract
- `app/models/schema.sql`: Complete PostgreSQL + PostGIS database DDL schema with RLS and spatial indexes
