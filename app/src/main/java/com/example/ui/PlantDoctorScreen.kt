package com.example.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.PlantDiagnosisResult

@Composable
fun PlantDoctorScreen(
    state: HarvestUiState,
    viewModel: HarvestViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = state.dashboardSpec.theme.darkMode
    val surfaceColor = if (isDark) Color(0xFF1E1E1E) else Color.White
    val onSurfaceColor = if (isDark) Color(0xFFECEFF1) else Color(0xFF1C1B1F)

    // Image Picker using zero-permission Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bitmap = BitmapFactory.decodeStream(stream)
                    if (bitmap != null) {
                        viewModel.setSelectedPlantBitmap(bitmap, "User Uploaded Specimen")
                    }
                }
            } catch (e: Exception) {
                // Ignore load error
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("plant_doctor_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("plant_doctor_hero_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Filled.DocumentScanner,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Plant Doctor (Vision AI)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = onSurfaceColor
                                )
                                Text(
                                    text = "Multimodal Pathology Diagnostic Engine",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = onSurfaceColor.copy(alpha = 0.7f)
                                )
                            }
                        }

                        AssistChip(
                            onClick = {},
                            label = { Text("Gemini 3.1 Pro", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            leadingIcon = {
                                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFFE8F5E9),
                                labelColor = Color(0xFF1B5E20),
                                leadingIconContentColor = Color(0xFF1B5E20)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Upload a leaf or plant photo to identify diseases, nutrient deficiencies, or pest infestations with confidence scoring, organic remedies, and expert extension validation.",
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceColor.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Photo Upload & Sample Specimens Selector
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_photo_selector"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Select or Capture Plant Photo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_upload_photo")
                        ) {
                            Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Upload Photo", fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = {
                                val sampleBitmap = createPlantSampleBitmap("Tomato Late Blight")
                                viewModel.setSelectedPlantBitmap(sampleBitmap, "Tomato")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_sample_photo")
                        ) {
                            Icon(Icons.Filled.Science, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load Specimen", fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Preset Agronomy Leaf Specimens:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = onSurfaceColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val samples = listOf(
                            "Tomato" to "Late Blight",
                            "Corn" to "Nitrogen Def.",
                            "Apple" to "Apple Scab",
                            "Squash" to "Powdery Mildew",
                            "Basil" to "Healthy Canopy"
                        )
                        samples.forEach { (crop, disease) ->
                            FilterChip(
                                selected = state.selectedSpecimenName.contains(crop),
                                onClick = {
                                    val bmp = createPlantSampleBitmap("$crop $disease")
                                    viewModel.setSelectedPlantBitmap(bmp, crop)
                                },
                                label = { Text("$crop ($disease)", fontSize = 11.sp) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Nature, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            )
                        }
                    }

                    // Display active preview image if available
                    if (state.selectedPlantBitmap != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1B4D3E).copy(alpha = 0.1f))
                                .border(1.dp, Color(0xFF2E7D32).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = state.selectedPlantBitmap!!.asImageBitmap(),
                                contentDescription = "Active Plant Specimen",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            Surface(
                                color = Color.Black.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = state.selectedSpecimenName,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { viewModel.diagnoseSelectedPlant() },
                            enabled = !state.isDiagnosing,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_run_diagnosis")
                        ) {
                            if (state.isDiagnosing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyzing with Gemini 3.1 Pro Preview...")
                            } else {
                                Icon(Icons.Filled.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Analyze Image with Gemini 3.1 Pro", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Diagnosis Results Card
        if (state.plantDiagnosisResult != null) {
            val result = state.plantDiagnosisResult!!
            item {
                DiagnosisResultCard(
                    result = result,
                    surfaceColor = surfaceColor,
                    onSurfaceColor = onSurfaceColor
                )
            }
        }
    }
}

@Composable
fun DiagnosisResultCard(
    result: PlantDiagnosisResult,
    surfaceColor: Color,
    onSurfaceColor: Color
) {
    val confidencePct = (result.confidenceScore * 100).toInt()
    val isHighConfidence = result.confidenceScore >= 0.85

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("card_diagnosis_result"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Crop & Condition
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.condition,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (result.isExpertConsultRecommended) Color(0xFFE65100) else Color(0xFF2E7D32)
                    )
                    Text(
                        text = result.cropIdentified,
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceColor.copy(alpha = 0.7f)
                    )
                }

                Surface(
                    color = if (isHighConfidence) Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isHighConfidence) Icons.Filled.CheckCircle else Icons.Filled.Warning,
                            contentDescription = null,
                            tint = if (isHighConfidence) Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$confidencePct% Conf.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHighConfidence) Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Engine provenance badge
            Surface(
                color = Color(0xFFF1F8E9),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Engine: ${result.sourceEngine}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF33691E),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Symptoms Observed
            Text(
                text = "Symptoms Identified:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            result.symptomsObserved.forEach { symptom ->
                Row(modifier = Modifier.padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Filled.FiberManualRecord,
                        contentDescription = null,
                        modifier = Modifier
                            .size(10.dp)
                            .padding(top = 4.dp),
                        tint = Color(0xFF2E7D32)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = symptom, fontSize = 12.sp, color = onSurfaceColor.copy(alpha = 0.85f))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Organic Remedy Box
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFE8F5E9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Eco, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("100% Organic Treatment (OMRI Compliant):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = result.organicRemedy, fontSize = 12.sp, color = Color(0xFF1B5E20))
                }
            }

            // Chemical Remedy Box if applicable
            if (result.chemicalRemedy != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEDE7F6),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Science, contentDescription = null, tint = Color(0xFF512DA8), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Conventional Chemical Alternative:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF512DA8))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = result.chemicalRemedy, fontSize = 12.sp, color = Color(0xFF311B92))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Safety Advisory
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFF3E0),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.safetyAdvisory,
                        fontSize = 11.sp,
                        color = Color(0xFFBF360C)
                    )
                }
            }

            // Expert Fallback Card
            if (result.isExpertConsultRecommended) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFEBEE),
                    border = CardDefaults.outlinedCardBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ContactPhone, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Expert Consultation Recommended", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFC62828))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Due to symptom ambiguity or critical pathogen risk, verified university extension diagnostics are advised before chemical intervention.",
                            fontSize = 11.sp,
                            color = Color(0xFFB71C1C)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "📞 University Extension Hotline: 1-800-555-AGRI\n📍 Regional Plant Pathology Diagnostic Lab",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF880E4F)
                        )
                    }
                }
            }
        }
    }
}

