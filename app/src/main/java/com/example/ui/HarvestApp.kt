package com.example.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.firebase.FirebaseHarvestService
import com.example.data.model.FarmScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HarvestApp(
    viewModel: HarvestViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isDark = state.dashboardSpec.theme.darkMode
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isSigningIn by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    // Dynamic root colors from active ThemeSpec
    val rootBgColor = if (isDark) Color(0xFF121212) else Color(0xFFF8F9FA)
    val barColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFF1B4D3E)
    val onBarColor = Color.White

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(rootBgColor),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, Color(0xFFFFD54F), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.harvest_logo_premium_1791454573312),
                                contentDescription = "The Harvest Emblem",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "The Harvest",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = onBarColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFF22C55E).copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = "● LIVE",
                                        color = Color(0xFF4ADE80),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (state.firebaseUser != null) {
                                    "Connected • ${state.firebaseUser?.displayName ?: "Farmer"}"
                                } else {
                                    "${state.selectedFarm.name} • ${state.selectedFarm.scale.displayName}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = onBarColor.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    // Live Telemetry Refresh Button
                    IconButton(
                        onClick = { viewModel.refreshLiveTelemetry() },
                        modifier = Modifier.testTag("btn_refresh_live_telemetry")
                    ) {
                        if (state.isWeatherLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFFFFD54F)
                            )
                        } else {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "Refresh Realtime Satellite Feed",
                                tint = Color(0xFFFFD54F)
                            )
                        }
                    }

                    // Google Sign In & Sync
                    if (state.firebaseUser != null) {
                        IconButton(
                            onClick = { viewModel.syncCurrentFarmToFirebase(context) },
                            modifier = Modifier.testTag("btn_sync_firestore")
                        ) {
                            Icon(
                                Icons.Filled.CloudSync,
                                contentDescription = "Sync to Firestore",
                                tint = Color(0xFFFFD54F)
                            )
                        }
                        IconButton(
                            onClick = {
                                FirebaseHarvestService.signOut(context, {}, coroutineScope)
                            },
                            modifier = Modifier.testTag("btn_sign_out")
                        ) {
                            Icon(
                                Icons.Filled.Logout,
                                contentDescription = "Sign Out",
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                isSigningIn = true
                                val activity = context as? Activity
                                if (activity != null) {
                                    FirebaseHarvestService.signInWithGoogle(
                                        activity = activity,
                                        onSuccess = { isSigningIn = false },
                                        onError = { isSigningIn = false },
                                        onCancelled = { isSigningIn = false },
                                        scope = coroutineScope
                                    )
                                } else {
                                    isSigningIn = false
                                }
                            },
                            enabled = !isSigningIn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .testTag("btn_google_signin")
                        ) {
                            if (isSigningIn) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    Icons.Filled.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sign in", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Architecture & Data inspector overflow menu
                    Box {
                        IconButton(
                            onClick = { showMoreMenu = true },
                            modifier = Modifier.testTag("btn_more_menu")
                        ) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "More tools",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Spray Window & Drift Risk") },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.selectTab(AppViewTab.SPRAY_ADVISORY)
                                },
                                leadingIcon = { Icon(Icons.Filled.Air, contentDescription = null, tint = Color(0xFF0288D1)) }
                            )
                            DropdownMenuItem(
                                text = { Text("Field Scouting Observations") },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.selectTab(AppViewTab.FIELD_SCOUT)
                                },
                                leadingIcon = { Icon(Icons.Filled.AssignmentTurnedIn, contentDescription = null, tint = Color(0xFF2E7D32)) }
                            )
                            DropdownMenuItem(
                                text = { Text("Crop Rotation Planner") },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.selectTab(AppViewTab.CROP_ROTATION)
                                },
                                leadingIcon = { Icon(Icons.Filled.Autorenew, contentDescription = null, tint = Color(0xFF388E3C)) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("8 Agents Topology") },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.selectTab(AppViewTab.AGENT_TOPOLOGY)
                                },
                                leadingIcon = { Icon(Icons.Filled.Hub, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Data & DB Schemas") },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.selectTab(AppViewTab.DATA_MODELS)
                                },
                                leadingIcon = { Icon(Icons.Filled.Storage, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Server JSON Spec") },
                                onClick = {
                                    showMoreMenu = false
                                    viewModel.selectTab(AppViewTab.SCHEMA_SPEC)
                                },
                                leadingIcon = { Icon(Icons.Filled.Code, contentDescription = null) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = barColor,
                    titleContentColor = onBarColor
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = if (isDark) Color(0xFF1E1E1E) else Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = state.currentTab == AppViewTab.DYNAMIC_DASHBOARD,
                    onClick = { viewModel.selectTab(AppViewTab.DYNAMIC_DASHBOARD) },
                    icon = { Icon(Icons.Filled.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 9.sp) },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    selected = state.currentTab == AppViewTab.COPILOT_CHAT,
                    onClick = { viewModel.selectTab(AppViewTab.COPILOT_CHAT) },
                    icon = { Icon(Icons.Filled.AutoAwesome, contentDescription = "Copilot AI") },
                    label = { Text("Copilot", fontSize = 9.sp) },
                    modifier = Modifier.testTag("nav_tab_copilot")
                )
                NavigationBarItem(
                    selected = state.currentTab == AppViewTab.PLANT_DOCTOR,
                    onClick = { viewModel.selectTab(AppViewTab.PLANT_DOCTOR) },
                    icon = { Icon(Icons.Filled.DocumentScanner, contentDescription = "Plant Doctor") },
                    label = { Text("Vision AI", fontSize = 9.sp) },
                    modifier = Modifier.testTag("nav_tab_plant_doctor")
                )
                NavigationBarItem(
                    selected = state.currentTab == AppViewTab.FIELD_MAP,
                    onClick = { viewModel.selectTab(AppViewTab.FIELD_MAP) },
                    icon = { Icon(Icons.Filled.SatelliteAlt, contentDescription = "Field Map") },
                    label = { Text("Field & NDVI", fontSize = 9.sp) },
                    modifier = Modifier.testTag("nav_tab_field_map")
                )
                NavigationBarItem(
                    selected = state.currentTab == AppViewTab.INPUTS_LEDGER,
                    onClick = { viewModel.selectTab(AppViewTab.INPUTS_LEDGER) },
                    icon = { Icon(Icons.Filled.Calculate, contentDescription = "Calculators") },
                    label = { Text("Calculators", fontSize = 9.sp) },
                    modifier = Modifier.testTag("nav_tab_calculators")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(rootBgColor)
        ) {
            // Cloud Sync Status Message Banner if present
            if (state.cloudSyncMessage != null) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CloudDone,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.cloudSyncMessage ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF1B5E20),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Farm Scale Preset Selector Bar (Demonstrating Instant Morphing Engine)
            Surface(
                color = if (isDark) Color(0xFF242424) else Color(0xFFE8ECE9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.allFarms.forEach { farm ->
                        val isSelected = farm.id == state.selectedFarm.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectFarm(farm) },
                            label = {
                                Text(
                                    text = farm.scale.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    when (farm.scale) {
                                        FarmScale.MICRO_POT -> Icons.Filled.LocalFlorist
                                        FarmScale.SMALL_1ACRE -> Icons.Filled.Yard
                                        FarmScale.MEDIUM_10ACRE -> Icons.Filled.Park
                                        FarmScale.COMMERCIAL_500ACRE -> Icons.Filled.Agriculture
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (isDark) Color(0xFF4CAF50) else Color(0xFF1B4D3E),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color(0xFFFFD54F)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("chip_farm_${farm.id}")
                        )
                    }

                    // Add New Farm Button to launch Onboarding Wizard
                    AssistChip(
                        onClick = { viewModel.openOnboarding() },
                        label = { Text("+ New Farm", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        leadingIcon = {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color(0xFF2E7D32),
                            labelColor = Color.White,
                            leadingIconContentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.testTag("btn_launch_onboarding")
                    )
                }
            }

            // Main View Content
            if (state.showOnboarding) {
                OnboardingScreen(
                    onDismiss = { viewModel.closeOnboarding() },
                    onComplete = { viewModel.addNewFarm(it) }
                )
            } else {
                when (state.currentTab) {
                    AppViewTab.DYNAMIC_DASHBOARD -> DynamicDashboardScreen(
                        state = state,
                        viewModel = viewModel
                    )
                    AppViewTab.COPILOT_CHAT -> {
                        androidx.activity.compose.BackHandler {
                            viewModel.selectTab(AppViewTab.DYNAMIC_DASHBOARD)
                        }
                        CopilotScreen(
                            state = state,
                            messages = state.copilotMessages,
                            onSendMessage = { viewModel.sendCopilotMessage(it) }
                        )
                    }
                    AppViewTab.PLANT_DOCTOR -> {
                        androidx.activity.compose.BackHandler {
                            viewModel.selectTab(AppViewTab.DYNAMIC_DASHBOARD)
                        }
                        PlantDoctorScreen(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                    AppViewTab.FIELD_MAP -> {
                        androidx.activity.compose.BackHandler {
                            viewModel.selectTab(AppViewTab.DYNAMIC_DASHBOARD)
                        }
                        FieldMapScreen(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                    AppViewTab.SPRAY_ADVISORY -> {
                        androidx.activity.compose.BackHandler {
                            viewModel.selectTab(AppViewTab.DYNAMIC_DASHBOARD)
                        }
                        SprayAdvisoryScreen(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                    AppViewTab.FIELD_SCOUT -> {
                        androidx.activity.compose.BackHandler {
                            viewModel.selectTab(AppViewTab.DYNAMIC_DASHBOARD)
                        }
                        FieldScoutScreen(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                    AppViewTab.CROP_ROTATION -> {
                        androidx.activity.compose.BackHandler {
                            viewModel.selectTab(AppViewTab.DYNAMIC_DASHBOARD)
                        }
                        CropRotationScreen(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                    AppViewTab.INPUTS_LEDGER -> {
                        androidx.activity.compose.BackHandler {
                            viewModel.selectTab(AppViewTab.DYNAMIC_DASHBOARD)
                        }
                        InputsLedgerScreen(
                            state = state,
                            viewModel = viewModel
                        )
                    }
                    AppViewTab.AGENT_TOPOLOGY -> {
                        androidx.activity.compose.BackHandler {
                            viewModel.selectTab(AppViewTab.DYNAMIC_DASHBOARD)
                        }
                        AgentTopologyScreen(
                            agents = state.allAgents
                        )
                    }
                    AppViewTab.DATA_MODELS -> {
                        androidx.activity.compose.BackHandler {
                            viewModel.selectTab(AppViewTab.DYNAMIC_DASHBOARD)
                        }
                        DataModelsScreen()
                    }
                    AppViewTab.SCHEMA_SPEC -> {
                        androidx.activity.compose.BackHandler {
                            viewModel.selectTab(AppViewTab.DYNAMIC_DASHBOARD)
                        }
                        SchemaSpecScreen(
                            spec = state.dashboardSpec
                        )
                    }
                }
            }
        }
    }
}
