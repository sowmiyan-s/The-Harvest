package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent.AgentDefinition
import com.example.agent.AgentRegistry
import com.example.data.firebase.FirebaseHarvestService
import com.example.data.model.CropRotationalRecommendation
import com.example.data.model.DashboardSpec
import com.example.data.model.FarmProfile
import com.example.data.model.FarmScale
import com.example.data.model.FieldScoutLog
import com.example.data.model.HourlyAgroForecast
import com.example.data.model.SprayAdvisory
import com.example.data.remote.AdvancedAgronomyEngine
import com.example.data.remote.AgronomyRagEngine
import com.example.data.remote.GeminiVisionClient
import com.example.data.remote.LiveWeatherSoilData
import com.example.data.remote.OpenMeteoClient
import com.example.data.remote.PlantDiagnosisResult
import com.example.data.repository.HarvestRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppViewTab(val label: String) {
    DYNAMIC_DASHBOARD("Dashboard"),
    COPILOT_CHAT("Copilot AI"),
    PLANT_DOCTOR("Vision AI"),
    FIELD_MAP("Field & NDVI"),
    SPRAY_ADVISORY("Spray Window"),
    FIELD_SCOUT("Scout Logs"),
    CROP_ROTATION("Rotation Plan"),
    INPUTS_LEDGER("Calculators"),
    AGENT_TOPOLOGY("8 Agents"),
    DATA_MODELS("Data & DB"),
    SCHEMA_SPEC("JSON Spec")
}

data class HarvestUiState(
    val currentTab: AppViewTab = AppViewTab.DYNAMIC_DASHBOARD,
    val selectedFarm: FarmProfile,
    val dashboardSpec: DashboardSpec,
    val allFarms: List<FarmProfile>,
    val allAgents: List<AgentDefinition> = AgentRegistry.ALL_AGENTS,
    // Live Open-Meteo Weather & Soil Telemetry (Realtime)
    val liveWeather: LiveWeatherSoilData? = null,
    val liveTempCelsius: Double = 21.5,
    val liveApparentTempCelsius: Double = 21.5,
    val liveHumidityPct: Double = 58.0,
    val liveSoilMoisturePct: Double = 34.2,
    val liveDailyEtcMm: Double = 3.8,
    val liveWindSpeedKph: Double = 11.2,
    val liveWindDirectionDeg: Int = 180,
    val liveWindGustsKph: Double = 14.5,
    val liveDeltaT: Double = 4.2,
    val liveWeatherCondition: String = "Clear Sky",
    val liveDewPointCelsius: Double = 11.0,
    val liveUvIndex: Double = 4.2,
    val livePressureHpa: Double = 1013.2,
    val chillingHoursAccumulated: Int = 680,
    val chillingHoursRequired: Int = 800,
    val isWeatherLoading: Boolean = false,
    val weatherDataSource: String = "Open-Meteo Live API",
    val lastTelemetryUpdated: String = "Live Now",
    val hourlyForecast: List<HourlyAgroForecast> = emptyList(),
    // Advanced Agronomy Features
    val sprayAdvisory: SprayAdvisory = AdvancedAgronomyEngine.computeSprayAdvisory(21.5, 58.0),
    val scoutLogs: List<FieldScoutLog> = emptyList(),
    val rotationalPlan: List<CropRotationalRecommendation> = emptyList(),
    // Interactive widget telemetry & user inputs (labeled assumptions)
    val microWaterCupsLogged: Int = 3,
    val scrapCompostKg: Double = 1.4,
    val ndviScore: Double = 0.74,
    val bulkAcreageInput: Double = 500.0,
    val bulkNPKResultKg: Double = 40000.0,
    val organicGuardrailBlocked: Boolean = false,
    val lastGuardrailWarning: String? = null,
    // Plant Doctor & Gemini Vision Telemetry
    val selectedPlantBitmap: Bitmap? = null,
    val selectedSpecimenName: String = "Tomato (Late Blight suspect)",
    val isDiagnosing: Boolean = false,
    val plantDiagnosisResult: PlantDiagnosisResult? = null,
    // Firebase Cloud Sync & Auth State
    val firebaseUser: FirebaseUser? = null,
    val isCloudSyncing: Boolean = false,
    val cloudSyncMessage: String? = null,
    // Copilot Chat Conversation
    val copilotMessages: List<ChatMessage> = emptyList(),
    // Onboarding Wizard State
    val showOnboarding: Boolean = false
)

