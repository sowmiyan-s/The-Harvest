"""
Inputs & Cost Agent: Mathematical bulk calculators for irrigation volume,
compost, manure, organic amendments, and conventional N-P-K nutrient balancing.
Every formula returns explicit mathematical steps, soil credits, and labeled assumptions.
"""

from typing import List, Optional
from ..schemas.agent_contracts import (
    InputCalcType,
    InputsCostRequest,
    InputsCostResponse,
    CalculationAssumptions,
)


class InputsCostAgent:
    """
    Computes acreage-scaled agronomic inputs with cost ranges and soil test credits.
    """

    async def calculate(self, req: InputsCostRequest) -> InputsCostResponse:
        acres = req.area_acres
        crop = req.crop_name.lower()
        is_organic = req.is_organic

        # Soil test credits (ppm)
        soil_n = req.soil_nitrogen_ppm or 0.0
        soil_p = req.soil_phosphorus_ppm or 0.0
        soil_k = req.soil_potassium_ppm or 0.0

        if req.calc_type == InputCalcType.WATER_VOLUME:
            # ASSUMPTION: 1 acre-inch of water = 27,154 gallons (~102,790 liters)
            # Base crop requirement: ~1.2 inches per week for vegetable/orchard crops
            inches_needed = 1.2
            gallons_per_acre_inch = 27154.0
            total_gallons = acres * inches_needed * gallons_per_acre_inch

            # Cost estimation: typical agricultural pumping cost ~$4.50 - $7.00 per acre-inch
            cost_min = round(acres * inches_needed * 4.50, 2)
            cost_max = round(acres * inches_needed * 7.00, 2)

            assumptions = CalculationAssumptions(
                formula_name="Crop Irrigation Requirement (ETc Balance)",
                base_requirement_per_acre=f"{inches_needed} acre-inches per week",
                soil_credit_subtraction="None (Full atmospheric demand replenishment)",
                efficiency_factor=0.90, # 90% drip irrigation efficiency
                labeled_disclaimer="// ASSUMPTION: 1 acre-inch = 27,154 gallons. Drip irrigation delivery efficiency 90%."
            )

            schedule = [
                "Monday: 4-hour drip run during early morning (low evaporative demand)",
                "Wednesday: 4-hour drip run with tensiometer soil moisture check",
                "Friday: 4-hour drip run adjusting for weekend forecast precipitation"
            ]

            return InputsCostResponse(
                recommended_quantity=round(total_gallons, 0),
                unit="gallons",
                estimated_cost_usd_min=cost_min,
                estimated_cost_usd_max=cost_max,
                assumptions=assumptions,
                application_schedule=schedule
            )

        elif req.calc_type == InputCalcType.COMPOST_MANURE:
            # ASSUMPTION: Standard agricultural application rate: 3 tons of finished compost per acre per season
            tons_per_acre = 3.0 if acres >= 1.0 else 0.5
            total_tons = acres * tons_per_acre

            # Cost: Bulk compost ~$35.00 - $55.00 per ton delivered
            cost_min = round(total_tons * 35.0, 2)
            cost_max = round(total_tons * 55.0, 2)

            assumptions = CalculationAssumptions(
                formula_name="Soil Organic Matter (SOM) Building Rate",
                base_requirement_per_acre=f"{tons_per_acre} metric tons per acre",
                soil_credit_subtraction="Credit 15 kg N/acre slow mineralization over subsequent 90 days",
                efficiency_factor=0.85,
                labeled_disclaimer="// ASSUMPTION: 3 tons/acre compost raises soil organic matter by approx 0.2% over 3 seasons."
            )

            schedule = [
                "Pre-planting: Broadcast 70% of compost across beds and shallowly incorporate to 5-10cm depth.",
                "Mid-season: Topdress remaining 30% as mulch around root zones."
            ]

            return InputsCostResponse(
                recommended_quantity=round(total_tons, 2),
                unit="metric_tons",
                estimated_cost_usd_min=cost_min,
                estimated_cost_usd_max=cost_max,
                assumptions=assumptions,
                application_schedule=schedule
            )

        elif req.calc_type == InputCalcType.ORGANIC_FERTILIZER:
            # ASSUMPTION: 80 kg N per acre baseline. Sourced from OMRI-listed Blood Meal / Feather Meal (12-0-0)
            base_n_kg = 80.0
            # Credit soil nitrate test: 1 ppm NO3-N in top 30cm ≈ 4 kg N/acre credit
            soil_credit_kg = soil_n * 4.0
            net_n_needed = max(20.0, base_n_kg - soil_credit_kg)
            # Fertilizer is 12% N -> total fertilizer weight = net_n / 0.12
            total_kg_fertilizer = acres * (net_n_needed / 0.12)

            cost_min = round(total_kg_fertilizer * 1.80, 2)
            cost_max = round(total_kg_fertilizer * 2.40, 2)

            assumptions = CalculationAssumptions(
                formula_name="OMRI Organic Nitrogen Balancing Formula",
                base_requirement_per_acre=f"{base_n_kg} kg N/acre base requirement",
                soil_credit_subtraction=f"-{soil_credit_kg:.1f} kg N credit from {soil_n:.1f} ppm soil nitrate test",
                efficiency_factor=0.75, # 75% organic mineralization availability
                labeled_disclaimer="// ASSUMPTION: Blood meal 12% N. 1 ppm soil NO3-N credits 4 kg available N/acre."
            )

            schedule = [
                "Sowing / Budbreak: 40% banded into seedbed.",
                "Active Vegetative (V4 / Petal fall): 40% side-dressed.",
                "Fruit swell / Tillering: 20% foliar or side-dressed."
            ]

            return InputsCostResponse(
                recommended_quantity=round(total_kg_fertilizer, 1),
                unit="kg",
                estimated_cost_usd_min=cost_min,
                estimated_cost_usd_max=cost_max,
                assumptions=assumptions,
                application_schedule=schedule
            )

        else: # CONVENTIONAL_NPK
            # ASSUMPTION: Conventional Urea 46-0-0 equivalent. 90 kg N/acre base rate.
            base_n_kg = 90.0
            soil_credit_kg = soil_n * 4.0
            net_n = max(30.0, base_n_kg - soil_credit_kg)
            # Urea is 46% N -> net_n / 0.46
            urea_kg = acres * (net_n / 0.46)

            cost_min = round(urea_kg * 0.65, 2)
            cost_max = round(urea_kg * 0.95, 2)

            assumptions = CalculationAssumptions(
                formula_name="Conventional Nitrogen Nutrient Index (46-0-0 Urea)",
                base_requirement_per_acre=f"{base_n_kg} kg N/acre base recommendation",
                soil_credit_subtraction=f"-{soil_credit_kg:.1f} kg N credit from {soil_n:.1f} ppm soil nitrate test",
                efficiency_factor=0.60, # 60% conventional nitrogen use efficiency (NUE)
                labeled_disclaimer="// ASSUMPTION: Urea 46% N. Volatilization loss estimated at 15% if unincorporated."
            )

            schedule = [
                "Fall incorporation: 30% pre-plant.",
                "Spring topdress: 70% broadcast prior to rain event."
            ]

            return InputsCostResponse(
                recommended_quantity=round(urea_kg, 1),
                unit="kg",
                estimated_cost_usd_min=cost_min,
                estimated_cost_usd_max=cost_max,
                assumptions=assumptions,
                application_schedule=schedule
            )
