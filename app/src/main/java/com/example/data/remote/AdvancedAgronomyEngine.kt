package com.example.data.remote

import com.example.data.model.CropRotationalRecommendation
import com.example.data.model.FarmProfile
import com.example.data.model.FarmScale
import com.example.data.model.SprayAdvisory
import com.example.data.model.SprayConditionRating

object AdvancedAgronomyEngine {

    /**
     * Calculates meteorological spray drift risk & thermal inversion hazards
     * based on air temperature, wind velocity, humidity, and atmospheric Delta T.
     * Standards align with USDA-ARS and APVMA agricultural spray drift guidelines.
     */
    fun computeSprayAdvisory(
        tempCelsius: Double,
        humidityPct: Double,
        windSpeedKph: Double = 9.2,
        rainProbabilityPct: Int = 12,
        deltaTOverride: Double? = null
    ): SprayAdvisory {
        val deltaT = deltaTOverride ?: OpenMeteoClient.calculateDeltaT(tempCelsius, humidityPct)

        val (rating, summary, inversionRisk, nozzle) = when {
            windSpeedKph > 18.0 -> {
                Quad(
                    SprayConditionRating.HAZARDOUS,
                    "High wind drift hazard (>18 km/h). Severe risk of off-target drift onto sensitive neighboring crops or watercourses.",
                    false,
                    "Ultra-Coarse (XC) Air-Induction Nozzles only"
                )
            }
            windSpeedKph < 3.0 && tempCelsius < 15.0 -> {
                Quad(
                    SprayConditionRating.HAZARDOUS,
                    "Calm air surface temperature inversion warning (<3 km/h). Droplets may stay suspended and drift unpredictably across long distances.",
                    true,
                    "Wait for positive breeze (4-15 km/h)"
                )
            }
            rainProbabilityPct >= 60 -> {
                Quad(
                    SprayConditionRating.HAZARDOUS,
                    "High imminent rainfall risk ($rainProbabilityPct%). Chemical wash-off will breach rainfast intervals.",
                    false,
                    "Delay application until rain front passes"
                )
            }
            deltaT > 8.0 || tempCelsius > 30.0 -> {
                Quad(
                    SprayConditionRating.CAUTION,
                    "High evaporation stress (Delta T = ${"%.1f".format(deltaT)}°C). High risk of fine droplet volatilization before leaf uptake.",
                    false,
                    "Coarse (C) to Very Coarse (VC) Nozzles; consider spray adjuvant"
                )
            }
            deltaT < 2.0 && humidityPct > 85.0 -> {
                Quad(
                    SprayConditionRating.CAUTION,
                    "Excessive humidity / low Delta T (<2.0°C). Extended leaf wetness may induce chemical runoff or delayed drying.",
                    false,
                    "Medium (M) to Coarse (C) Nozzles"
                )
            }
            else -> {
                Quad(
                    SprayConditionRating.OPTIMAL,
                    "Ideal application window. Moderate 4-15 km/h airflow, favorable Delta T, and acceptable rainfast margin.",
                    false,
                    "Air-Induction Coarse (C) Nozzle for optimal canopy coverage"
                )
            }
        }

        return SprayAdvisory(
            rating = rating,
            summary = summary,
            windSpeedKph = windSpeedKph,
            maxWindThresholdKph = 15.0,
            rainRiskPct = rainProbabilityPct,
            tempCelsius = tempCelsius,
            inversionRisk = inversionRisk,
            deltaTCelsius = deltaT,
            recommendedNozzle = nozzle,
            legalNotice = "Always read and adhere to pesticide product EPA registration labels. Never spray during thermal inversions."
        )
    }

