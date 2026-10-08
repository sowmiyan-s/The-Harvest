"""
Orchestrator Agent: Central router, dispatcher, and response synthesizer.
Coordinates specialist agents, enforces persona tone, audits latency/cost,
and ensures deterministic fallback when downstream services are unavailable.
"""

import time
import uuid
from typing import List, Optional
from ..schemas.agent_contracts import (
    OrchestratorRequest,
    OrchestratorResponse,
    AgentExecutionAudit,
    CropKnowledgeRequest,
    WeatherSoilRequest,
    GuardrailCheckRequest,
    FarmScale,
)
from .crop_knowledge_agent import CropKnowledgeAgent
from .weather_soil_agent import WeatherSoilAgent
from .guardrail_agent import GuardrailAgent


class OrchestratorAgent:
    def __init__(self):
        self.crop_agent = CropKnowledgeAgent()
        self.weather_agent = WeatherSoilAgent()
        self.guardrail_agent = GuardrailAgent()

    async def run(self, req: OrchestratorRequest) -> OrchestratorResponse:
        t_start = time.time()
        audits: List[AgentExecutionAudit] = []
        assumptions: List[str] = []
        action_steps: List[str] = []

        ctx = req.farm_context
        query = req.user_query.lower()

        # Step 1: Detect intent and dispatch to Crop Knowledge Agent
        crop_t0 = time.time()
        topic = "general"
        if "water" in query or "irrigate" in query:
            topic = "irrigation"
        elif "fertiliz" in query or "feed" in query or "compost" in query:
            topic = "nutrients"
        elif "pest" in query or "spray" in query or "bug" in query or "disease" in query:
            topic = "pest_disease"

        crop_resp = await self.crop_agent.query_agronomy(
            CropKnowledgeRequest(
                crop_name=ctx.crop_common_name,
                question_topic=topic,
                is_organic=ctx.is_organic
            )
        )
        crop_latency = int((time.time() - crop_t0) * 1000)
        citation_str = crop_resp.citations[0].source_name if crop_resp.citations else "FAO Agronomic Guidelines"
        audits.append(
            AgentExecutionAudit(
                agent_name="Crop Knowledge Agent",
                latency_ms=crop_latency,
                tokens_used=180,
                cost_usd=0.00036,
                fallback_used=not crop_resp.is_verified,
                source_citation=citation_str
            )
        )

        # Step 2: If query involves weather, frost, chilling, or irrigation, dispatch to Weather & Soil Agent
        weather_info = ""
        if any(w in query for w in ["weather", "temp", "rain", "chill", "frost", "water", "today", "forecast"]):
            w_t0 = time.time()
            w_resp = await self.weather_agent.get_weather_and_soil(
                WeatherSoilRequest(
                    location=ctx.location,
                    chilling_threshold_celsius=7.2
                )
            )
            w_lat = int((time.time() - w_t0) * 1000)
            weather_info = (
                f"\n\n**Live Telemetry ({w_resp.source}):**\n"
                f"• Current Temperature: {w_resp.current_temp_celsius}°C (Humidity: {w_resp.current_humidity_pct}%)\n"
                f"• Volumetric Topsoil Moisture: {w_resp.soil_moisture_pct}%\n"
                f"• Daily ETc Evapotranspiration: {w_resp.evapotranspiration_daily_mm} mm/day\n"
                f"• Cumulative Chilling (<7.2°C): {w_resp.accumulated_chilling_hours} hrs"
            )
            if w_resp.frost_warning:
                weather_info += "\n• ⚠️ **FROST ALERT: Below 0°C forecast detected in next 7 days!**"

            audits.append(
                AgentExecutionAudit(
                    agent_name="Weather & Soil Agent",
                    latency_ms=w_lat,
                    tokens_used=60,
                    cost_usd=0.00000, # Open-Meteo free API
                    fallback_used="fallback" in w_resp.source,
                    source_citation="Open-Meteo Global Hydrology Model"
                )
            )
            assumptions.append("// ASSUMPTION: Evapotranspiration calculated using standard FAO-56 Penman-Monteith equation.")

        # Step 3: Synthesize candidate answer in persona tone
        if ctx.scale == FarmScale.MICRO_POT:
            tone = "playful_educational"
            greeting = f"🌱 Hello urban grower! Let's check on your **{ctx.crop_common_name}** on the balcony."
            action_steps = [
                "Touch the top 2 cm of soil to check moisture.",
                "Water with room-temperature water if dry.",
                "Add shredded eggshells or coffee grounds to your scrap bin."
            ]
        elif ctx.scale == FarmScale.SMALL_1ACRE:
            tone = "practical_artisan"
            greeting = f"🌿 Greetings from the market garden. Here is the agronomic status for **{ctx.crop_common_name}**."
            action_steps = [
                "Inspect drip irrigation lines for emitter clogs.",
                "Scout under-leaf foliage for early pest pressure.",
                "Log amendment volume in your seasonal harvest ledger."
            ]
        else: # MEDIUM_10ACRE & COMMERCIAL_500ACRE
            tone = "commercial_agronomic"
            greeting = f"🚜 Field Telemetry & Agronomy Report for **{ctx.crop_common_name}** ({ctx.scale.value})."
            action_steps = [
                "Calibrate variable-rate fertilizer applicators based on soil nitrate test credits.",
                "Monitor Sentinel-2 NDVI canopy vigor index across field boundary.",
                "Verify chemical REI/PHI compliance records before spray rig dispatch."
            ]

        candidate_body = (
            f"{greeting}\n\n"
            f"**Agronomic Guidance:**\n{crop_resp.factual_guidance}\n"
            f"• Optimal pH: {crop_resp.recommended_ph_range[0]} - {crop_resp.recommended_ph_range[1]}\n"
            f"• Weekly Water Demand: {crop_resp.water_requirements_mm_week} mm/week"
            f"{weather_info}\n\n"
            f"**Citations:** {citation_str}"
        )

        # Step 4: Pass through Safety & Guardrail Agent
        g_t0 = time.time()
        guard_resp = await self.guardrail_agent.audit_advice(
            GuardrailCheckRequest(
                user_query=req.user_query,
                candidate_advice=candidate_body,
                is_organic_farm=ctx.is_organic,
                scale=ctx.scale
            )
        )
        g_lat = int((time.time() - g_t0) * 1000)
        audits.append(
            AgentExecutionAudit(
                agent_name="Safety & Guardrail Agent",
                latency_ms=g_lat,
                tokens_used=95,
                cost_usd=0.00019,
                fallback_used=False,
                source_citation="USDA NOP §205.105 & EPA Worker Protection Standards"
            )
        )

        final_answer = guard_resp.sanitized_advice

        # Add total orchestrator latency
        total_lat = int((time.time() - t_start) * 1000)
        audits.insert(
            0,
            AgentExecutionAudit(
                agent_name="Orchestrator Agent",
                latency_ms=total_lat,
                tokens_used=335,
                cost_usd=0.00055,
                fallback_used=False,
                source_citation="The Harvest Multi-Agent Router v1.0"
            )
        )

        return OrchestratorResponse(
            trace_id=req.trace_id,
            answer_markdown=final_answer,
            target_language=ctx.language,
            persona_tone=tone,
            actionable_steps=action_steps,
            guardrail_cleared=guard_resp.is_safe,
            active_assumptions=assumptions,
            agent_audits=audits
        )
