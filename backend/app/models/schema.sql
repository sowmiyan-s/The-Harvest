-- ============================================================================
-- The Harvest (Farmers AI Assistant) - Production Relational & Spatial Schema
-- Engine: PostgreSQL 16+ with PostGIS Extension
-- ============================================================================

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "postgis";

-- ----------------------------------------------------------------------------
-- 1. USERS TABLE
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    default_language VARCHAR(10) NOT NULL DEFAULT 'en', -- e.g. en, es, hi, fr, sw
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- ----------------------------------------------------------------------------
-- 2. CROP PROFILES (Reference agronomy catalog, dynamic not hardcoded)
-- ----------------------------------------------------------------------------
CREATE TYPE crop_lifecycle AS ENUM ('annual', 'perennial', 'biennial');

CREATE TABLE IF NOT EXISTS crop_profiles (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    scientific_name VARCHAR(200) NOT NULL,
    common_name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL, -- cereal, fruit_tree, vegetable, legume, herb
    lifecycle crop_lifecycle NOT NULL DEFAULT 'annual',
    -- Agronomy metrics
    min_temp_celsius NUMERIC(5, 2) NOT NULL DEFAULT 10.0,
    max_temp_celsius NUMERIC(5, 2) NOT NULL DEFAULT 35.0,
    optimal_soil_ph_min NUMERIC(4, 2) NOT NULL DEFAULT 6.0,
    optimal_soil_ph_max NUMERIC(4, 2) NOT NULL DEFAULT 7.5,
    -- ASSUMPTION: Base chilling temperature threshold is 7.2°C (45°F) for temperate perennials
    chilling_hours_required INTEGER DEFAULT 0,
    water_req_mm_per_season NUMERIC(8, 2) NOT NULL DEFAULT 450.0,
    -- N-P-K recommendation per hectare (kg/ha)
    recommended_n_kg_ha NUMERIC(6, 2) NOT NULL DEFAULT 80.0,
    recommended_p_kg_ha NUMERIC(6, 2) NOT NULL DEFAULT 40.0,
    recommended_k_kg_ha NUMERIC(6, 2) NOT NULL DEFAULT 40.0,
    -- JSON metadata for stages: germination, vegetative, flowering, yield, harvest
    growth_stages JSONB NOT NULL DEFAULT '[]'::jsonb,
    source_citation TEXT NOT NULL DEFAULT 'FAO Agronomy Database (2024)',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_crop_profiles_common_name ON crop_profiles(common_name);
CREATE INDEX IF NOT EXISTS idx_crop_profiles_category ON crop_profiles(category);

-- ----------------------------------------------------------------------------
-- 3. FARMS TABLE (Spatial polygon boundary, scale, timezone, organic constraint)
-- ----------------------------------------------------------------------------
CREATE TYPE farm_scale AS ENUM ('micro_pot', 'small_1acre', 'medium_10acre', 'commercial_500acre');

CREATE TABLE IF NOT EXISTS farms (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    scale farm_scale NOT NULL DEFAULT 'small_1acre',
    primary_crop_id UUID REFERENCES crop_profiles(id) ON DELETE SET NULL,
    is_organic BOOLEAN NOT NULL DEFAULT TRUE,
    area_sq_meters NUMERIC(14, 2) NOT NULL,
    -- ASSUMPTION: PostGIS geography polygon with SRID 4326 (WGS84 lat/long)
    boundary GEOGRAPHY(POLYGON, 4326),
    centroid GEOGRAPHY(POINT, 4326) NOT NULL,
    timezone VARCHAR(50) NOT NULL DEFAULT 'UTC',
    preferred_language VARCHAR(10) NOT NULL DEFAULT 'en',
    soil_type VARCHAR(50) DEFAULT 'loam',
    soil_test_results JSONB DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_farms_user_id ON farms(user_id);
CREATE INDEX IF NOT EXISTS idx_farms_spatial_centroid ON farms USING GIST(centroid);
CREATE INDEX IF NOT EXISTS idx_farms_spatial_boundary ON farms USING GIST(boundary);

-- ----------------------------------------------------------------------------
-- 4. OBSERVATIONS TABLE (Time-series weather, soil moisture, NDVI snapshots)
-- ----------------------------------------------------------------------------
CREATE TYPE observation_type AS ENUM ('weather_soil', 'satellite_ndvi', 'soil_sensor', 'manual_scouting');

CREATE TABLE IF NOT EXISTS observations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    obs_type observation_type NOT NULL DEFAULT 'weather_soil',
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    source VARCHAR(80) NOT NULL, -- e.g. 'open-meteo', 'sentinel-2', 'user_photo'
    -- Typed telemetry metrics
    temp_celsius NUMERIC(5, 2),
    humidity_pct NUMERIC(5, 2),
    soil_moisture_pct NUMERIC(5, 2),
    evapotranspiration_mm NUMERIC(6, 2),
    accumulated_chilling_hours INTEGER,
    mean_ndvi NUMERIC(4, 3), -- -1.000 to +1.000
    raw_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_observations_farm_time ON observations(farm_id, timestamp DESC);
CREATE INDEX IF NOT EXISTS idx_observations_type ON observations(obs_type);

-- ----------------------------------------------------------------------------
-- 5. LOG ENTRIES & LEDGER (Watering, compost, chemical, CapEx/OpEx expenses)
-- ----------------------------------------------------------------------------
CREATE TYPE log_category AS ENUM ('watering', 'fertilizer', 'compost', 'pest_control', 'pruning', 'harvest', 'expense_capex', 'expense_opex');

CREATE TABLE IF NOT EXISTS log_entries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    category log_category NOT NULL,
    recorded_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    quantity NUMERIC(10, 3),
    unit VARCHAR(30) NOT NULL, -- 'liters', 'cups', 'kg', 'metric_tons', 'hours'
    cost_amount NUMERIC(12, 2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'USD',
    notes TEXT,
    -- ASSUMPTION: Organic certification audit requires input formula & supplier verification
    input_formula TEXT,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_log_entries_farm_cat ON log_entries(farm_id, category, recorded_at DESC);

-- ----------------------------------------------------------------------------
-- 6. AGENT RUNS (Observability, token audit, latency, cost tracking)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS agent_runs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trace_id VARCHAR(64) NOT NULL,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    farm_id UUID REFERENCES farms(id) ON DELETE SET NULL,
    agent_name VARCHAR(50) NOT NULL, -- orchestrator, persona, weather_soil, crop_rag, inputs, vision, guardrail, memory
    prompt_tokens INTEGER NOT NULL DEFAULT 0,
    completion_tokens INTEGER NOT NULL DEFAULT 0,
    latency_ms INTEGER NOT NULL,
    estimated_cost_usd NUMERIC(8, 6) NOT NULL DEFAULT 0.000000,
    model_version VARCHAR(60) NOT NULL,
    is_fallback_triggered BOOLEAN NOT NULL DEFAULT FALSE,
    guardrail_passed BOOLEAN NOT NULL DEFAULT TRUE,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_agent_runs_trace ON agent_runs(trace_id);
CREATE INDEX IF NOT EXISTS idx_agent_runs_created ON agent_runs(created_at DESC);

-- ----------------------------------------------------------------------------
-- 7. ACHIEVEMENTS & MILESTONES (Gamification for micro/hobbyist & milestones for commercial)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS achievements (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    farm_id UUID NOT NULL REFERENCES farms(id) ON DELETE CASCADE,
    code VARCHAR(60) NOT NULL, -- 'first_sprout', 'compost_master_100kg', 'water_conservation_20pct'
    title VARCHAR(120) NOT NULL,
    description TEXT NOT NULL,
    badge_icon_url VARCHAR(255),
    unlocked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    points INTEGER NOT NULL DEFAULT 10
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_farm_achievement_unique ON achievements(farm_id, code);

-- ----------------------------------------------------------------------------
-- ROW LEVEL SECURITY (RLS)
-- ----------------------------------------------------------------------------
ALTER TABLE farms ENABLE ROW LEVEL SECURITY;
ALTER TABLE observations ENABLE ROW LEVEL SECURITY;
ALTER TABLE log_entries ENABLE ROW LEVEL SECURITY;
ALTER TABLE achievements ENABLE ROW LEVEL SECURITY;

-- ASSUMPTION: Application users authenticate with session token setting app.current_user_id
CREATE POLICY farms_user_isolation ON farms
    FOR ALL
    USING (user_id = NULLIF(current_setting('app.current_user_id', true), '')::UUID);

CREATE POLICY log_entries_user_isolation ON log_entries
    FOR ALL
    USING (farm_id IN (SELECT id FROM farms WHERE user_id = NULLIF(current_setting('app.current_user_id', true), '')::UUID));
