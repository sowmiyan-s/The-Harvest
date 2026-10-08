"""
Persona & UI Agent: Compiles server-driven JSON Dashboard Specifications.
Maps farm profile (scale, crop lifecycle, organic status, language)
to theme tokens, widget priority hierarchy, and communication tone.
"""

from typing import Dict, Any, List
from ..schemas.agent_contracts import (
    FarmScale,
    CropLifecycle,
    WidgetType,
    ThemeSpec,
    WidgetSpecItem,
    DashboardSpec,
    PersonaAgentRequest,
    PersonaAgentResponse,
)


class PersonaUIAgent:
    """
    Evaluates farm context and emits a strictly validated DashboardSpec.
    Ensures that adding a new farm scale requires zero mobile app code releases.
    """

    async def compile_dashboard_spec(self, req: PersonaAgentRequest) -> PersonaAgentResponse:
        ctx = req.farm_context

        # 1. Determine Communication Tone
        if ctx.scale == FarmScale.MICRO_POT:
            tone = "playful_educational"
        elif ctx.scale == FarmScale.SMALL_1ACRE:
            tone = "practical_artisan"
        elif ctx.scale == FarmScale.MEDIUM_10ACRE:
            tone = "commercial_agronomic"
        else: # COMMERCIAL_500ACRE
            tone = "commercial_agronomic"

        # 2. Compile Theme Specification
        if ctx.scale == FarmScale.MICRO_POT:
            theme = ThemeSpec(
                id="pastel_nature",
                primary_color="#2E7D32",
                secondary_color="#81C784",
                background_color="#F1F8E9",
                surface_color="#FFFFFF",
                accent_color="#FFB74D",
                dark_mode=False,
                density="comfortable",
            )
        elif ctx.scale == FarmScale.SMALL_1ACRE:
            theme = ThemeSpec(
                id="earth_organic",
                primary_color="#1B5E20",
                secondary_color="#4CAF50",
                background_color="#FAFAFA",
                surface_color="#FFFFFF",
                accent_color="#F57F17",
                dark_mode=False,
                density="comfortable",
            )
        elif ctx.scale == FarmScale.MEDIUM_10ACRE:
            theme = ThemeSpec(
                id="orchard_amber",
                primary_color="#E65100",
                secondary_color="#FF9800",
                background_color="#FFF8E1",
                surface_color="#FFFFFF",
                accent_color="#2E7D32",
                dark_mode=False,
                density="comfortable",
            )
        else: # COMMERCIAL_500ACRE
            theme = ThemeSpec(
                id="industrial_dark",
                primary_color="#4CAF50",
                secondary_color="#81C784",
                background_color="#121212",
                surface_color="#1E1E1E",
                accent_color="#00E676",
                dark_mode=True,
                density="data_dense",
            )

        # 3. Dynamic Widget Hierarchy Matrix
        widgets: List[WidgetSpecItem] = []
        order = 1

        if ctx.scale == FarmScale.MICRO_POT:
            widgets.append(WidgetSpecItem(
                id="w_cup_water",
                widget_type=WidgetType.WATERING_CUP_LOG,
                title="Cup-by-Cup Watering",
                priority_order=order,
                grid_span=1,
                params={"default_cup_ml": 240}
            ))
            order += 1
            widgets.append(WidgetSpecItem(
                id="w_scrap_compost",
                widget_type=WidgetType.KITCHEN_COMPOST_HELPER,
                title="Kitchen Scrap Compost Helper",
                priority_order=order,
                grid_span=1,
                params={"carbon_nitrogen_ratio": "2:1"}
            ))
            order += 1
            widgets.append(WidgetSpecItem(
                id="w_micro_weather",
                widget_type=WidgetType.WEATHER_SOIL_CARD,
                title="Balcony Climate & Sun",
                priority_order=order,
                grid_span=2,
            ))
            order += 1
            widgets.append(WidgetSpecItem(
                id="w_badges",
                widget_type=WidgetType.MILESTONE_BADGES,
                title="Seedling Milestones",
                priority_order=order,
                grid_span=2,
            ))
            order += 1

        elif ctx.scale == FarmScale.SMALL_1ACRE:
            widgets.append(WidgetSpecItem(
                id="w_weather_soil",
                widget_type=WidgetType.WEATHER_SOIL_CARD,
                title="Soil Moisture & ETc Infiltration",
                priority_order=order,
                grid_span=2,
            ))
            order += 1
            if ctx.crop_lifecycle == CropLifecycle.ANNUAL:
                widgets.append(WidgetSpecItem(
                    id="w_timeline",
                    widget_type=WidgetType.ANNUAL_GROWTH_TIMELINE,
                    title="Season Growth Timeline",
                    priority_order=order,
                    grid_span=2,
                ))
                order += 1
            widgets.append(WidgetSpecItem(
                id="w_calc_organic",
                widget_type=WidgetType.BULK_INPUT_CALCULATOR,
                title="Organic Amendments Calculator",
                priority_order=order,
                grid_span=2,
            ))
            order += 1
            widgets.append(WidgetSpecItem(
                id="w_ledger",
                widget_type=WidgetType.CAPEX_OPEX_LEDGER,
                title="Market Garden Crop Ledger",
                priority_order=order,
                grid_span=2,
            ))
            order += 1

        elif ctx.scale == FarmScale.MEDIUM_10ACRE:
            if ctx.crop_lifecycle == CropLifecycle.PERENNIAL:
                widgets.append(WidgetSpecItem(
                    id="w_chilling",
                    widget_type=WidgetType.WINTER_CHILLING_GAUGE,
                    title="Winter Chilling Accumulator (<7.2°C)",
                    priority_order=order,
                    grid_span=1,
                ))
                order += 1
                widgets.append(WidgetSpecItem(
                    id="w_roi",
                    widget_type=WidgetType.MULTI_YEAR_ROI_TRACKER,
                    title="Perennial Orchard Multi-Year ROI",
                    priority_order=order,
                    grid_span=1,
                ))
                order += 1
            widgets.append(WidgetSpecItem(
                id="w_weather_orchard",
                widget_type=WidgetType.WEATHER_SOIL_CARD,
                title="Orchard Microclimate & ETc",
                priority_order=order,
                grid_span=2,
            ))
            order += 1
            widgets.append(WidgetSpecItem(
                id="w_bulk_calc",
                widget_type=WidgetType.BULK_INPUT_CALCULATOR,
                title="Foliar Nutrition & Drip Irrigation Sizing",
                priority_order=order,
                grid_span=2,
            ))
            order += 1

        else: # COMMERCIAL_500ACRE
            widgets.append(WidgetSpecItem(
                id="w_ndvi",
                widget_type=WidgetType.SATELLITE_NDVI_MAP,
                title="Sentinel-2 Field Polygon NDVI",
                priority_order=order,
                grid_span=2,
            ))
            order += 1
            widgets.append(WidgetSpecItem(
                id="w_weather_dense",
                widget_type=WidgetType.WEATHER_SOIL_CARD,
                title="Multi-Sensor Telemetry & Infiltration",
                priority_order=order,
                grid_span=2,
            ))
            order += 1
            widgets.append(WidgetSpecItem(
                id="w_bulk_commercial",
                widget_type=WidgetType.BULK_INPUT_CALCULATOR,
                title="500-Acre Bulk N-P-K & Anhydrous Sizing",
                priority_order=order,
                grid_span=2,
            ))
            order += 1
            widgets.append(WidgetSpecItem(
                id="w_dense_ledger",
                widget_type=WidgetType.CAPEX_OPEX_LEDGER,
                title="Industrial CapEx/OpEx Cost Centers",
                priority_order=order,
                grid_span=2,
            ))
            order += 1

        # Always include Copilot Quick Actions at end
        widgets.append(WidgetSpecItem(
            id="w_copilot",
            widget_type=WidgetType.COPILOT_QUICK_ACTIONS,
            title="The Harvest Copilot",
            priority_order=order,
            grid_span=2,
        ))

        spec = DashboardSpec(
            schema_version="1.0.0",
            scale=ctx.scale,
            crop=ctx.crop_common_name,
            is_organic=ctx.is_organic,
            theme=theme,
            copy_tone=tone,
            widgets=widgets,
        )

        return PersonaAgentResponse(
            spec=spec,
            reasoning_summary=f"Compiled {len(widgets)} widgets with {tone} tone and {theme.id} theme tokens for {ctx.scale.value}."
        )
