"""
Pydantic contracts for the 8 specialized agents of The Harvest.
Strict type hints, validation, latency, and assumption tracking.
"""

from enum import Enum
from typing import List, Dict, Optional, Any, Union
from pydantic import BaseModel, Field, field_validator
from datetime import datetime


# -----------------------------------------------------------------------------
# Common Core Enums & Schemas
# -----------------------------------------------------------------------------

class FarmScale(str, Enum):
    MICRO_POT = "micro_pot"           # Window pot, balcony herbs, urban terrace
    SMALL_1ACRE = "small_1acre"       # 1-acre market garden, permaculture
    MEDIUM_10ACRE = "medium_10acre"   # 10-acre orchard, vineyard, diversified crop
    COMMERCIAL_500ACRE = "commercial_500acre" # 500-acre industrial row crops / grain


class CropLifecycle(str, Enum):
    ANNUAL = "annual"
    PERENNIAL = "perennial"
    BIENNIAL = "biennial"


class LanguageCode(str, Enum):
    EN = "en"
    ES = "es"
    HI = "hi"
    FR = "fr"
    SW = "sw"


class LocationCoords(BaseModel):
    latitude: float = Field(..., ge=-90.0, le=90.0, description="WGS84 latitude")
    longitude: float = Field(..., ge=-180.0, le=180.0, description="WGS84 longitude")
    elevation_meters: Optional[float] = Field(None, description="Elevation above sea level")

    # ASSUMPTION: Location rounding to 2 decimal places (~1.1km) protects privacy while enabling grid weather caching
    def rounded_cache_key(self) -> str:
        return f"{round(self.latitude, 2)}_{round(self.longitude, 2)}"


class FarmContext(BaseModel):
    farm_id: str
    user_id: str
    name: str
    scale: FarmScale
    crop_common_name: str
    crop_lifecycle: CropLifecycle
    is_organic: bool
    location: LocationCoords
    language: LanguageCode = LanguageCode.EN
    timezone: str = "UTC"
    soil_type: Optional[str] = "loam"
    sowing_date: Optional[str] = None # ISO date for annual timeline


# -----------------------------------------------------------------------------
# 1. Orchestrator Agent Contracts
# -----------------------------------------------------------------------------

class OrchestratorRequest(BaseModel):
    trace_id: str = Field(..., description="Distributed tracing UUID")
    farm_context: FarmContext
    user_query: str = Field(..., min_length=1, description="Farmer query or command")
    image_base64: Optional[str] = Field(None, description="Optional attached plant leaf/soil image")
    session_history_turns: Optional[int] = Field(3, description="Number of context turns to retain")


class AgentExecutionAudit(BaseModel):
    agent_name: str
    latency_ms: int
    tokens_used: int
    cost_usd: float
    fallback_used: bool
    source_citation: Optional[str] = None


class OrchestratorResponse(BaseModel):
    trace_id: str
    answer_markdown: str
    target_language: LanguageCode
    persona_tone: str
    actionable_steps: List[str] = Field(default_factory=list)
    guardrail_cleared: bool = True
    active_assumptions: List[str] = Field(default_factory=list)
    agent_audits: List[AgentExecutionAudit] = Field(default_factory=list)


# -----------------------------------------------------------------------------
# 2. Persona / UI Agent Contracts (Dashboard Spec Engine)
# -----------------------------------------------------------------------------

class WidgetType(str, Enum):
    WATERING_CUP_LOG = "watering_cup_log"
    KITCHEN_COMPOST_HELPER = "kitchen_compost_helper"
    MILESTONE_BADGES = "milestone_badges"
    WEATHER_SOIL_CARD = "weather_soil_card"
    BULK_INPUT_CALCULATOR = "bulk_input_calculator"
    SATELLITE_NDVI_MAP = "satellite_ndvi_map"
    CAPEX_OPEX_LEDGER = "capex_opex_ledger"
    WINTER_CHILLING_GAUGE = "winter_chilling_gauge"
    MULTI_YEAR_ROI_TRACKER = "multi_year_roi_tracker"
    ANNUAL_GROWTH_TIMELINE = "annual_growth_timeline"
    COPILOT_QUICK_ACTIONS = "copilot_quick_actions"


class ThemeSpec(BaseModel):
    id: str # 'pastel_nature', 'earth_organic', 'orchard_gold', 'industrial_dark'
    primary_color: str
    secondary_color: str
    background_color: str
    surface_color: str
    accent_color: str
    dark_mode: bool
    density: str = Field("comfortable", description="'compact', 'comfortable', 'data_dense'")


class WidgetSpecItem(BaseModel):
    id: str
    widget_type: WidgetType
    title: str
    priority_order: int
    grid_span: int = Field(1, ge=1, le=4, description="1 to 4 columns grid span")
    params: Dict[str, Any] = Field(default_factory=dict)


class DashboardSpec(BaseModel):
    schema_version: str = "1.0.0"
    scale: FarmScale
    crop: str
    is_organic: bool
    theme: ThemeSpec
    copy_tone: str # 'playful_educational', 'practical_artisan', 'commercial_agronomic'
    widgets: List[WidgetSpecItem]
    generated_at: datetime = Field(default_factory=datetime.utcnow)


class PersonaAgentRequest(BaseModel):
    farm_context: FarmContext


class PersonaAgentResponse(BaseModel):
    spec: DashboardSpec
    reasoning_summary: str


# -----------------------------------------------------------------------------
# 3. Weather & Soil Agent Contracts (Live Open-Meteo Integration)
# -----------------------------------------------------------------------------