    /**
     * Generates rotational crop plans for pest-cycle disruption,
     * biological nitrogen fixation, and soil biome balance.
     */
    fun computeRotationalPlan(farm: FarmProfile): List<CropRotationalRecommendation> {
        val cropLower = farm.cropName.lowercase()
        return when {
            cropLower.contains("wheat") || cropLower.contains("grain") -> listOf(
                CropRotationalRecommendation(
                    season = "Year +1 Double Crop / Summer",
                    suggestedCrop = "Grain Sorghum or Soybeans",
                    family = "Poaceae / Fabaceae",
                    agronomicBenefit = "Legume rotation adds nitrogen credit and utilizes subsoil moisture after winter wheat harvest.",
                    breakPestCycle = "Breaks Cephus cinctus (Wheat stem sawfly) and take-all fungus (Gaeumannomyces graminis) spore cycles.",
                    nitrogenImpactKgPerAcre = +35.0
                ),
                CropRotationalRecommendation(
                    season = "Autumn Cover Crop",
                    suggestedCrop = "Hairy Vetch & Austrian Winter Pea",
                    family = "Fabaceae (Legume)",
                    agronomicBenefit = "Extensive biomass generation and deep nitrogen fixation for next cereal season.",
                    breakPestCycle = "Prevents soil erosion across broadacre acreage during winter winds.",
                    nitrogenImpactKgPerAcre = +40.0
                ),
                CropRotationalRecommendation(
                    season = "Year +2 Succession",
                    suggestedCrop = "Broadacre Sunflowers or Canola",
                    family = "Asteraceae / Brassicaceae",
                    agronomicBenefit = "Taproot extracts deep subsoil nutrients and breaks grass-specific weed pressures.",
                    breakPestCycle = "Controls cheatgrass and feral rye populations.",
                    nitrogenImpactKgPerAcre = -15.0
                )
            )
            cropLower.contains("corn") || cropLower.contains("maize") -> listOf(
                CropRotationalRecommendation(
                    season = "Next Spring (Year +1)",
                    suggestedCrop = "Soybeans or Field Peas",
                    family = "Fabaceae (Legume)",
                    agronomicBenefit = "Symbiotic nitrogen fixation restores soil reserves, cutting required synthetic N by 45 kg/acre.",
                    breakPestCycle = "Breaks Western Corn Rootworm & European Corn Borer reproductive cycles.",
                    nitrogenImpactKgPerAcre = +40.0
                ),
                CropRotationalRecommendation(
                    season = "Autumn Cover Crop",
                    suggestedCrop = "Winter Cereal Rye",
                    family = "Poaceae (Grass)",
                    agronomicBenefit = "Allelopathic weed suppression, high organic matter accumulation, and erosion control.",
                    breakPestCycle = "Scavenges residual autumn nitrates to prevent groundwater leaching.",
                    nitrogenImpactKgPerAcre = +10.0
                ),
                CropRotationalRecommendation(
                    season = "Year +2 Succession",
                    suggestedCrop = "Soft Red Winter Wheat",
                    family = "Poaceae (Grain)",
                    agronomicBenefit = "Fibrous root structure improves soil aggregation and tilth before returning to row crops.",
                    breakPestCycle = "Disrupts soybean cyst nematode cycle.",
                    nitrogenImpactKgPerAcre = -20.0
                )
            )
            cropLower.contains("tomato") || cropLower.contains("potato") || cropLower.contains("eggplant") -> listOf(
                CropRotationalRecommendation(
                    season = "Next Rotation (Year +1)",
                    suggestedCrop = "Snap Beans or Sweet Peas",
                    family = "Fabaceae (Legume)",
                    agronomicBenefit = "Biological nitrogen credit and low heavy-metal uptake.",
                    breakPestCycle = "Interrupts Solanaceae early/late blight (Phytophthora infestans) spore soil banks.",
                    nitrogenImpactKgPerAcre = +30.0
                ),
                CropRotationalRecommendation(
                    season = "Cover / Succession (Year +2)",
                    suggestedCrop = "Mustard Biofumigant (Brassica)",
                    family = "Brassicaceae",
                    agronomicBenefit = "Glucosinolates release natural biofumigants upon soil incorporation, targeting soil nematodes.",
                    breakPestCycle = "Suppresses Verticillium wilt microsclerotia and root-knot nematodes.",
                    nitrogenImpactKgPerAcre = +15.0
                ),
                CropRotationalRecommendation(
                    season = "Year +3 Cycle",
                    suggestedCrop = "Winter Squash or Cucumbers",
                    family = "Cucurbitaceae",
                    agronomicBenefit = "Broadleaf canopy shades out weed seeds and allows deep soil moisture recovery.",
                    breakPestCycle = "Maintains at least 3 seasons before returning Solanaceae to same ground.",
                    nitrogenImpactKgPerAcre = -10.0
                )
            )
            cropLower.contains("apple") || cropLower.contains("cherry") || farm.scale == FarmScale.MEDIUM_10ACRE -> listOf(
                CropRotationalRecommendation(
                    season = "Alleyway Understory",
                    suggestedCrop = "White Dutch Clover & Fescue",
                    family = "Fabaceae / Poaceae",
                    agronomicBenefit = "Perennial ground cover fixing atmospheric nitrogen while withstanding tractor equipment traffic.",
                    breakPestCycle = "Harbors predatory mites (Typhlodromus pyri) that consume harmful orchard spider mites.",
                    nitrogenImpactKgPerAcre = +25.0
                ),
                CropRotationalRecommendation(
                    season = "Perimeter Pollinator Strip",
                    suggestedCrop = "Phacelia, Buckwheat & Sunflowers",
                    family = "Hydrophyllaceae / Polygonaceae",
                    agronomicBenefit = "Attracts native solitary bees (Osmia cornifrons) and hoverfly pollinators for bloom season.",
                    breakPestCycle = "Supports braconid parasitoid wasps targeting leafrollers.",
                    nitrogenImpactKgPerAcre = +5.0
                )
            )
            else -> listOf(
                CropRotationalRecommendation(
                    season = "Next Planting Season",
                    suggestedCrop = "Crimson Clover & Vetch",
                    family = "Fabaceae",
                    agronomicBenefit = "Fixes 35-50 kg/acre atmospheric nitrogen in upper root zone.",
                    breakPestCycle = "Breaks monoculture soil pathogen concentrations and boosts earthworm density.",
                    nitrogenImpactKgPerAcre = +35.0
                ),
                CropRotationalRecommendation(
                    season = "Follow-up Cycle",
                    suggestedCrop = "Daikon Tillage Radish",
                    family = "Brassicaceae",
                    agronomicBenefit = "Deep taproot (up to 70cm) bio-drills hardpan soil compaction, enhancing rainwater infiltration.",
                    breakPestCycle = "Natural biofumigant suppressive to root rot fungi.",
                    nitrogenImpactKgPerAcre = +12.0
                )
            )
        }
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
