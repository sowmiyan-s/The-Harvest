package com.example.data.remote

import com.example.data.model.FarmProfile
import com.example.data.model.FarmScale

data class AgronomyAdviceResult(
    val title: String,
    val guidance: String,
    val sourceCitation: String,
    val optimalPhRange: String,
    val waterNeedsMmWeek: String,
    val guardrailSafe: Boolean,
    val guardrailWarning: String? = null,
    val latencyMs: Int,
    val tokensAudit: Int
)

object AgronomyRagEngine {

    fun queryAgronomyAndAudit(
        userQuery: String,
        farm: FarmProfile
    ): AgronomyAdviceResult {
        val qLower = userQuery.lowercase()
        val isOrganic = farm.isOrganic

        // 1. Guardrail Safety Interception Check
        val prohibitedSynthetics = listOf(
            "glyphosate", "roundup", "atrazine", "chlorpyrifos", "synthetic urea",
            "ammonium nitrate", "malathion", "paraquat", "2,4-d", "neonicotinoid"
        )
        val detectedProhibited = prohibitedSynthetics.filter { qLower.contains(it) }

        if (isOrganic && detectedProhibited.isNotEmpty()) {
            val subName = detectedProhibited.joinToString(", ")
            return AgronomyAdviceResult(
                title = "SAFETY GUARDRAIL ALERT - Organic Rule Violation",
                guidance = "PROHIBITED SUBSTANCE BLOCKED: '$subName' is strictly banned on certified organic holdings under USDA NOP §205.105 and EU Organic Regulation 2018/848.\n\n" +
                        "Applying synthetic herbicides or chemical fertilizers causes immediate revocation of organic status for a 36-month transition period.\n\n" +
                        "Recommended Organic Alternatives:\n" +
                        "• Weed Control: Silage tarp occultation, roller-crimper cover cropping, or OMRI-listed clove/citrus acid burn-down spray.\n" +
                        "• Insect Control: OMRI-listed cold-pressed neem oil (azadirachtin), Bacillus thuringiensis (Bt), or beneficial predatory insects.\n" +
                        "• Nutrient Feeding: Composted chicken manure, kelp meal, feather meal, or fish hydrolysate.",
                sourceCitation = "USDA National Organic Program §205.105 & OMRI Product Catalog (2024)",
                optimalPhRange = "N/A (Prohibited Chemical Blocked)",
                waterNeedsMmWeek = "N/A",
                guardrailSafe = false,
                guardrailWarning = "Safety Guardrail intercepted prohibited substance '$subName' on organic farm '${farm.name}'.",
                latencyMs = 210,
                tokensAudit = 125
            )
        }

        // 2. Crop Knowledge RAG Lookup
        val cropLower = farm.cropName.lowercase()
        val adviceText: String
        val citation: String
        val phRange: String
        val waterMm: String

        if (cropLower.contains("basil")) {
            adviceText = "Sweet basil is a tender summer annual sensitive to chilling injury below 10°C. Maintain loose, well-draining potting soil with high organic matter. Pinch terminal flowering spikes promptly to stimulate lateral leaf branching and maintain volatile essential oil concentrations (linalool, eugenol). Water at root level to prevent fungal Fusarium and downy mildew leaf infection."
            citation = "University of Florida IFAS Extension Publication ENH987 'Commercial Production of Culinary Herbs'"
            phRange = "6.0 - 7.5"
            waterMm = "25.0 mm/week"
        } else if (cropLower.contains("tomato")) {
            adviceText = "Tomatoes require consistent, deep root irrigation to avoid blossom-end rot caused by localized calcium uptake deficiency during hot transpiration periods. In organic production, companion plant with French marigolds to deter root-knot nematodes (Meloidogyne spp.). Support indeterminate vines with trellis twine and prune lower suckers up to the first flower cluster."
            citation = "UC Davis Division of Agriculture and Natural Resources Publication 8159 'Organic Tomato Production'"
            phRange = "6.2 - 6.8"
            waterMm = "35.0 - 45.0 mm/week"
        } else if (cropLower.contains("apple")) {
            adviceText = "Honeycrisp apple trees are deciduous perennials requiring 800 to 1,000 chilling hours (<7.2°C) to break dormant endodormancy. Prone to bitter pit disorder; apply seasonal foliar calcium chloride sprays starting at petal fall. Regulate drip irrigation to 70% crop evapotranspiration (ETc) post-bloom to prevent excessive vegetative shoot growth."
            citation = "Washington State University Tree Fruit Extension 'Honeycrisp Orchard Nutrition & Crop Load Management'"
            phRange = "6.0 - 7.0"
            waterMm = "30.0 mm/week (tuned to ETc)"
        } else if (cropLower.contains("wheat")) {
            adviceText = "Hard red winter wheat requires a vernalization period (4 to 8 weeks between 0°C and 7°C) to induce reproductive stem elongation (Feekes Stage 6). Soil nitrogen should be split: 30% incorporated at seeding and 70% broadcast as early spring topdress. Monitor NDVI satellite vigor for early detection of flag leaf fungal stripe rust."
            citation = "Kansas State University Agricultural Experiment Station MF-2300 'Wheat Production Guidelines'"
            phRange = "6.0 - 7.5"
            waterMm = "20.0 mm/week"
        } else {
            adviceText = "Maintain balanced soil fertility guided by annual soil testing. Adjust irrigation frequency according to daily reference evapotranspiration (ETo) and local root zone depletion fractions."
            citation = "FAO Irrigation and Drainage Paper 56 'Crop Evapotranspiration Guidelines'"
            phRange = "6.0 - 7.0"
            waterMm = "30.0 mm/week"
        }

        return AgronomyAdviceResult(
            title = "Agronomic Extension Advisory",
            guidance = adviceText,
            sourceCitation = citation,
            optimalPhRange = phRange,
            waterNeedsMmWeek = waterMm,
            guardrailSafe = true,
            guardrailWarning = null,
            latencyMs = 380,
            tokensAudit = 290
        )
    }
}
