package com.example

import com.example.agent.AgentRegistry
import com.example.data.model.FarmScale
import com.example.data.remote.AgronomyRagEngine
import com.example.data.repository.HarvestRepository
import org.junit.Assert.*
import org.junit.Test

class AgronomyLogicTest {

    private val repository = HarvestRepository()

    @Test
    fun `test agent registry contains all 8 agronomy agents`() {
        val agents = AgentRegistry.ALL_AGENTS
        assertEquals(8, agents.size)
        val agentIds = agents.map { it.id }.toSet()
        assertTrue(agentIds.contains("agent_orchestrator"))
        assertTrue(agentIds.contains("agent_persona_ui"))
        assertTrue(agentIds.contains("agent_weather_soil"))
        assertTrue(agentIds.contains("agent_crop_knowledge"))
        assertTrue(agentIds.contains("agent_inputs_cost"))
        assertTrue(agentIds.contains("agent_vision_satellite"))
        assertTrue(agentIds.contains("agent_guardrail"))
        assertTrue(agentIds.contains("agent_memory"))
    }

    @Test
    fun `test available farm presets span all scales`() {
        val farms = repository.availableFarms
        assertTrue(farms.isNotEmpty())
        val scales = farms.map { it.scale }.toSet()
        assertTrue(scales.contains(FarmScale.MICRO_POT))
        assertTrue(scales.contains(FarmScale.SMALL_1ACRE))
        assertTrue(scales.contains(FarmScale.MEDIUM_10ACRE))
        assertTrue(scales.contains(FarmScale.COMMERCIAL_500ACRE))
    }

    @Test
    fun `test organic guardrail intercepts forbidden synthetic pesticide`() {
        val microFarm = repository.availableFarms.first { it.scale == FarmScale.MICRO_POT }
        val response = AgronomyRagEngine.queryAgronomyAndAudit(
            userQuery = "Can I spray glyphosate or Roundup on my urban herbs?",
            farm = microFarm
        )
        assertFalse("Synthetic herbicide should be flagged unsafe for organic farm", response.guardrailSafe)
        assertNotNull(response.guardrailWarning)
        assertTrue(response.guardrailWarning?.contains("prohibited substance") == true)
    }

    @Test
    fun `test safe query passes agronomy guardrail audit`() {
        val smallFarm = repository.availableFarms.first { it.scale == FarmScale.SMALL_1ACRE }
        val response = AgronomyRagEngine.queryAgronomyAndAudit(
            userQuery = "What is the recommended soil pH and neem oil spray timing for tomatoes?",
            farm = smallFarm
        )
        assertTrue("Neem oil and organic pH question should be safe", response.guardrailSafe)
        assertNull(response.guardrailWarning)
        assertTrue(response.guidance.isNotEmpty())
    }

    @Test
    fun `test dashboard spec generation dynamically configures widgets`() {
        val industrialFarm = repository.availableFarms.first { it.scale == FarmScale.COMMERCIAL_500ACRE }
        val spec = repository.getDashboardSpecForFarm(industrialFarm)
        assertEquals(FarmScale.COMMERCIAL_500ACRE, spec.scale)
        assertTrue(spec.widgets.isNotEmpty())
        assertTrue(spec.widgets.any { it.title.contains("Bulk") || it.title.contains("NDVI") })
    }
}
