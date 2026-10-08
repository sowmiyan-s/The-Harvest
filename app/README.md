# The Harvest - Mobile Client (Android Jetpack Compose)

## Overview
Offline-first mobile client for **The Harvest (Farmers AI Assistant)**.
The app morphs its dashboard layout, theme, widgets, and copy tone dynamically
based on the server-driven `DashboardSpec` JSON schema, ensuring micro-scale balcony
growers and industrial farm managers experience tailored interfaces driven by one engine.

## Technology Stack
- **UI**: Jetpack Compose, Material 3, dynamic theme engine
- **Local Persistence**: Room Database (SQLite), Flow, Coroutines
- **Networking**: Retrofit, Moshi/Serialization, OkHttp
- **Architecture**: MVVM with Unidirectional Data Flow (UDF)
- **Offline Capability**: Local caching of Farm Profiles, Dashboard Specs, Observations, and Ledger Logs
