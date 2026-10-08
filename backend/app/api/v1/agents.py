"""
Agents API router: Exposes endpoints for all 8 specialized agronomy agents of The Harvest.
Strict type validation, deterministic fallbacks, and zero-hallucination assumptions.
"""

from fastapi import APIRouter
from ...schemas.agent_contracts import (
    WeatherSoilRequest,
    WeatherSoilResponse,
    PersonaAgentRequest,
    PersonaAgentResponse,
    CropKnowledgeRequest,
    CropKnowledgeResponse,
    InputsCostRequest,
    InputsCostResponse,
    VisionDiagnosisRequest,
    VisionDiagnosisResponse,
    SatelliteNDVIRequest,
    SatelliteNDVIResponse,
    GuardrailCheckRequest,
    GuardrailCheckResponse,
    MemoryStoreRequest,
    MemoryExportResponse,
    OrchestratorRequest,
    OrchestratorResponse,
)
from ...agents.weather_soil_agent import WeatherSoilAgent
from ...agents.persona_ui_agent import PersonaUIAgent
from ...agents.crop_knowledge_agent import CropKnowledgeAgent
from ...agents.inputs_cost_agent import InputsCostAgent
from ...agents.satellite_vision_agent import SatelliteVisionAgent
from ...agents.guardrail_agent import GuardrailAgent
from ...agents.memory_agent import MemoryAgent
from ...agents.orchestrator_agent import OrchestratorAgent

router = APIRouter(prefix="/agents", tags=["Agronomy Agents"])

_weather_agent = WeatherSoilAgent()
_persona_agent = PersonaUIAgent()
_crop_agent = CropKnowledgeAgent()
_inputs_agent = InputsCostAgent()
_vision_agent = SatelliteVisionAgent()
_guardrail_agent = GuardrailAgent()
_memory_agent = MemoryAgent()
_orchestrator = OrchestratorAgent()


@router.post("/orchestrator", response_model=OrchestratorResponse)
async def run_orchestrator(req: OrchestratorRequest):
    """Agent 1: End-to-end multi-agent orchestration, routing, synthesis, and audit."""
    return await _orchestrator.run(req)


@router.post("/persona-ui", response_model=PersonaAgentResponse)
async def compile_dashboard_spec(req: PersonaAgentRequest):
    """Agent 2: Compiles server-driven JSON DashboardSpec based on farm context."""
    return await _persona_agent.compile_dashboard_spec(req)


@router.post("/weather-soil", response_model=WeatherSoilResponse)
async def query_weather_and_soil(req: WeatherSoilRequest):
    """Agent 3: Real-time meteorological and soil telemetry from Open-Meteo."""
    return await _weather_agent.get_weather_and_soil(req)


@router.post("/crop-knowledge", response_model=CropKnowledgeResponse)
async def query_crop_knowledge(req: CropKnowledgeRequest):
    """Agent 4: Agronomy RAG query backed by verified university extension citations."""
    return await _crop_agent.query_agronomy(req)


@router.post("/inputs-cost", response_model=InputsCostResponse)
async def calculate_inputs_and_cost(req: InputsCostRequest):
    """Agent 5: Acreage-driven calculators (water, compost, N-P-K) with labeled assumptions."""
    return await _inputs_agent.calculate(req)


@router.post("/vision-diagnosis", response_model=VisionDiagnosisResponse)
async def diagnose_plant_photo(req: VisionDiagnosisRequest):
    """Agent 6a: Plant leaf pathology diagnosis with confidence score & expert fallback."""
    return await _vision_agent.diagnose_plant_photo(req)


@router.post("/satellite-ndvi", response_model=SatelliteNDVIResponse)
async def compute_satellite_ndvi(req: SatelliteNDVIRequest):
    """Agent 6b: Sentinel-2 L2A multispectral canopy NDVI for field polygons."""
    return await _vision_agent.compute_polygon_ndvi(req)


@router.post("/guardrail-check", response_model=GuardrailCheckResponse)
async def check_guardrails(req: GuardrailCheckRequest):
    """Agent 7: Reviews advice for organic rules and chemical safety compliance."""
    return await _guardrail_agent.audit_advice(req)


@router.post("/memory/store")
async def store_user_memory(req: MemoryStoreRequest):
    """Agent 8a: Stores user preference or farm observation in scoped memory."""
    return await _memory_agent.store_memory(req)


@router.get("/memory/export/{user_id}", response_model=MemoryExportResponse)
async def export_user_memory(user_id: str):
    """Agent 8b: GDPR Article 20 data portability export for user."""
    return await _memory_agent.export_user_data(user_id)


@router.delete("/memory/{user_id}")
async def delete_user_memory(user_id: str):
    """Agent 8c: GDPR Article 17 right-to-erasure irrevocable data purge."""
    return await _memory_agent.delete_user_data(user_id)
