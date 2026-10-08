# The Harvest (Farmers AI Assistant) 🌱🚜

**The Harvest** is an intelligent, multi-agent agronomy platform and modern Android application designed for farmers across every agricultural scale—from micro urban hydroponic growers to industrial mega-acreage producers. It bridges dynamic agronomic science, real-time weather telemetrics, satellite NDVI vegetation indices, Gemini Vision plant diagnosis, and tailored user personas (from 10-year-old curious learners to commercial farm managers).

---

## 🌟 Key Highlights & Architecture

### 1. 8 Specialized Agronomy Agents Ecosystem
- **Orchestrator Agent**: Contextual query dispatcher, persona synthesis, multi-lingual tone adapter.
- **Persona UI Agent**: Adapts tone, layout density, and complexity based on farmer profile (Beginner, Experienced, Industrial, Student).
- **Weather & Soil Agent**: Integrates open agrometeorological data (precipitation, VPD, GDD, evapotranspiration) and soil characteristics.
- **Crop Knowledge Agent**: Growth stages, companion planting, biological pest management, and deficit irrigation strategies.
- **Inputs & Cost Agent**: Mathematical farm calculators (Acre-inch water volume, compost mineralization, N-P-K chemical vs. organic budgets).
- **Satellite & Vision Agent**: Sentinel-2 multispectral NDVI processing and Gemini 3.1 Pro Preview plant pathology diagnosis.
- **Safety & Guardrail Agent**: EPA/WHO chemical dosage verification, strict banned substance blocks, and mandatory agronomist consultation prompts.
- **Memory Agent**: Zero-knowledge multi-tenant farm history, seasonal snapshot exports, and strict GDPR right-to-erasure compliance.

### 2. Android Client (Jetpack Compose & Material Design 3)
- **Dynamic Dashboard**: Schema-driven widget engine rendering telemetry, live weather forecasts, nitrogen meters, and action advisories.
- **Plant Doctor (Vision AI)**: Zero-permission photo picker and sample specimen selector analyzed by **Gemini 3.1 Pro Preview** (with fallback local pathology matrix).
- **Field Map & Satellite NDVI**: Interactive polygon canvas mapping field coordinates, zone health distributions, and infrared reflection scales.
- **Inputs & Calculators Ledger**: Live mathematical engines with explicit agronomical assumptions and CapEx/OpEx financial tables.
- **Copilot Multi-Agent Chat**: Real-time conversation with agronomy agents supporting multi-persona switching and voice/text assistance.
- **GDPR & Farm Profile Settings**: Farm scale switcher, multi-lingual localization, JSON data export, and complete data wipe.

---

## 📱 How to Run in Android Studio

Follow these steps to open, build, and run **The Harvest** in Android Studio:

### Prerequisites
1. **Android Studio**: Install the latest stable version of Android Studio (Koala, Ladybug, or Meerkat / Android Studio 2024.1+).
2. **Java Development Kit (JDK)**: JDK 17 or JDK 21 (configured as the Gradle JDK).
3. **Android SDK**:
   - `compileSdk`: **35**
   - `minSdk`: **26** (Android 8.0 Oreo or higher)
   - `targetSdk`: **35**

---

### Step-by-Step Instructions

#### Step 1: Open the Project in Android Studio
1. Launch **Android Studio**.
2. Select **Open** (or `File` > `Open...`).
3. Navigate to the root directory of this repository and click **OK**.
4. Allow Android Studio to sync the Gradle build files automatically. If prompted, trust the project.

#### Step 2: Configure Gradle JDK
1. Navigate to **Settings / Preferences** (`Ctrl + Alt + S` on Windows/Linux or `Cmd + ,` on macOS).
2. Go to **Build, Execution, Deployment** > **Build Tools** > **Gradle**.
3. Under **Gradle JDK**, ensure **JDK 17** or **JDK 21** is selected.
4. Click **Apply** and **OK**.

#### Step 3: Configure Environment Variables (Optional / Recommended)
To enable live Gemini Vision plant pathology analysis:
1. In the root directory, locate `.env.example`.
2. Create a `.env` file or provide credentials in Android Studio's **Run/Debug Configurations** or AI Studio Secrets panel:
   ```env
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
   *(Note: The app contains built-in offline agronomy fallbacks with sample specimens if no API key is provided).*

#### Step 4: Run the App on an Emulator or Physical Device
1. In the toolbar, select an Android Virtual Device (AVD with API 30+ recommended) or connect a physical Android device with **USB Debugging** enabled.
2. Ensure the run configuration is set to **`app`**.
3. Click the green **Run ▶** button (or press `Shift + F10`).
4. Android Studio will assemble the debug APK and launch **The Harvest** directly on the device.

#### Step 5: Running Unit & Robolectric Tests
To run unit and architecture tests in Android Studio:
- Open the **Terminal** tab in Android Studio and run:
  ```bash
  gradle :app:testDebugUnitTest
  ```
- Or right-click the `app/src/test/java` directory in the Project pane and choose **Run 'Tests in 'test''**.

---

## 🐍 Backend Architecture (FastAPI Service)

The companion Python service located in `/backend` coordinates agent workflows and provides automated agronomical tests:

### Structure
```
backend/
├── app/
│   ├── agents/
│   │   ├── orchestrator_agent.py
│   │   ├── persona_ui_agent.py
│   │   ├── weather_soil_agent.py
│   │   ├── crop_knowledge_agent.py
│   │   ├── inputs_cost_agent.py
│   │   ├── satellite_vision_agent.py
│   │   ├── guardrail_agent.py
│   │   └── memory_agent.py
│   ├── api/v1/
│   │   └── agents.py
│   ├── schemas/
│   │   └── agent_contracts.py
│   └── main.py
└── tests/
    └── test_agents.py
```

### Running Backend Tests
If testing the backend independently with Python 3.10+:
```bash
cd backend
pip install -r requirements.txt
pytest tests/
```

---

## 🔒 Security, Compliance & Assumptions
- **Strict Agrometeorological Guardrails**: Pesticide recommendations enforce mandatory PPE checks, reentry intervals (REI), and human toxicity warnings.
- **Zero Hallucination Tolerance**: If visual or spectral confidence falls below 70%, users are directed to local agricultural extension offices.
- **GDPR Compliance**: The Memory Agent provides deterministic data export and hard data erasure functions directly accessible from the app bar.

---

*Built with Jetpack Compose, Kotlin Coroutines & Flow, Material 3, and Gemini Vision AI.*
