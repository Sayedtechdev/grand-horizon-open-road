package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import kotlin.math.roundToInt
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.StepStage

@Composable
fun StepProgressionDialog(
    currentStage: StepStage,
    step1Completed: Boolean,
    step2Completed: Boolean,
    currentTripKm: Float,
    onSelectStage: (StepStage) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val flyingCarDrawableId = context.resources.getIdentifier("img_flying_car", "drawable", context.packageName)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, Color(0xFF334155), RoundedCornerShape(24.dp)),
            color = Color(0xFF0F172A)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Journey Stages (Steps 1, 2, 3)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Complete Step 1 & 2 to unlock the Flying Car in Step 3!",
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_stages_dialog_btn")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // FLYING CAR TEASER / STATUS BANNER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(95.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.5.dp, if (step1Completed && step2Completed) Color(0xFF00E5FF) else Color(0xFF334155), RoundedCornerShape(16.dp))
                ) {
                    if (flyingCarDrawableId != 0) {
                        Image(
                            painter = painterResource(id = flyingCarDrawableId),
                            contentDescription = "AeroDrive Flying Car",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(95.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF030712).copy(alpha = 0.85f), Color.Transparent)
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AirplanemodeActive, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("FLYING CAR (AERODRIVE)", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color.White)
                            }
                            Text(
                                if (step1Completed && step2Completed) "UNLOCKED! Available in Step 3" else "Locked: Complete Steps 1 & 2 first",
                                fontSize = 11.sp,
                                color = if (step1Completed && step2Completed) Color(0xFF34D399) else Color(0xFFFCD34D),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (step1Completed && step2Completed) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF00E5FF))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("READY", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // STAGE 1 CARD
                StageItemCard(
                    stage = StepStage.STEP_1,
                    isSelected = currentStage == StepStage.STEP_1,
                    isUnlocked = true,
                    isCompleted = step1Completed,
                    currentTripKm = if (currentStage == StepStage.STEP_1) currentTripKm else (if (step1Completed) 12f else 0f),
                    onSelect = {
                        onSelectStage(StepStage.STEP_1)
                        onDismiss()
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // STAGE 2 CARD
                StageItemCard(
                    stage = StepStage.STEP_2,
                    isSelected = currentStage == StepStage.STEP_2,
                    isUnlocked = step1Completed,
                    isCompleted = step2Completed,
                    currentTripKm = if (currentStage == StepStage.STEP_2) currentTripKm else (if (step2Completed) 28f else 0f),
                    onSelect = {
                        if (step1Completed) {
                            onSelectStage(StepStage.STEP_2)
                            onDismiss()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // STAGE 3 CARD (FLYING CAR MODE)
                StageItemCard(
                    stage = StepStage.STEP_3,
                    isSelected = currentStage == StepStage.STEP_3,
                    isUnlocked = step1Completed && step2Completed,
                    isCompleted = false,
                    currentTripKm = if (currentStage == StepStage.STEP_3) currentTripKm else 0f,
                    onSelect = {
                        if (step1Completed && step2Completed) {
                            onSelectStage(StepStage.STEP_3)
                            onDismiss()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun StageItemCard(
    stage: StepStage,
    isSelected: Boolean,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    currentTripKm: Float,
    onSelect: () -> Unit
) {
    val progress = (currentTripKm / stage.targetKm).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = when {
                    isSelected -> Color(0xFF00E5FF)
                    isCompleted -> Color(0xFF10B981)
                    isUnlocked -> Color(0xFF334155)
                    else -> Color(0xFF1E293B)
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = isUnlocked) { onSelect() }
            .testTag("stage_card_${stage.stageNumber}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFF1E293B) else Color(0xFF111827).copy(alpha = 0.6f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    stage == StepStage.STEP_3 && isUnlocked -> Color(0xFF00E5FF)
                                    isCompleted -> Color(0xFF10B981)
                                    isUnlocked -> Color(0xFF3B82F6)
                                    else -> Color(0xFF475569)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isUnlocked) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        } else if (isCompleted) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                        } else if (stage == StepStage.STEP_3) {
                            Icon(Icons.Default.AirplanemodeActive, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                        } else {
                            Text("${stage.stageNumber}", fontWeight = FontWeight.Black, color = Color.White, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(stage.title, fontWeight = FontWeight.Bold, color = if (isUnlocked) Color.White else Color.Gray, fontSize = 14.sp)
                        Text(stage.subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8))
                    }
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0284C7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("CURRENT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(stage.description, fontSize = 11.sp, color = Color(0xFFCBD5E1), lineHeight = 15.sp)

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            if (isUnlocked) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            if (isCompleted) "Stage Target Met!" else "Trip Progress: ${String.format("%.1f", currentTripKm)} / ${stage.targetKm.toInt()} km",
                            fontSize = 11.sp,
                            color = if (isCompleted) Color(0xFF34D399) else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                        Text("${(progress * 100).roundToInt()}%", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (stage == StepStage.STEP_3) Color(0xFF00E5FF) else Color(0xFF10B981),
                        trackColor = Color(0xFF0F172A),
                    )
                }
            } else {
                Text(
                    "🔒 Locked: Complete Step ${stage.stageNumber - 1} to unlock this stage",
                    fontSize = 11.sp,
                    color = Color(0xFFF87171),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