class WeatherSoilRequest(BaseModel):
    location: LocationCoords
    include_hourly: bool = True
    chilling_threshold_celsius: float = Field(7.2, description="Chilling threshold: 7.2°C / 45°F")


class HourlyForecastItem(BaseModel):
    time_iso: str
    temp_celsius: float
    humidity_pct: float
    precipitation_mm: float
    soil_moisture_volumetric_pct: float
    evapotranspiration_mm: float


class WeatherSoilResponse(BaseModel):
    source: str = "open-meteo"
    cache_hit: bool = False
    grid_cell: str
    current_temp_celsius: float
    current_humidity_pct: float
    soil_moisture_pct: float
    soil_temperature_celsius: float
    evapotranspiration_daily_mm: float
    accumulated_chilling_hours: int
    frost_warning: bool
    forecast_7d: List[HourlyForecastItem] = Field(default_factory=list)
    recorded_at: datetime = Field(default_factory=datetime.utcnow)


# -----------------------------------------------------------------------------
# 4. Crop Knowledge Agent (RAG Agronomy Engine)
# -----------------------------------------------------------------------------

class CropKnowledgeRequest(BaseModel):
    crop_name: str
    growth_stage: Optional[str] = None
    question_topic: str # 'irrigation', 'nutrients', 'pest_disease', 'pruning', 'harvest'
    is_organic: bool = True


class AgronomySourceCitation(BaseModel):
    source_name: str
    publication_year: int
    doi_or_url: Optional[str] = None
    confidence_level: float = Field(..., ge=0.0, le=1.0)


class CropKnowledgeResponse(BaseModel):
    crop_name: str
    lifecycle: CropLifecycle
    status: str # 'found', 'unknown'
    factual_guidance: str
    recommended_ph_range: List[float]
    water_requirements_mm_week: float
    chilling_hours_needed: Optional[int] = None
    citations: List[AgronomySourceCitation]
    # Never invent data: return 'unknown' if not backed by citation
    is_verified: bool


# -----------------------------------------------------------------------------
# 5. Inputs & Cost Agent Contracts (Formulas & Ledger)
# -----------------------------------------------------------------------------

class InputCalcType(str, Enum):
    WATER_VOLUME = "water_volume"
    COMPOST_MANURE = "compost_manure"
    ORGANIC_FERTILIZER = "organic_fertilizer"
    CONVENTIONAL_NPK = "conventional_npk"


class InputsCostRequest(BaseModel):
    calc_type: InputCalcType
    area_acres: float = Field(..., gt=0.0)
    crop_name: str
    is_organic: bool = True
    soil_nitrogen_ppm: Optional[float] = None
    soil_phosphorus_ppm: Optional[float] = None
    soil_potassium_ppm: Optional[float] = None


class CalculationAssumptions(BaseModel):
    formula_name: str
    base_requirement_per_acre: str
    soil_credit_subtraction: str
    efficiency_factor: float
    labeled_disclaimer: str = "ASSUMPTION: Estimated based on standard agronomic extension models."


class InputsCostResponse(BaseModel):
    recommended_quantity: float
    unit: str # 'liters', 'kg', 'tons', 'gallons'
    estimated_cost_usd_min: float
    estimated_cost_usd_max: float
    assumptions: CalculationAssumptions
    application_schedule: List[str]


# -----------------------------------------------------------------------------
# 6. Satellite & Vision Agent Contracts (NDVI + Plant Photo Doctor)
# -----------------------------------------------------------------------------

class VisionDiagnosisRequest(BaseModel):
    image_base64: str
    crop_suspected: Optional[str] = None
    location: Optional[LocationCoords] = None


class SatelliteNDVIRequest(BaseModel):
    field_polygon_geojson: Dict[str, Any]
    target_date: Optional[str] = None


class VisionDiagnosisResponse(BaseModel):
    crop_identified: str
    condition: str # 'healthy', 'leaf_blight', 'nitrogen_deficiency', etc.
    confidence_score: float = Field(..., ge=0.0, le=1.0)
    is_expert_consult_recommended: bool
    symptoms_observed: List[str]
    organic_remedy: Optional[str] = None
    chemical_remedy: Optional[str] = None
    safety_advisory: str


class SatelliteNDVIResponse(BaseModel):
    satellite_source: str = "Sentinel-2 L2A"
    acquisition_date: str
    mean_ndvi: float = Field(..., ge=-1.0, le=1.0)
    ndvi_min: float
    ndvi_max: float
    vigor_classification: str # 'vigorous', 'moderate', 'stressed', 'bare_soil'
    cloud_cover_pct: float


# -----------------------------------------------------------------------------
# 7. Safety / Guardrail Agent Contracts
# -----------------------------------------------------------------------------

class GuardrailCheckRequest(BaseModel):
    user_query: str
    candidate_advice: str
    is_organic_farm: bool
    scale: FarmScale
    jurisdiction_country: str = "US"


class GuardrailCheckResponse(BaseModel):
    is_safe: bool
    organic_constraint_violated: bool
    blocked_substances_detected: List[str] = Field(default_factory=list)
    sanitized_advice: str
    guardrail_reason: Optional[str] = None


# -----------------------------------------------------------------------------
# 8. Memory Agent Contracts (Farm History & GDPR Controls)
# -----------------------------------------------------------------------------

class MemoryStoreRequest(BaseModel):
    user_id: str
    farm_id: str
    key: str
    value_json: Dict[str, Any]
    expires_at: Optional[datetime] = None


class MemoryExportResponse(BaseModel):
    user_id: str
    farm_profiles: List[Dict[str, Any]]
    observation_records: int
    log_entries_count: int
    export_generated_at: datetime = Field(default_factory=datetime.utcnow)
