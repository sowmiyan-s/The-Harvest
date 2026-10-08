/**
 * The Harvest - Canonical TypeScript Strict Mode Contracts
 * Defines multi-agent I/O payloads, dashboard specification, and data models.
 */

export type FarmScale =
  | 'micro_pot'           // Window pot, balcony herbs, urban terrace
  | 'small_1acre'         // 1-acre market garden, permaculture
  | 'medium_10acre'       // 10-acre orchard, vineyard, diversified crop
  | 'commercial_500acre'; // 500-acre industrial row crops / grain

export type CropLifecycle = 'annual' | 'perennial' | 'biennial';

export type LanguageCode = 'en' | 'es' | 'hi' | 'fr' | 'sw';

export type CopyTone =
  | 'playful_educational'
  | 'practical_artisan'
  | 'commercial_agronomic';

export interface LocationCoordinates {
  latitude: number;
  longitude: number;
  elevationMeters?: number;
}

export interface FarmProfile {
  id: string;
  userId: string;
  name: string;
  scale: FarmScale;
  cropCommonName: string;
  cropScientificName?: string;
  cropLifecycle: CropLifecycle;
  isOrganic: boolean;
  areaSqMeters: number;
  location: LocationCoordinates;
  timezone: string;
  language: LanguageCode;
  soilType?: string;
  sowingDate?: string;
}

// -----------------------------------------------------------------------------
// Dynamic Dashboard Spec Types
// -----------------------------------------------------------------------------

export type WidgetType =
  | 'watering_cup_log'
  | 'kitchen_compost_helper'
  | 'milestone_badges'
  | 'weather_soil_card'
  | 'bulk_input_calculator'
  | 'satellite_ndvi_map'
  | 'capex_opex_ledger'
  | 'winter_chilling_gauge'
  | 'multi_year_roi_tracker'
  | 'annual_growth_timeline'
  | 'copilot_quick_actions';

export interface ThemeSpec {
  id: string;
  primaryColor: string;
  secondaryColor: string;
  backgroundColor: string;
  surfaceColor: string;
  accentColor: string;
  darkMode: boolean;
  density: 'compact' | 'comfortable' | 'data_dense';
}

export interface WidgetSpecItem {
  id: string;
  widgetType: WidgetType;
  title: string;
  priorityOrder: number;
  gridSpan: 1 | 2 | 3 | 4;
  params: Record<string, unknown>;
}

export interface QuickActionItem {
  id: string;
  label: string;
  iconName: string;
  actionType: 'navigate_camera' | 'open_calculator' | 'log_watering' | 'ask_copilot';
  payload: Record<string, unknown>;
}

export interface DashboardSpec {
  schemaVersion: '1.0.0';
  scale: FarmScale;
  crop: string;
  lifecycle: CropLifecycle;
  isOrganic: boolean;
  language: LanguageCode;
  copyTone: CopyTone;
  theme: ThemeSpec;
  quickActions: QuickActionItem[];
  widgets: WidgetSpecItem[];
  metadata?: Record<string, unknown>;
}

// -----------------------------------------------------------------------------
// 1. Orchestrator Agent Contracts
// -----------------------------------------------------------------------------

export interface OrchestratorRequest {
  traceId: string;
  farmContext: FarmProfile;
  userQuery: string;
  imageBase64?: string;
  sessionHistoryTurns?: number;
}

export interface AgentExecutionAudit {
  agentName: string;
  latencyMs: number;
  tokensUsed: number;
  costUsd: number;
  fallbackUsed: boolean;
  sourceCitation?: string;
}

export interface OrchestratorResponse {
  traceId: string;
  answerMarkdown: string;
  targetLanguage: LanguageCode;
  personaTone: CopyTone;
  actionableSteps: string[];
  guardrailCleared: boolean;
  activeAssumptions: string[];
  agentAudits: AgentExecutionAudit[];
}

// -----------------------------------------------------------------------------
// 2. Weather & Soil Agent Contracts
// -----------------------------------------------------------------------------

export interface WeatherSoilRequest {
  location: LocationCoordinates;
  includeHourly?: boolean;
  // ASSUMPTION: 7.2°C (45°F) is the standardized Weinberger base temperature for fruit tree chilling accumulation
  chillingThresholdCelsius?: number;
}

export interface HourlyForecastItem {
  timeIso: string;
  tempCelsius: number;
  humidityPct: number;
  precipitationMm: number;
  soilMoistureVolumetricPct: number;
  evapotranspirationMm: number;
}

