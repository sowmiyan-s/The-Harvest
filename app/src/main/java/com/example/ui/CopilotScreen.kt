package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.AgronomyAdviceResult

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val agronomyResult: AgronomyAdviceResult? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun CopilotScreen(
    state: HarvestUiState,
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val isDark = state.dashboardSpec.theme.darkMode

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("copilot_screen")
    ) {
        // Quick Question Chips
        Surface(
            color = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF1F8E9),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Text(
                    text = "Quick Multi-Agent Queries:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickQueries = listOf(
                        "How much water does my crop need?",
                        "Can I spray glyphosate herbicide?",
                        "What are winter chilling requirements?",
                        "How do I prevent pest damage organically?"
                    )
                    quickQueries.forEach { q ->
                        SuggestionChip(
                            onClick = { onSendMessage(q) },
                            label = { Text(q, fontSize = 11.sp) },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (isDark) Color(0xFF2C2C2C) else Color.White
                            )
                        )
                    }
                }
            }
        }

        // Message Thread
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                if (msg.isUser) {
                    UserMessageBubble(text = msg.text, isDark = isDark)
                } else {
                    AgentMessageBubble(
                        text = msg.text,
                        result = msg.agronomyResult,
                        toneTitle = state.dashboardSpec.copyTone.title,
                        isDark = isDark
                    )
                }
            }
        }

        // Message Input Row
        Surface(
            tonalElevation = 4.dp,
            color = if (isDark) Color(0xFF1E1E1E) else Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Ask The Harvest Copilot...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_copilot_query"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText.trim())
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .background(Color(0xFF2E7D32), RoundedCornerShape(20.dp))
                        .size(44.dp)
                        .testTag("btn_send_copilot")
                ) {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun UserMessageBubble(text: String, isDark: Boolean) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Surface(
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            color = if (isDark) Color(0xFF2E7D32) else Color(0xFFE8F5E9),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) Color.White else Color(0xFF1B5E20)
            )
        }
    }
}

@Composable
fun AgentMessageBubble(
    text: String,
    result: AgronomyAdviceResult?,
    toneTitle: String,
    isDark: Boolean
) {
    val isBlocked = result?.guardrailSafe == false

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Card(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isBlocked) {
                    if (isDark) Color(0xFF3E1C1C) else Color(0xFFFFEBEE)
                } else {
                    if (isDark) Color(0xFF242424) else Color.White
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                // Header: Multi-Agent Dispatch Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isBlocked) Icons.Filled.Warning else Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = if (isBlocked) Color(0xFFC62828) else Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBlocked) "Guardrail Intercepted" else "Orchestrator Synthesized",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isBlocked) Color(0xFFC62828) else Color(0xFF2E7D32)
                        )
                    }

                    Text(
                        text = "Tone: $toneTitle",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isBlocked) Color(0xFFB71C1C) else MaterialTheme.colorScheme.onSurface
                )

                if (result != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    // Agronomic Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "pH: ${result.optimalPhRange}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Water: ${result.waterNeedsMmWeek}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📚 Source Citation: ${result.sourceCitation}",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF558B2F)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Latency: ${result.latencyMs}ms",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "Audit: ${result.tokensAudit} tokens",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