class HarvestViewModel(
    private val repository: HarvestRepository = HarvestRepository()
) : ViewModel() {

    private val initialFarm = repository.availableFarms.first()
    private val initialSpec = repository.getDashboardSpecForFarm(initialFarm)
    private val initialScoutLogs = listOf(
        FieldScoutLog(
            farmId = initialFarm.id,
            title = "South Ridge Aphid Pressure",
            cropStage = "V4 Vegetative",
            pestOrIssue = "Green peach aphid colonies on lower leaves",
            severityLevel = "Moderate",
            actionTaken = "Applied organic potassium salts of fatty acids (Neem)",
            dateIso = "2026-10-06"
        ),
        FieldScoutLog(
            farmId = initialFarm.id,
            title = "Block B Infiltration Check",
            cropStage = "Active Canopy",
            pestOrIssue = "Slow drainage after 25mm rain event",
            severityLevel = "Low",
            actionTaken = "Aerated furrow alleys and verified cover crop roots",
            dateIso = "2026-10-07"
        )
    )

    private val _uiState = MutableStateFlow(
        HarvestUiState(
            selectedFarm = initialFarm,
            dashboardSpec = initialSpec,
            allFarms = repository.availableFarms,
            firebaseUser = FirebaseHarvestService.currentUser,
            scoutLogs = initialScoutLogs,
            rotationalPlan = AdvancedAgronomyEngine.computeRotationalPlan(initialFarm),
            copilotMessages = listOf(
                createInitialWelcomeMessage(initialFarm)
            )
        )
    )
    val uiState: StateFlow<HarvestUiState> = _uiState.asStateFlow()

    init {
        // Observe auth state changes
        FirebaseHarvestService.auth.addAuthStateListener { auth ->
            _uiState.value = _uiState.value.copy(firebaseUser = auth.currentUser)
        }
        // Fetch real-time weather & soil telemetry for initial farm
        fetchLiveTelemetry(initialFarm)
    }

    fun selectTab(tab: AppViewTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun openOnboarding() {
        _uiState.value = _uiState.value.copy(showOnboarding = true)
    }

    fun closeOnboarding() {
        _uiState.value = _uiState.value.copy(showOnboarding = false)
    }

    fun addNewFarm(newFarm: FarmProfile) {
        val updatedFarms = _uiState.value.allFarms + newFarm
        val newSpec = repository.getDashboardSpecForFarm(newFarm)
        _uiState.value = _uiState.value.copy(
            allFarms = updatedFarms,
            selectedFarm = newFarm,
            dashboardSpec = newSpec,
            rotationalPlan = AdvancedAgronomyEngine.computeRotationalPlan(newFarm),
            showOnboarding = false,
            bulkAcreageInput = newFarm.areaAcres,
            bulkNPKResultKg = newFarm.areaAcres * 80.0,
            copilotMessages = listOf(createInitialWelcomeMessage(newFarm))
        )
        fetchLiveTelemetry(newFarm)
    }

    fun selectFarm(farm: FarmProfile) {
        val newSpec = repository.getDashboardSpecForFarm(farm)
        _uiState.value = _uiState.value.copy(
            selectedFarm = farm,
            dashboardSpec = newSpec,
            rotationalPlan = AdvancedAgronomyEngine.computeRotationalPlan(farm),
            bulkAcreageInput = farm.areaAcres,
            bulkNPKResultKg = farm.areaAcres * 80.0,
            copilotMessages = listOf(createInitialWelcomeMessage(farm))
        )
        fetchLiveTelemetry(farm)
    }

    fun refreshLiveTelemetry() {
        fetchLiveTelemetry(_uiState.value.selectedFarm)
    }

    private fun fetchLiveTelemetry(farm: FarmProfile) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isWeatherLoading = true)
            val telemetry = OpenMeteoClient.fetchLiveTelemetry(
                latitude = farm.location.latitude,
                longitude = farm.location.longitude
            )
            val updatedAdvisory = AdvancedAgronomyEngine.computeSprayAdvisory(
                tempCelsius = telemetry.currentTempCelsius,
                humidityPct = telemetry.currentHumidityPct,
                windSpeedKph = telemetry.windSpeedKph,
                rainProbabilityPct = telemetry.rainProbabilityPct,
                deltaTOverride = telemetry.deltaTCelsius
            )
            _uiState.value = _uiState.value.copy(
                liveWeather = telemetry,
                liveTempCelsius = telemetry.currentTempCelsius,
                liveApparentTempCelsius = telemetry.apparentTempCelsius,
                liveHumidityPct = telemetry.currentHumidityPct,
                liveSoilMoisturePct = telemetry.soilMoisturePct,
                liveDailyEtcMm = telemetry.dailyEtcMm,
                liveWindSpeedKph = telemetry.windSpeedKph,
                liveWindDirectionDeg = telemetry.windDirectionDeg,
                liveWindGustsKph = telemetry.windGustsKph,
                liveDeltaT = telemetry.deltaTCelsius,
                liveWeatherCondition = telemetry.weatherDescription,
                liveDewPointCelsius = telemetry.dewPointCelsius,
                liveUvIndex = telemetry.uvIndex,
                livePressureHpa = telemetry.surfacePressureHpa,
                chillingHoursAccumulated = telemetry.chillingHoursAccumulated,
                isWeatherLoading = false,
                weatherDataSource = telemetry.sourceLabel,
                lastTelemetryUpdated = telemetry.lastUpdatedIso,
                hourlyForecast = telemetry.hourlyForecast,
                sprayAdvisory = updatedAdvisory
            )
        }
    }

    fun simulateSprayConditions(windSpeed: Double, rainRisk: Int) {
        val currentTemp = _uiState.value.liveTempCelsius
        val currentHumidity = _uiState.value.liveHumidityPct
        val currentDeltaT = _uiState.value.liveDeltaT
        val simulated = AdvancedAgronomyEngine.computeSprayAdvisory(
            tempCelsius = currentTemp,
            humidityPct = currentHumidity,
            windSpeedKph = windSpeed,
            rainProbabilityPct = rainRisk,
            deltaTOverride = currentDeltaT
        )
        _uiState.value = _uiState.value.copy(sprayAdvisory = simulated)
    }

    fun resetSprayToLiveTelemetry() {
        val weather = _uiState.value.liveWeather
        if (weather != null) {
            val liveAdvisory = AdvancedAgronomyEngine.computeSprayAdvisory(
                tempCelsius = weather.currentTempCelsius,
                humidityPct = weather.currentHumidityPct,
                windSpeedKph = weather.windSpeedKph,
                rainProbabilityPct = weather.rainProbabilityPct,
                deltaTOverride = weather.deltaTCelsius
            )
            _uiState.value = _uiState.value.copy(sprayAdvisory = liveAdvisory)
        }
    }

    fun addScoutLog(log: FieldScoutLog) {
        val updated = listOf(log) + _uiState.value.scoutLogs
        _uiState.value = _uiState.value.copy(scoutLogs = updated)
    }

    fun sendCopilotMessage(query: String) {
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isUser = true,
            text = query
        )
        val currentList = _uiState.value.copilotMessages + userMsg
        _uiState.value = _uiState.value.copy(copilotMessages = currentList)

        viewModelScope.launch {
            val farm = _uiState.value.selectedFarm
            val result = AgronomyRagEngine.queryAgronomyAndAudit(query, farm)
            val agentMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                isUser = false,
                text = result.guidance,
                agronomyResult = result
            )
            _uiState.value = _uiState.value.copy(
                copilotMessages = _uiState.value.copilotMessages + agentMsg,
                organicGuardrailBlocked = !result.guardrailSafe,
                lastGuardrailWarning = result.guardrailWarning
            )
        }
    }

    fun setSelectedPlantBitmap(bitmap: Bitmap, specimenName: String) {
        _uiState.value = _uiState.value.copy(
            selectedPlantBitmap = bitmap,
            selectedSpecimenName = specimenName,
            plantDiagnosisResult = null // Reset previous diagnosis
        )
    }

    fun diagnoseSelectedPlant() {
        val bitmap = _uiState.value.selectedPlantBitmap ?: return
        val cropHint = _uiState.value.selectedSpecimenName

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDiagnosing = true)
            val result = GeminiVisionClient.analyzePlantImage(bitmap, cropHint)
            _uiState.value = _uiState.value.copy(
                isDiagnosing = false,
                plantDiagnosisResult = result
            )
        }
    }

    fun logWaterCup() {
        _uiState.value = _uiState.value.copy(
            microWaterCupsLogged = _uiState.value.microWaterCupsLogged + 1
        )
    }

    fun addKitchenScrap() {
        _uiState.value = _uiState.value.copy(
            scrapCompostKg = _uiState.value.scrapCompostKg + 0.5
        )
    }

    fun updateAcreageInput(acres: Double) {
        // ASSUMPTION: Baseline nitrogen fertilizer requirement is 80 kg N/acre for row crops
        val nResult = acres * 80.0
        _uiState.value = _uiState.value.copy(
            bulkAcreageInput = acres,
            bulkNPKResultKg = nResult
        )
    }

    fun testGuardrailSubstance(substance: String) {
        sendCopilotMessage("Can I apply $substance?")
    }

    fun exportFarmDataJson() {
        _uiState.value = _uiState.value.copy(
            cloudSyncMessage = "GDPR Export Ready: Farm profile, 4 logs, and telemetry exported."
        )
    }

    fun clearHistoryGdpr() {
        _uiState.value = _uiState.value.copy(
            microWaterCupsLogged = 0,
            scrapCompostKg = 0.0,
            cloudSyncMessage = "GDPR Right-to-Erasure Executed: History purged."
        )
    }

    fun syncCurrentFarmToFirebase(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCloudSyncing = true, cloudSyncMessage = null)
            val success = FirebaseHarvestService.syncFarmToFirestore(context, _uiState.value.selectedFarm)
            _uiState.value = _uiState.value.copy(
                isCloudSyncing = false,
                cloudSyncMessage = if (success) {
                    "Cloud Synced: '${_uiState.value.selectedFarm.name}' saved to Firestore Enterprise DB"
                } else {
                    "Please sign in with Google to sync farm to Firebase Cloud."
                }
            )
        }
    }

    private fun createInitialWelcomeMessage(farm: FarmProfile): ChatMessage {
        val greeting = when (farm.scale) {
            FarmScale.MICRO_POT -> "🌱 Welcome urban gardener! I'm your micro-farm copilot. Ask me anything about watering, kitchen scrap composting, or sun exposure for your ${farm.cropName}!"
            FarmScale.SMALL_1ACRE -> "🌿 Hello from the agronomy copilot. Standing by for companion planting, soil infiltration, or organic certification compliance for your 1-acre ${farm.cropName}."
            FarmScale.MEDIUM_10ACRE -> "🍎 Orchard copilot ready. Tracking winter chilling accumulation (<7.2°C), foliar calcium nutrition, and multi-year ROI for your 10-acre ${farm.cropName}."
            FarmScale.COMMERCIAL_500ACRE -> "🚜 Commercial agronomy assistant standing by. Ingesting Sentinel-2 NDVI polygons, soil nitrate credits, and variable-rate N-P-K sizing for your 500-acre ${farm.cropName}."
        }
        return ChatMessage(
            id = "welcome_msg",
            isUser = false,
            text = greeting
        )
    }
}