export interface WeatherSoilResponse {
  source: 'open-meteo';
  cacheHit: boolean;
  gridCell: string;
  currentTempCelsius: number;
  currentHumidityPct: number;
  soilMoisturePct: number;
  soilTemperatureCelsius: number;
  evapotranspirationDailyMm: number;
  accumulatedChillingHours: number;
  frostWarning: boolean;
  forecast7d: HourlyForecastItem[];
  recordedAt: string;
}

// -----------------------------------------------------------------------------
// 3. Crop Knowledge Agent (RAG) Contracts
// -----------------------------------------------------------------------------

export interface CropKnowledgeRequest {
  cropName: string;
  growthStage?: string;
  questionTopic: 'irrigation' | 'nutrients' | 'pest_disease' | 'pruning' | 'harvest';
  isOrganic: boolean;
}

export interface AgronomyCitation {
  sourceName: string;
  publicationYear: number;
  doiOrUrl?: string;
  confidenceLevel: number;
}

export interface CropKnowledgeResponse {
  cropName: string;
  lifecycle: CropLifecycle;
  status: 'found' | 'unknown';
  factualGuidance: string;
  recommendedPhRange: [number, number];
  waterRequirementsMmWeek: number;
  chillingHoursNeeded?: number;
  citations: AgronomyCitation[];
  isVerified: boolean;
}

// -----------------------------------------------------------------------------
// 4. Inputs & Cost Agent Contracts
// -----------------------------------------------------------------------------

export type InputCalcType =
  | 'water_volume'
  | 'compost_manure'
  | 'organic_fertilizer'
  | 'conventional_npk';

export interface InputsCostRequest {
  calcType: InputCalcType;
  areaAcres: number;
  cropName: string;
  isOrganic: boolean;
  soilNitrogenPpm?: number;
  soilPhosphorusPpm?: number;
  soilPotassiumPpm?: number;
}

export interface CalculationAssumptions {
  formulaName: string;
  baseRequirementPerAcre: string;
  soilCreditSubtraction: string;
  efficiencyFactor: number;
  // ASSUMPTION: Standard university agricultural extension guidelines applied
  labeledDisclaimer: string;
}

export interface InputsCostResponse {
  recommendedQuantity: number;
  unit: 'liters' | 'kg' | 'metric_tons' | 'gallons' | 'cups';
  estimatedCostUsdMin: number;
  estimatedCostUsdMax: number;
  assumptions: CalculationAssumptions;
  applicationSchedule: string[];
}

// -----------------------------------------------------------------------------
// 5. Satellite & Vision Agent Contracts
// -----------------------------------------------------------------------------

export interface VisionDiagnosisRequest {
  imageBase64: string;
  cropSuspected?: string;
  location?: LocationCoordinates;
}

export interface VisionDiagnosisResponse {
  cropIdentified: string;
  condition: string;
  confidenceScore: number;
  isExpertConsultRecommended: boolean;
  symptomsObserved: string[];
  organicRemedy?: string;
  chemicalRemedy?: string;
  safetyAdvisory: string;
}

export interface SatelliteNDVIRequest {
  fieldPolygonGeoJson: Record<string, unknown>;
  targetDate?: string;
}

export interface SatelliteNDVIResponse {
  satelliteSource: 'Sentinel-2 L2A';
  acquisitionDate: string;
  meanNdvi: number;
  ndviMin: number;
  ndviMax: number;
  vigorClassification: 'vigorous' | 'moderate' | 'stressed' | 'bare_soil';
  cloudCoverPct: number;
}

// -----------------------------------------------------------------------------
// 6. Safety & Guardrail Agent Contracts
// -----------------------------------------------------------------------------

export interface GuardrailCheckRequest {
  userQuery: string;
  candidateAdvice: string;
  isOrganicFarm: boolean;
  scale: FarmScale;
  jurisdictionCountry: string;
}

export interface GuardrailCheckResponse {
  isSafe: boolean;
  organicConstraintViolated: boolean;
  blockedSubstancesDetected: string[];
  sanitizedAdvice: string;
  guardrailReason?: string;
}

// -----------------------------------------------------------------------------
// 7. Memory Agent Contracts
// -----------------------------------------------------------------------------

export interface MemoryStoreRequest {
  userId: string;
  farmId: string;
  key: string;
  valueJson: Record<string, unknown>;
  expiresAt?: string;
}

export interface MemoryExportResponse {
  userId: string;
  farmProfiles: FarmProfile[];
  observationRecords: number;
  logEntriesCount: number;
  exportGeneratedAt: string;
}
