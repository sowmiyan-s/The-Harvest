"""
Dashboard Spec definitions for dynamically morphing client interfaces.
Driven by Farm Profile parameters (Scale, Crop, Organic, Language).
"""

from typing import List, Dict, Any, Optional
from pydantic import BaseModel, Field
from .agent_contracts import FarmScale, WidgetType, ThemeSpec, WidgetSpecItem


class QuickActionItem(BaseModel):
    id: str
    label: str
    icon_name: str
    action_type: str # 'navigate_camera', 'open_calculator', 'log_watering', 'ask_copilot'
    payload: Dict[str, Any] = Field(default_factory=dict)


class DynamicDashboardSpec(BaseModel):
    schema_version: str = Field("1.0.0", description="Semver of dashboard schema")
    scale: FarmScale
    crop: str
    lifecycle: str # 'annual', 'perennial'
    is_organic: bool
    language: str
    copy_tone: str # 'playful_educational', 'practical_artisan', 'commercial_agronomic'
    theme: ThemeSpec
    quick_actions: List[QuickActionItem]
    widgets: List[WidgetSpecItem]
    metadata: Dict[str, Any] = Field(default_factory=dict)
