"""
Integration & Contract Tests for The Harvest Multi-Agent Backend Service.
Validates inputs & cost mathematical formulas, satellite vision diagnosis,
safety guardrails, and memory GDPR deletion.
"""

import pytest
from httpx import AsyncClient, ASGITransport
from backend.app.main import app
from backend.app.schemas.agent_contracts import (
    FarmScale,
    InputCalcType,
)


@pytest.mark.asyncio
async def test_health_check():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        response = await ac.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "healthy"


@pytest.mark.asyncio
async def test_inputs_cost_water_calculator():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        payload = {
            "calc_type": "water_volume",
            "area_acres": 10.0,
            "crop_name": "Apples",
            "is_organic": True
        }
        res = await ac.post("/api/v1/agents/inputs-cost", json=payload)
    assert res.status_code == 200
    data = res.json()
    assert data["unit"] == "gallons"
    assert data["recommended_quantity"] > 0
    # Verifies labeled assumption disclaimer is present
    assert "ASSUMPTION" in data["assumptions"]["labeled_disclaimer"]


@pytest.mark.asyncio
async def test_satellite_vision_diagnosis_and_expert_fallback():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        # Known condition test
        res_tomato = await ac.post("/api/v1/agents/vision-diagnosis", json={
            "image_base64": "dummy_base64",
            "crop_suspected": "Tomato"
        })
        assert res_tomato.status_code == 200
        data_tomato = res_tomato.json()
        assert "Late Blight" in data_tomato["condition"]
        assert data_tomato["confidence_score"] >= 0.85

        # Unknown condition test -> triggers expert fallback
        res_unknown = await ac.post("/api/v1/agents/vision-diagnosis", json={
            "image_base64": "dummy_base64",
            "crop_suspected": "Exotic Unknown Plant"
        })
        assert res_unknown.status_code == 200
        data_unknown = res_unknown.json()
        assert data_unknown["is_expert_consult_recommended"] is True
        assert data_unknown["confidence_score"] < 0.70


@pytest.mark.asyncio
async def test_memory_agent_gdpr_export_and_delete():
    async with AsyncClient(transport=ASGITransport(app=app), base_url="http://test") as ac:
        user_id = "test_farmer_99"
        # 1. Store
        await ac.post("/api/v1/agents/memory/store", json={
            "user_id": user_id,
            "farm_id": "farm_01",
            "key": "pref_theme",
            "value_json": {"dark_mode": True}
        })

        # 2. Export
        export_res = await ac.get(f"/api/v1/agents/memory/export/{user_id}")
        assert export_res.status_code == 200
        export_data = export_res.json()
        assert export_data["user_id"] == user_id
        assert export_data["observation_records"] >= 1

        # 3. Delete
        delete_res = await ac.delete(f"/api/v1/agents/memory/{user_id}")
        assert delete_res.status_code == 200
        assert delete_res.json()["status"] == "purged"
