package com.example.ui

import android.app.Activity
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
                        Icon(
                            Icons.Filled.Spa,
                            contentDescription = "The Harvest Logo",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "The Harvest",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = onBarColor
                            )
                            Text(
                                text = if (state.firebaseUser != null) {
                                    "Connected • ${state.firebaseUser?.displayName ?: "Farmer"}"
                                } else {
                                    "Farmers AI • Cloud Ready"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = onBarColor.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
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
                                .padding(end = 8.dp)
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
                    label = { Text("Dashboard", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    selected = state.currentTab == AppViewTab.AGENT_TOPOLOGY,
                    onClick = { viewModel.selectTab(AppViewTab.AGENT_TOPOLOGY) },
                    icon = { Icon(Icons.Filled.Hub, contentDescription = "8 Agents") },
                    label = { Text("8 Agents", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_topology")
                )
                NavigationBarItem(
                    selected = state.currentTab == AppViewTab.DATA_MODELS,
                    onClick = { viewModel.selectTab(AppViewTab.DATA_MODELS) },
                    icon = { Icon(Icons.Filled.Storage, contentDescription = "Data Models") },
                    label = { Text("Data & DB", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_data_models")
                )
                NavigationBarItem(
                    selected = state.currentTab == AppViewTab.SCHEMA_SPEC,
                    onClick = { viewModel.selectTab(AppViewTab.SCHEMA_SPEC) },
                    icon = { Icon(Icons.Filled.Code, contentDescription = "JSON Schema") },
                    label = { Text("JSON Spec", fontSize = 10.sp) },
                    modifier = Modifier.testTag("nav_tab_schema_spec")
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
                }
            }

            // Main View Content
            when (state.currentTab) {
                AppViewTab.DYNAMIC_DASHBOARD -> DynamicDashboardScreen(
                    state = state,
                    viewModel = viewModel
                )
                AppViewTab.AGENT_TOPOLOGY -> AgentTopologyScreen(
                    agents = state.allAgents
                )
                AppViewTab.DATA_MODELS -> DataModelsScreen()
                AppViewTab.SCHEMA_SPEC -> SchemaSpecScreen(
                    spec = state.dashboardSpec
                )
            }
        }
    }
}
