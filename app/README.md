# The Harvest - Mobile Client (Android Jetpack Compose)

## Overview
Offline-first mobile client for **The Harvest (Farmers AI Assistant)**.
The app morphs its dashboard layout, theme, widgets, and copy tone dynamically
based on the server-driven `DashboardSpec` JSON schema, ensuring micro-scale balcony
growers and industrial farm managers experience tailored interfaces driven by one engine.

## Technology Stack
- **UI**: Jetpack Compose, Material 3, dynamic theme engine
- **Local Persistence**: Room Database (SQLite), Flow, Coroutines
- **Networking**: Retrofit, OkHttp, Open-Meteo REST API
- **Cloud Backend**: Firebase Firestore Enterprise DB & Google Sign-In via Credential Manager
- **Architecture**: MVVM with Unidirectional Data Flow (UDF)

## Phase 4 Features
- **Interactive 5-Step Onboarding Wizard** (`OnboardingScreen.kt`):
  1. Operating Scale selection (Micro Pot, 1-Acre Market Garden, 10-Acre Orchard, 500-Acre Industrial)
  2. Primary Crop & Botanical Profile (Common name, scientific genus, annual vs perennial lifecycle)
  3. Geography & WGS84 coordinates with quick regional presets (SF, Willamette Valley, Yakima, Kansas)
  4. Methodology & Soil Profile (Certified Organic toggle with NOP Guardrail activation vs Conventional)
  5. Live Persona & Dashboard Spec compilation
- **Dynamic Dashboard Renderer** (`DynamicDashboardScreen.kt`):
  - Theme tokens automatically inject primary/secondary/surface colors and density modes
  - Server-driven widget priority layout engine
  - Live Open-Meteo telemetry cards (air temperature, volumetric soil moisture %, daily ETc mm, chilling hours)
  - Quick action buttons with instant feedback
- **Copilot Multi-Agent Chat** (`CopilotScreen.kt`):
  - Conversational interface with verified agronomic citations (FAO, UC Davis, WSU, K-State)
  - Active Guardrail interception preventing prohibited synthetic chemicals on organic holdings
