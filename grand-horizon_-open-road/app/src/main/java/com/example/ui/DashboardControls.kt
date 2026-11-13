package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CameraView
import com.example.model.CarColor
import com.example.model.DriveGear

@Composable
fun DashboardControls(
    modifier: Modifier = Modifier,
    speedKmH: Float,
    rpm: Float,
    currentGear: DriveGear,
    carColor: CarColor,
    isFlightUnlocked: Boolean,
    isFlightMode: Boolean,
    isBoosting: Boolean,
    boostEnergy: Float,
    headlightsMode: Int,
    cameraView: CameraView,
    handbrakeActive: Boolean,
    soundEnabled: Boolean,
    radioStation: Int = 0,
    onTapAccelerate: () -> Unit,
    onReleaseAccelerate: () -> Unit = {},
    onTapBrake: () -> Unit,
    onReleaseBrake: () -> Unit = {},
    onSteerLeft: () -> Unit,
    onSteerRight: () -> Unit,
    onReleaseSteer: () -> Unit,
    onNitroBoost: (Boolean) -> Unit,
    onHandbrakeToggle: () -> Unit,
    onGearChange: (DriveGear) -> Unit,
    onFlightToggle: () -> Unit,
    onAscendChange: (Float) -> Unit,
    onHornTrigger: (Boolean) -> Unit,
    onHeadlightsCycle: () -> Unit,
    onCameraCycle: () -> Unit,
    onSoundToggle: () -> Unit,
    onRadioCycle: () -> Unit = {},
    onBackToMenu: () -> Unit = {},
    onOpenCountrySelect: () -> Unit,
    onOpenStages: () -> Unit,
    onOpenGarage: () -> Unit,
    onOpenRouteMap: () -> Unit,
    onCycleWeather: () -> Unit,
    onCycleTimeOfDay: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        // 1. TOP FLOATING ACTION BAR: Quick Settings, Stages & Utilities (Horizontally scrollable on narrow screens)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = onBackToMenu,
                modifier = Modifier.testTag("menu_return_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFDC2626).copy(alpha = 0.90f),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Menu", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Menu", fontSize = 11.sp, fontWeight = FontWeight.Black)
            }

            FilledTonalButton(
                onClick = onOpenCountrySelect,
                modifier = Modifier.testTag("country_select_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.90f),
                    contentColor = Color.White
                )
            ) {
                Text("Route", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            FilledTonalButton(
                onClick = onOpenStages,
                modifier = Modifier.testTag("stages_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isFlightUnlocked) Color(0xFF0284C7).copy(alpha = 0.92f) else Color(0xFF1E293B).copy(alpha = 0.90f),
                    contentColor = Color.White
                )
            ) {
                Text("Stages", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            FilledTonalButton(
                onClick = onOpenGarage,
                modifier = Modifier.testTag("garage_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.90f),
                    contentColor = carColor.glowColor
                )
            ) {
                Text("Paint", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            FilledTonalButton(
                onClick = onOpenRouteMap,
                modifier = Modifier.testTag("map_route_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFF1E293B).copy(alpha = 0.90f),
                    contentColor = Color.White
                )
            ) {
                Text("Map", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Right utility icons
            IconButton(
                onClick = onCycleWeather,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.90f), CircleShape)
                    .testTag("weather_cycle_btn")
            ) {
                Icon(Icons.Default.WbSunny, contentDescription = "Cycle Weather", tint = Color(0xFFFDE047), modifier = Modifier.size(16.dp))
            }

            IconButton(
                onClick = onCycleTimeOfDay,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.90f), CircleShape)
                    .testTag("time_cycle_btn")
            ) {
                Icon(Icons.Default.FlashOn, contentDescription = "Cycle Time of Day", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
            }

            IconButton(
                onClick = onCameraCycle,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.90f), CircleShape)
                    .testTag("camera_cycle_btn")
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Switch Camera (${cameraView.label})", tint = Color.White, modifier = Modifier.size(16.dp))
            }

            IconButton(
                onClick = onHeadlightsCycle,
                modifier = Modifier
                    .size(36.dp)
                    .background(if (headlightsMode > 0) Color(0xFF0369A1) else Color(0xFF1E293B).copy(alpha = 0.90f), CircleShape)
                    .testTag("headlights_btn")
            ) {
                Icon(
                    Icons.Default.Highlight,
                    contentDescription = "Cycle Headlights",
                    tint = if (headlightsMode == 2) Color(0xFFFEF08A) else if (headlightsMode == 1) Color.White else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Horn Button (hold to honk)
            var hornPressed by remember { mutableStateOf(false) }
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (hornPressed) Color(0xFFF59E0B) else Color(0xFF1E293B).copy(alpha = 0.90f))
                    .border(1.dp, if (hornPressed) Color.White else Color(0xFF475569), CircleShape)
                    .testTag("horn_btn")
                    .arcadeTouch(
                        onDown = {
                            hornPressed = true
                            onHornTrigger(true)
                        },
                        onUp = {
                            hornPressed = false
                            onHornTrigger(false)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.NotificationsActive,
                    contentDescription = "Car Horn",
                    tint = if (hornPressed) Color.Black else Color(0xFFFDE047),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Radio Station Cycle (Chill -> Synth -> Off)
            IconButton(
                onClick = onRadioCycle,
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        when (radioStation) {
                            0 -> Color(0xFF0F766E)
                            1 -> Color(0xFF6D28D9)
                            else -> Color(0xFF1E293B).copy(alpha = 0.90f)
                        },
                        CircleShape
                    )
                    .testTag("radio_cycle_btn")
            ) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = "Cycle Car Radio",
                    tint = if (radioStation == 2) Color.Gray else Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = onSoundToggle,
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.90f), CircleShape)
                    .testTag("sound_toggle_btn")
            ) {
                Icon(
                    if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeMute,
                    contentDescription = "Toggle Sound",
                    tint = if (soundEnabled) Color(0xFF10B981) else Color.Gray,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // STEP 3 SPECIAL: FLYING CAR FLIGHT CONTROLS (When Unlocked)
        if (isFlightUnlocked) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .border(1.dp, if (isFlightMode) Color(0xFF00E5FF) else Color(0xFF334155), RoundedCornerShape(12.dp)),
                color = if (isFlightMode) Color(0xFF0C4A6E).copy(alpha = 0.90f) else Color(0xFF0F172A).copy(alpha = 0.85f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onFlightToggle,
                        modifier = Modifier.testTag("flight_mode_toggle_btn"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFlightMode) Color(0xFF00E5FF) else Color(0xFF1E293B),
                            contentColor = if (isFlightMode) Color.Black else Color.White
                        )
                    ) {
                        Icon(Icons.Default.AirplanemodeActive, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isFlightMode) "FLIGHT ACTIVE" else "STEP 3: FLYING CAR", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                    }

                    if (isFlightMode) {
                        var ascendPressed by remember { mutableStateOf(false) }
                        var descendPressed by remember { mutableStateOf(false) }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (ascendPressed) Color(0xFF10B981) else Color(0xFF059669))
                                    .border(1.dp, if (ascendPressed) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("flight_ascend_btn")
                                    .arcadeTouch(
                                        onDown = {
                                            ascendPressed = true
                                            onAscendChange(1f)
                                        },
                                        onUp = {
                                            ascendPressed = false
                                            onAscendChange(0f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = "Ascend", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("UP", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (descendPressed) Color(0xFF64748B) else Color(0xFF475569))
                                    .border(1.dp, if (descendPressed) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("flight_descend_btn")
                                    .arcadeTouch(
                                        onDown = {
                                            descendPressed = true
                                            onAscendChange(-1f)
                                        },
                                        onUp = {
                                            descendPressed = false
                                            onAscendChange(0f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = "Descend", tint = Color.White, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("DOWN", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. PRIMARY ARCADE DRIVING CONTROLS: Left Steering, Center Gear, Right Pedals Cluster
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // LEFT ZONE: STEER LEFT & RIGHT & DRIFT
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                    .border(2.dp, Color(0xFF38BDF8), RoundedCornerShape(16.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // STEER LEFT BUTTON (◀ LEFT)
                var steerLeftPressed by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .size(54.dp, 66.dp)
                        .graphicsLayer {
                            scaleX = if (steerLeftPressed) 0.94f else 1f
                            scaleY = if (steerLeftPressed) 0.94f else 1f
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                if (steerLeftPressed) listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                                else listOf(Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF075985))
                            )
                        )
                        .border(2.dp, if (steerLeftPressed) Color.White else Color(0xFF7DD3FC), RoundedCornerShape(12.dp))
                        .testTag("steer_left_btn")
                        .arcadeTouch(
                            onDown = {
                                steerLeftPressed = true
                                onSteerLeft()
                            },
                            onUp = {
                                steerLeftPressed = false
                                onReleaseSteer()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("◀", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Black)
                        Text("STEER", fontSize = 8.sp, color = Color(0xFFE0F2FE), fontWeight = FontWeight.Bold)
                        Text("LEFT", fontSize = 9.sp, color = Color(0xFFBAE6FD), fontWeight = FontWeight.Black)
                    }
                }

                // DRIFT / HANDBRAKE BUTTON
                Box(
                    modifier = Modifier
                        .size(40.dp, 66.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (handbrakeActive) Color(0xFFDC2626) else Color(0xFF1E293B)
                        )
                        .border(
                            1.5.dp,
                            if (handbrakeActive) Color(0xFFFCA5A5) else Color(0xFF64748B),
                            RoundedCornerShape(10.dp)
                        )
                        .testTag("handbrake_btn")
                        .pointerInput(Unit) {
                            detectTapGestures { onHandbrakeToggle() }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("(P)", fontSize = 13.sp, color = if (handbrakeActive) Color.White else Color(0xFFEF4444), fontWeight = FontWeight.Black)
                        Text("DRIFT", fontSize = 7.5.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // STEER RIGHT BUTTON (▶ RIGHT)
                var steerRightPressed by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .size(54.dp, 66.dp)
                        .graphicsLayer {
                            scaleX = if (steerRightPressed) 0.94f else 1f
                            scaleY = if (steerRightPressed) 0.94f else 1f
                        }
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.verticalGradient(
                                if (steerRightPressed) listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
                                else listOf(Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF075985))
                            )
                        )
                        .border(2.dp, if (steerRightPressed) Color.White else Color(0xFF7DD3FC), RoundedCornerShape(12.dp))
                        .testTag("steer_right_btn")
                        .arcadeTouch(
                            onDown = {
                                steerRightPressed = true
                                onSteerRight()
                            },
                            onUp = {
                                steerRightPressed = false
                                onReleaseSteer()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("▶", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Black)
                        Text("STEER", fontSize = 8.sp, color = Color(0xFFE0F2FE), fontWeight = FontWeight.Bold)
                        Text("RIGHT", fontSize = 9.sp, color = Color(0xFFBAE6FD), fontWeight = FontWeight.Black)
                    }
                }
            }

            // CENTER: COMPACT GEAR TRANSMISSION SELECTOR (R, N, D, S)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                    .border(1.5.dp, Color(0xFF475569), RoundedCornerShape(10.dp))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                listOf(DriveGear.REVERSE, DriveGear.NEUTRAL, DriveGear.DRIVE, DriveGear.SPORT).forEach { gear ->
                    val isSelected = currentGear == gear
                    Box(
                        modifier = Modifier
                            .size(24.dp, 44.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) carColor.primaryColor else Color(0xFF1E293B)
                            )
                            .testTag("gear_${gear.label}")
                            .pointerInput(gear) {
                                detectTapGestures { onGearChange(gear) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            gear.label,
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // RIGHT ZONE: SPEED BOOSTER WITH FUEL GAUGE, SEPARATE BRAKE & ACCELERATOR PEDALS
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // SEPARATE REALISTIC SPEED BOOSTER BUTTON (🚀 NITRO BOOST + LIVE CHARGE BAR)
                var boostPressed by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier
                        .size(126.dp, 36.dp)
                        .graphicsLayer {
                            scaleX = if (boostPressed || isBoosting) 0.96f else 1f
                            scaleY = if (boostPressed || isBoosting) 0.96f else 1f
                        }
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .border(
                            2.dp,
                            if (isBoosting) Color(0xFFFDE047) else Color(0xFF38BDF8),
                            RoundedCornerShape(10.dp)
                        )
                        .testTag("nitro_booster_btn")
                        .arcadeTouch(
                            onDown = {
                                boostPressed = true
                                onNitroBoost(true)
                            },
                            onUp = {
                                boostPressed = false
                                onNitroBoost(false)
                            }
                        )
                ) {
                    // Live Nitro Fuel Gauge Fill
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(boostEnergy.coerceIn(0.05f, 1f))
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    if (isBoosting) listOf(Color(0xFFF97316), Color(0xFFDC2626))
                                    else listOf(Color(0xFF0284C7), Color(0xFF06B6D4))
                                )
                            )
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.RocketLaunch,
                            contentDescription = "Nitro Speed Booster",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            if (isBoosting) "BOOST ${(boostEnergy * 100).toInt()}%" else "SPEED BOOST ${(boostEnergy * 100).toInt()}%",
                            fontSize = 8.5.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                // SIDE-BY-SIDE SEPARATE REALISTIC PEDALS: BRAKE & ACCELERATOR
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // SEPARATE REALISTIC BRAKE PEDAL (STOP)
                    var brakePressed by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .size(56.dp, 76.dp)
                            .graphicsLayer {
                                scaleX = if (brakePressed) 0.94f else 1f
                                scaleY = if (brakePressed) 0.94f else 1f
                            }
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    if (brakePressed) listOf(Color(0xFFEF4444), Color(0xFFB91C1C))
                                    else listOf(Color(0xFF991B1B), Color(0xFF7F1D1D), Color(0xFF450A0A))
                                )
                            )
                            .border(2.dp, if (brakePressed) Color(0xFFFDE047) else Color(0xFFEF4444), RoundedCornerShape(12.dp))
                            .testTag("brake_pedal")
                            .arcadeTouch(
                                onDown = {
                                    brakePressed = true
                                    onTapBrake()
                                },
                                onUp = {
                                    brakePressed = false
                                    onReleaseBrake()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            for (i in 0..1) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp, 3.dp)
                                        .background(Color(0xFF2E0505))
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                            }
                            Text("BRAKE", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Black)
                            Text("■ STOP ■", fontSize = 7.5.sp, color = Color(0xFFFCA5A5), fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // SEPARATE REALISTIC ACCELERATOR (GAS) PEDAL (GO / SPEED UP)
                    var gasPressed by remember { mutableStateOf(false) }
                    Box(
                        modifier = Modifier
                            .size(62.dp, 88.dp)
                            .graphicsLayer {
                                scaleX = if (gasPressed) 0.94f else 1f
                                scaleY = if (gasPressed) 0.94f else 1f
                            }
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.verticalGradient(
                                    if (gasPressed) listOf(Color(0xFF10B981), Color(0xFF047857))
                                    else listOf(Color(0xFF047857), Color(0xFF065F46), Color(0xFF064E3B))
                                )
                            )
                            .border(2.5.dp, if (gasPressed) Color(0xFFFDE047) else Color(0xFF10B981), RoundedCornerShape(14.dp))
                            .testTag("accelerator_pedal")
                            .arcadeTouch(
                                onDown = {
                                    gasPressed = true
                                    onTapAccelerate()
                                },
                                onUp = {
                                    gasPressed = false
                                    onReleaseAccelerate()
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("ACCELERATOR", fontSize = 7.sp, color = Color(0xFFA7F3D0), fontWeight = FontWeight.Black)
                            Spacer(modifier = Modifier.height(1.dp))
                            Text("GAS", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Black)
                            Text("▲▲", fontSize = 14.sp, color = Color(0xFF6EE7B7), fontWeight = FontWeight.Black)
                            Text("MOVE / GO", fontSize = 7.5.sp, color = Color(0xFFD1FAE5), fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .size(40.dp, 3.dp)
                                    .background(Color(0xFF022C22))
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Modifier.arcadeTouch(
    onDown: () -> Unit,
    onUp: () -> Unit
): Modifier {
    val currentOnDown by rememberUpdatedState(onDown)
    val currentOnUp by rememberUpdatedState(onUp)
    return this.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val pointerId = down.id
            down.consume()
            currentOnDown()
            try {
                while (true) {
                    val event = awaitPointerEvent()
                    val pointer = event.changes.firstOrNull { it.id == pointerId } ?: break
                    if (pointer.changedToUpIgnoreConsumed() || !pointer.pressed) {
                        pointer.consume()
                        break
                    }
                }
            } finally {
                currentOnUp()
            }
        }
    }
}