// Generates a visual simulated plant specimen leaf bitmap for offline/instant evaluation
private fun createPlantSampleBitmap(title: String): Bitmap {
    val width = 400
    val height = 240
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Background leaf green gradient
    val paint = Paint().apply { isAntiAlias = true }
    paint.color = android.graphics.Color.rgb(46, 125, 50)
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

    // Draw central vein
    paint.color = android.graphics.Color.rgb(129, 199, 132)
    paint.strokeWidth = 6f
    canvas.drawLine(20f, height / 2f, (width - 20).toFloat(), height / 2f, paint)

    // Draw secondary veins
    paint.strokeWidth = 3f
    for (i in 60 until width - 40 step 50) {
        canvas.drawLine(i.toFloat(), height / 2f, (i + 30).toFloat(), 30f, paint)
        canvas.drawLine(i.toFloat(), height / 2f, (i + 30).toFloat(), (height - 30).toFloat(), paint)
    }

    // Draw symptom lesions if diseased
    if (title.contains("Blight", ignoreCase = true)) {
        paint.color = android.graphics.Color.rgb(62, 39, 35) // Brown necrotic lesion
        canvas.drawCircle(150f, 90f, 35f, paint)
        canvas.drawCircle(260f, 150f, 25f, paint)
        paint.color = android.graphics.Color.rgb(255, 235, 59) // Chlorotic yellow halo
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        canvas.drawCircle(150f, 90f, 40f, paint)
    } else if (title.contains("Nitrogen", ignoreCase = true)) {
        paint.color = android.graphics.Color.rgb(253, 216, 53) // V-shaped chlorosis
        paint.style = Paint.Style.FILL
        canvas.drawCircle(width - 80f, height / 2f, 50f, paint)
    } else if (title.contains("Mildew", ignoreCase = true)) {
        paint.color = android.graphics.Color.rgb(245, 245, 245) // White powdery patches
        paint.style = Paint.Style.FILL
        canvas.drawCircle(120f, 80f, 25f, paint)
        canvas.drawCircle(220f, 160f, 30f, paint)
        canvas.drawCircle(300f, 90f, 20f, paint)
    }

    // Title label
    paint.style = Paint.Style.FILL
    paint.color = android.graphics.Color.WHITE
    paint.textSize = 22f
    paint.isFakeBoldText = true
    canvas.drawText(title, 24f, height - 20f, paint)

    return bitmap
}
