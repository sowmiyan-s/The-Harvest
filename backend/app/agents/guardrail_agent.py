"""
Safety & Guardrail Agent: Enforces chemical compliance and organic certification rules.
Audits every proposed recommendation before it reaches the farmer.
Enforces USDA NOP / EU Organic restrictions and mandates PPE safety standards.
"""

from typing import List, Tuple
from ..schemas.agent_contracts import (
    GuardrailCheckRequest,
    GuardrailCheckResponse,
)

# Prohibited substances under USDA NOP § 205.105 & EU Organic Regulation (EU) 2018/848
PROHIBITED_ORGANIC_SUBSTANCES = [
    "glyphosate",
    "roundup",
    "atrazine",
    "chlorpyrifos",
    "synthetic urea",
    "ammonium nitrate",
    "anhydrous ammonia",
    "superphosphate",
    "imidacloprid",
    "malathion",
    "paraquat",
    "2,4-d",
    "dicamba",
    "neonicotinoid",
    "synthetic pyrethroid",
]

# Organic Certified (OMRI Listed) alternatives
ORGANIC_RECOMMENDED_ALTERNATIVES = {
    "glyphosate": "Sheet mulching, silage tarps, mechanical flail mowing, or OMRI-listed citrus oil/caprylic acid herbicide.",
    "chlorpyrifos": "Bacillus thuringiensis (Bt), Spinosad (OMRI-listed), or beneficial predatory nematodes (Steinernema carpocapsae).",
    "synthetic urea": "Feather meal (12-0-0), blood meal (12-0-0), composted chicken manure, or liquid fish hydrolysate.",
    "imidacloprid": "Cold-pressed neem oil (azadirachtin), insecticidal potassium salts of fatty acids, or yellow sticky traps.",
}


class GuardrailAgent:
    """
    Zero-trust safety layer filtering outputs for environmental regulations and human health.
    """

    async def audit_advice(self, req: GuardrailCheckRequest) -> GuardrailCheckResponse:
        advice_lower = req.candidate_advice.lower()
        query_lower = req.user_query.lower()

        detected_prohibited: List[str] = []
        is_violated = False

        # 1. Check for prohibited substances on organic farms
        if req.is_organic_farm:
            for substance in PROHIBITED_ORGANIC_SUBSTANCES:
                if substance in advice_lower or substance in query_lower:
                    detected_prohibited.append(substance)
                    is_violated = True

        if is_violated:
            # Generate sanitized, organic-compliant alternative
            alternatives_list = []
            for sub in detected_prohibited:
                alt = ORGANIC_RECOMMENDED_ALTERNATIVES.get(sub, "OMRI-listed biological or cultural controls")
                alternatives_list.append(f"• Instead of '{sub}': Use {alt}")

            sanitized = (
                f"⚠️ **SAFETY GUARDRAIL ALERT - ORGANIC COMPLIANCE VIOLATION**\n\n"
                f"The requested substance ({', '.join(detected_prohibited)}) is strictly prohibited on certified organic holdings under USDA NOP §205.105 and international organic standards.\n\n"
                f"**Approved Organic Alternatives:**\n" +
                "\n".join(alternatives_list) +
                f"\n\n*Using prohibited chemicals will result in immediate loss of organic certification for a 36-month transition period.*"
            )

            return GuardrailCheckResponse(
                is_safe=False,
                organic_constraint_violated=True,
                blocked_substances_detected=detected_prohibited,
                sanitized_advice=sanitized,
                guardrail_reason=f"Blocked synthetic substances ({', '.join(detected_prohibited)}) for organic farm context."
            )

        # 2. Conventional PPE Safety Advisory
        sanitized = req.candidate_advice
        if any(chem in advice_lower for chem in ["spray", "pesticide", "fertilizer", "herbicide", "fungicide"]):
            sanitized += (
                "\n\n🛡️ **Safety & Application Advisory:**\n"
                "• Wear mandatory Personal Protective Equipment (chemical-resistant nitrile gloves, eye protection, long sleeves).\n"
                "• Observe the EPA Restricted-Entry Interval (REI) and Pre-Harvest Interval (PHI) printed on the product label.\n"
                "• Do not apply during high wind (>10 mph) or when pollinator bees are active."
            )

        return GuardrailCheckResponse(
            is_safe=True,
            organic_constraint_violated=False,
            blocked_substances_detected=[],
            sanitized_advice=sanitized,
            guardrail_reason=None
        )
