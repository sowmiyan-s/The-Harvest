package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agent.AgentDefinition
import com.example.agent.AgentRegistry
import com.example.data.firebase.FirebaseHarvestService
import com.example.data.model.DashboardSpec
import com.example.data.model.FarmProfile
import com.example.data.model.FarmScale
import com.example.data.repository.HarvestRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppViewTab(val label: String) {
    DYNAMIC_DASHBOARD("Dynamic Dashboard"),
    AGENT_TOPOLOGY("8 Agents Architecture"),
    DATA_MODELS("Data & Spatial Models"),
    SCHEMA_SPEC("JSON Schema Spec")
}

data class HarvestUiState(
    val currentTab: AppViewTab = AppViewTab.DYNAMIC_DASHBOARD,
    val selectedFarm: FarmProfile,
    val dashboardSpec: DashboardSpec,
    val allFarms: List<FarmProfile>,
    val allAgents: List<AgentDefinition> = AgentRegistry.ALL_AGENTS,
    // Interactive widget telemetry & user inputs (labeled assumptions)
    val microWaterCupsLogged: Int = 3,
    val scrapCompostKg: Double = 1.4,
    val simulatedSoilMoisturePct: Double = 34.2,
    val chillingHoursAccumulated: Int = 680,
    val chillingHoursRequired: Int = 800,
    val ndviScore: Double = 0.74,
    val bulkAcreageInput: Double = 500.0,
    val bulkNPKResultKg: Double = 40000.0,
    val organicGuardrailBlocked: Boolean = false,
    val lastGuardrailWarning: String? = null,
    // Firebase Cloud Sync & Auth State
    val firebaseUser: FirebaseUser? = null,
    val isCloudSyncing: Boolean = false,
    val cloudSyncMessage: String? = null
)

class HarvestViewModel(
    private val repository: HarvestRepository = HarvestRepository()
) : ViewModel() {

    private val initialFarm = repository.availableFarms.first()
    private val initialSpec = repository.getDashboardSpecForFarm(initialFarm)

    private val _uiState = MutableStateFlow(
        HarvestUiState(
            selectedFarm = initialFarm,
            dashboardSpec = initialSpec,
            allFarms = repository.availableFarms,
            firebaseUser = FirebaseHarvestService.currentUser
        )
    )
    val uiState: StateFlow<HarvestUiState> = _uiState.asStateFlow()

    init {
        // Observe auth state changes
        FirebaseHarvestService.auth.addAuthStateListener { auth ->
            _uiState.value = _uiState.value.copy(firebaseUser = auth.currentUser)
        }
    }

    fun selectTab(tab: AppViewTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun selectFarm(farm: FarmProfile) {
        val newSpec = repository.getDashboardSpecForFarm(farm)
        _uiState.value = _uiState.value.copy(
            selectedFarm = farm,
            dashboardSpec = newSpec,
            bulkAcreageInput = farm.areaAcres,
            bulkNPKResultKg = farm.areaAcres * 80.0
        )
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
        val isOrganic = _uiState.value.selectedFarm.isOrganic
        val prohibitedSynthetic = listOf("glyphosate", "synthetic urea", "chlorpyrifos", "atrazine", "synthetic ammonium")
        val isProhibited = prohibitedSynthetic.any { substance.contains(it, ignoreCase = true) }

        if (isOrganic && isProhibited) {
            _uiState.value = _uiState.value.copy(
                organicGuardrailBlocked = true,
                lastGuardrailWarning = "SAFETY GUARDRAIL BLOCKED: '$substance' is prohibited on certified organic farm '${_uiState.value.selectedFarm.name}' (NOP §205.105 rule violation)."
            )
        } else {
            _uiState.value = _uiState.value.copy(
                organicGuardrailBlocked = false,
                lastGuardrailWarning = "GUARDRAIL PASSED: Advice for '$substance' cleared under ${_uiState.value.selectedFarm.scale.name} guidelines."
            )
        }
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
}
