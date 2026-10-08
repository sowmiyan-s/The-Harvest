package com.example.agent

data class AgentDefinition(
    val id: String,
    val name: String,
    val roleTitle: String,
    val description: String,
    val primaryResponsibility: String,
    val inputSchemaName: String,
    val outputSchemaName: String,
    val targetLatencyMs: Int,
    val defaultModel: String,
    val deterministicFallback: String,
    val isSafetyCritical: Boolean = false
)

object AgentRegistry {
    val ALL_AGENTS = listOf(
        AgentDefinition(
            id = "agent_orchestrator",
            name = "Orchestrator Agent",
            roleTitle = "Multi-Agent Router & Synthesizer",
            description = "Reads the farm profile and user prompt, routes sub-tasks to specialist agents, merges results, and ensures persona and language consistency.",
            primaryResponsibility = "Intent parsing, tool routing, multi-agent dispatch, final markdown synthesis.",
            inputSchemaName = "OrchestratorRequest",
            outputSchemaName = "OrchestratorResponse",
            targetLatencyMs = 1200,
            defaultModel = "gemini-3.5-flash",
            deterministicFallback = "Fallback to direct agronomy knowledge lookup rules without multi-turn agent expansion."
        ),
        AgentDefinition(
            id = "agent_persona_ui",
            name = "Persona & UI Agent",
            roleTitle = "Dynamic Dashboard Spec Compiler",
            description = "Analyzes farm scale, crop lifecycle, and farming methodology to compile a server-driven JSON dashboard specification. The mobile UI renders strictly from this spec.",
            primaryResponsibility = "Generates theme tokens, widget layout hierarchy, copy tone, and primary action bar.",
            inputSchemaName = "PersonaAgentRequest",
            outputSchemaName = "DashboardSpec (JSON Schema Draft-07)",
            targetLatencyMs = 600,
            defaultModel = "gemini-3.5-flash",
            deterministicFallback = "Static rule-matrix mapping: scale x crop_lifecycle x is_organic -> hardcoded theme/widget array."
        ),
        AgentDefinition(
            id = "agent_weather_soil",
            name = "Weather & Soil Agent",
            roleTitle = "Atmospheric & Hydrological Telemetry",
            description = "Connects to live Open-Meteo API for hourly forecast, evapotranspiration, volumetric soil moisture, and cumulative chilling hours calculation.",
            primaryResponsibility = "Fetches meteorological telemetry, aggregates chilling threshold (<7.2°C), evaluates frost risks, caches by 1km grid.",
            inputSchemaName = "WeatherSoilRequest",
            outputSchemaName = "WeatherSoilResponse",
            targetLatencyMs = 450,
            defaultModel = "Pure API / Deterministic Python Engine",
            deterministicFallback = "Cached weather snapshot from Redis or local climatological average."
        ),
        AgentDefinition(
            id = "agent_crop_knowledge",
            name = "Crop Knowledge Agent",
            roleTitle = "Agronomic RAG & Extension Specialist",
            description = "RAG over peer-reviewed agricultural extension databases. Never invents advice; always cites the exact source or returns 'unknown'.",
            primaryResponsibility = "Growth stage tracking, pH tolerances, crop water requirements (ETc), and companion planting.",
            inputSchemaName = "CropKnowledgeRequest",
            outputSchemaName = "CropKnowledgeResponse",
            targetLatencyMs = 900,
            defaultModel = "gemini-3.5-flash (with Search Grounding)",
            deterministicFallback = "Static FAO Crop Profile Table with verified agronomic parameters."
        ),
        AgentDefinition(
            id = "agent_inputs_cost",
            name = "Inputs & Cost Agent",
            roleTitle = "Nutrient Calculator & Farm Ledger",
            description = "Bulk formulas for irrigation volume, organic compost/manure, and N-P-K nutrient balancing driven by acreage and soil tests. Every assumption is explicitly labeled.",
            primaryResponsibility = "Mathematical formulations for acreage-scaled inputs, cost ranges (min/max), and CapEx/OpEx classification.",
            inputSchemaName = "InputsCostRequest",
            outputSchemaName = "InputsCostResponse",
            targetLatencyMs = 300,
            defaultModel = "Deterministic Mathematical Model",
            deterministicFallback = "Standard University Extension bulk formula with conservative baseline coefficients."
        ),
        AgentDefinition(
            id = "agent_vision_satellite",
            name = "Satellite & Vision Agent",
            roleTitle = "Remote Sensing & Plant Doctor",
            description = "Processes Sentinel-2 multispectral imagery for field NDVI vigor and runs computer vision diagnosis on user plant photos with confidence scores.",
            primaryResponsibility = "NDVI polygon zonal stats, foliar symptom classification, expert consult fallback threshold.",
            inputSchemaName = "VisionDiagnosisRequest / SatelliteNDVIRequest",
            outputSchemaName = "VisionDiagnosisResponse / SatelliteNDVIResponse",
            targetLatencyMs = 1500,
            defaultModel = "gemini-3.1-pro-preview / Sentinel Hub API",
            deterministicFallback = "Heuristic visual guideline prompt + 'Consult local ag extension agent' recommendation."
        ),
        AgentDefinition(
            id = "agent_guardrail",
            name = "Safety & Guardrail Agent",
            roleTitle = "Organic Integrity & Chemical Compliance",
            description = "Audits every agent recommendation before it reaches the user. Enforces strict organic-only prohibitions and verifies legal chemical dosages.",
            primaryResponsibility = "Blocks prohibited synthetic fertilizers/pesticides on organic farms, warns of toxicity, enforces safety PPE.",
            inputSchemaName = "GuardrailCheckRequest",
            outputSchemaName = "GuardrailCheckResponse",
            targetLatencyMs = 250,
            defaultModel = "Deterministic Rule Filter + LLM Safety Classifier",
            deterministicFallback = "Hard block on prohibited substance dictionary (e.g. glyphosate, chlorpyrifos on organic).",
            isSafetyCritical = true
        ),
        AgentDefinition(
            id = "agent_memory",
            name = "Memory Agent",
            roleTitle = "Farm History & Privacy Controller",
            description = "Stores farm profile evolution, seasonal logs, and user preferences. Enforces user data sovereignty with instant export and deletion.",
            primaryResponsibility = "Long-term episodic farm context, observation time-series queries, GDPR/privacy data purge.",
            inputSchemaName = "MemoryStoreRequest",
            outputSchemaName = "MemoryExportResponse",
            targetLatencyMs = 200,
            defaultModel = "PostgreSQL + Redis Store",
            deterministicFallback = "Local SQLite Room cache on mobile device."
        )
    )
}
