package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioSynthEngine
import com.example.data.GamePreferencesRepository
import com.example.model.CameraView
import com.example.model.CarColor
import com.example.model.CountryRoute
import com.example.model.CountryType
import com.example.model.DriveGear
import com.example.model.StepStage
import com.example.model.TimeOfDay
import com.example.model.TravelCompanion
import com.example.model.WeatherCondition
import com.example.physics.VehiclePhysicsEngine
import com.example.renderer.CarModelRenderer
import com.example.renderer.RoadRenderer
import kotlin.math.roundToInt

@Composable
fun DriveGameScreen(
    modifier: Modifier = Modifier,
    initialCountry: CountryType? = null,
    onBackToMenu: () -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember { GamePreferencesRepository(context) }
    val audioSynth = remember { AudioSynthEngine() }
    val physics = remember { VehiclePhysicsEngine() }
    val roadRenderer = remember { RoadRenderer() }
    val carRenderer = remember { CarModelRenderer() }

    // Intercept back button to return to Main Menu
    BackHandler {
        onBackToMenu()
    }

    // Country, Stage, Car & Companion State
    var currentCountryType by remember { mutableStateOf(initialCountry ?: repository.currentCountry) }
    var currentCountry by remember(currentCountryType) { mutableStateOf(CountryRoute.getRoute(currentCountryType)) }
    var currentStage by remember {
        mutableStateOf(
            StepStage.entries.find { it.stageNumber == repository.currentStageNumber } ?: StepStage.STEP_1
        )
    }
    var step1Completed by remember { mutableStateOf(repository.isStep1Completed) }
    var step2Completed by remember { mutableStateOf(repository.isStep2Completed) }
    var carColor by remember { mutableStateOf(repository.selectedCarColor) }
    var companion by remember { mutableStateOf(repository.selectedCompanion) }
    var soundEnabled by remember { mutableStateOf(repository.soundEnabled) }
    var radioStation by remember { mutableIntStateOf(repository.radioIndex.coerceIn(0, 2)) }

    // Restore saved odometer and flight readiness on entry
    LaunchedEffect(Unit) {
        physics.totalDistanceKm = if (currentCountryType == CountryType.TURKEY) {
            repository.totalKmTurkey
        } else {
            repository.totalKmBangladesh
        }
        if (currentStage == StepStage.STEP_3 && step1Completed && step2Completed) {
            physics.isFlightMode = true
            physics.flightAltitude = 15f
        }
    }

    // Dynamic Weather and Day-Night Cycle
    var weatherIndex by remember { mutableIntStateOf(0) }
    val weathers = remember { WeatherCondition.entries }
    val currentWeather = weathers[weatherIndex % weathers.size]

    var timeOfDayIndex by remember { mutableIntStateOf(1) } // start midday
    val timesOfDay = remember { TimeOfDay.entries }
    val currentTimeOfDay = timesOfDay[timeOfDayIndex % timesOfDay.size]

    // Driving Controls State
    var headlightsMode by remember { mutableIntStateOf(0) } // 0=Off, 1=Low, 2=High
    var cameraView by remember { mutableStateOf(CameraView.CHASE) }
    var handbrakeActive by remember { mutableStateOf(false) }

    // Dialog sheets
    var showCountryDialog by remember { mutableStateOf(false) }
    var showStagesDialog by remember { mutableStateOf(false) }
    var showGarageDialog by remember { mutableStateOf(false) }
    var showMapDialog by remember { mutableStateOf(false) }
    var showCompanionDialog by remember { mutableStateOf(false) }

    // Milestone Celebration Notification
    var milestoneMessage by remember { mutableStateOf<String?>(null) }
    var milestoneTimer by remember { mutableFloatStateOf(0f) }

    // Companion interactive speech quote
    var companionQuoteIndex by remember { mutableIntStateOf(0) }
    val activeQuote = companion.dialogueQuotes[companionQuoteIndex % companion.dialogueQuotes.size]

    // Helper to save odometer per country
    fun saveCountryOdometer(type: CountryType, km: Float) {
        if (type == CountryType.TURKEY) {
            repository.totalKmTurkey = km
        } else {
            repository.totalKmBangladesh = km
        }
    }

    // Audio lifecycle & odometer persistence on exit
    DisposableEffect(Unit) {
        audioSynth.isEnabled = soundEnabled
        audioSynth.radioStation = radioStation
        audioSynth.start()
        onDispose {
            saveCountryOdometer(currentCountryType, physics.totalDistanceKm)
            audioSynth.stop()
        }
    }

    // 60 FPS Dynamic Animation Tick & Continuous Elapsed Clock
    var frameTick by remember { mutableLongStateOf(0L) }
    var elapsedSeconds by remember { mutableFloatStateOf(0f) }

    // Main 60 FPS Game Loop
    LaunchedEffect(currentCountry, currentStage) {
        var lastTimeNanos = 0L
        var dynamicCycleTimer = 0f
        var companionChatTimer = 0f
        var saveOdometerTimer = 0f

        while (true) {
            withFrameNanos { nowNanos ->
                if (lastTimeNanos != 0L) {
                    val dt = ((nowNanos - lastTimeNanos) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                    elapsedSeconds += dt

                    // 1. Update Physics Simulation
                    physics.update(dt, currentCountry, currentStage)

                    // 2. Update Audio Synthesizer
                    audioSynth.isEnabled = soundEnabled
                    audioSynth.radioStation = radioStation
                    audioSynth.rpm = physics.rpm
                    audioSynth.speedRatio = (physics.speedKmH / 300f).coerceIn(0f, 1f)
                    audioSynth.isHandbraking = physics.handbrakeActive
                    audioSynth.isFlightMode = physics.isFlightMode
                    audioSynth.isBoosting = physics.isBoosting
                    audioSynth.rainIntensity = currentWeather.rainIntensity

                    // 3. Periodically persist odometer every 2 seconds
                    saveOdometerTimer += dt
                    if (saveOdometerTimer > 2f) {
                        saveOdometerTimer = 0f
                        saveCountryOdometer(currentCountry.type, physics.totalDistanceKm)
                    }

                    // 4. Dynamic Day-Night & Weather Progression (every 45s advances time)
                    dynamicCycleTimer += dt
                    if (dynamicCycleTimer > 45f) {
                        dynamicCycleTimer = 0f
                        timeOfDayIndex = (timeOfDayIndex + 1) % timesOfDay.size
                    }

                    // 5. Companion occasional road dialogue (every 30s)
                    companionChatTimer += dt
                    if (companionChatTimer > 30f) {
                        companionChatTimer = 0f
                        companionQuoteIndex++
                    }

                    // 6. Milestone Check: Stage 1 or Stage 2 Target Reached
                    if (currentStage == StepStage.STEP_1 && !step1Completed && physics.tripDistanceKm >= currentStage.targetKm) {
                        step1Completed = true
                        repository.isStep1Completed = true
                        milestoneMessage = "🎉 Step 1 Complete! Step 2 Grand Traverse Unlocked!"
                        milestoneTimer = 5f
                    } else if (currentStage == StepStage.STEP_2 && !step2Completed && physics.tripDistanceKm >= currentStage.targetKm) {
                        step2Completed = true
                        repository.isStep2Completed = true
                        milestoneMessage = "🚀 STEP 2 COMPLETE! FLYING CAR UNLOCKED IN STEP 3!"
                        milestoneTimer = 7f
                    }

                    if (milestoneTimer > 0f) {
                        milestoneTimer -= dt
                        if (milestoneTimer <= 0f) {
                            milestoneMessage = null
                        }
                    }
                }
                lastTimeNanos = nowNanos
                frameTick = nowNanos
            }
        }
    }

    // Read frameTick to trigger smooth 60 FPS UI recomposition for speedometer and road
    @Suppress("UNUSED_VARIABLE")
    val currentFrameTick = frameTick

    // Unified current & next city waypoint calculation (synced with RouteMapDialog)
    val cityProgress = currentCountry.findCityProgress(physics.totalDistanceKm)
    val activeCity = cityProgress.activeCity
    val nextCity = cityProgress.nextCity

    val cityDrawableId = remember(activeCity.imageResName, currentCountryType) {
        val id = context.resources.getIdentifier(activeCity.imageResName, "drawable", context.packageName)
        if (id != 0) id else {
            if (currentCountryType == CountryType.TURKEY) {
                com.example.R.drawable.img_city_istanbul
            } else {
                com.example.R.drawable.img_city_chittagong
            }
        }
    }

    // Top Level Game Screen Layout
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("game_screen_container")
    ) {
        // 0. FULL-SCREEN PHOTOGRAPHIC REALISTIC CITY BACKDROP (Synchronized with car movement, steering, and road hills)
        val cityHorizonPan = (physics.steeringInput * -45f) - (physics.carX * 28f)
        val cityPitchShift = (physics.currentHillPitch * 25f) + (if (physics.isBoosting) (kotlin.random.Random.nextFloat() - 0.5f) * 4f else 0f)
        val cityScale = if (physics.isBoosting) 1.25f else 1.18f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clipToBounds()
        ) {
            if (cityDrawableId != 0) {
                Image(
                    painter = painterResource(id = cityDrawableId),
                    contentDescription = activeCity.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationX = cityHorizonPan
                            translationY = cityPitchShift
                            scaleX = cityScale
                            scaleY = cityScale
                        },
                    contentScale = ContentScale.Crop
                )
            }
            // Atmospheric Day / Night / Weather overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                if (currentTimeOfDay.isNight) Color(0xFF030712).copy(alpha = 0.45f)
                                else if (currentTimeOfDay == TimeOfDay.SUNSET || currentTimeOfDay == TimeOfDay.DAWN) Color(0xFF7C2D12).copy(alpha = 0.25f)
                                else Color.Transparent,
                                Color(0xFF090D16).copy(alpha = if (currentTimeOfDay.isNight) 0.65f else 0.20f)
                            )
                        )
                    )
            )
        }

        // 1. Canvas Rendering Layer (Road, 3D Car, Traffic, Weather)
        Canvas(modifier = Modifier.fillMaxSize()) {
            @Suppress("UNUSED_VARIABLE")
            val currentCanvasTick = frameTick
            val timeSeconds = elapsedSeconds

            // Render Road, Sky, Mountains, Traffic, Weather
            roadRenderer.drawEnvironmentAndRoad(
                drawScope = this,
                physics = physics,
                country = currentCountry,
                weather = currentWeather,
                timeOfDay = currentTimeOfDay,
                headlightsMode = headlightsMode,
                timeSeconds = timeSeconds,
                activeCityName = activeCity.name,
                nextCityName = nextCity.name
            )

            // Render Advanced Hypercar (Chase, Cockpit, or Drone)
            carRenderer.drawCar(
                drawScope = this,
                physics = physics,
                carColor = carColor,
                cameraView = cameraView,
                headlightsMode = headlightsMode,
                isRaining = currentWeather.rainIntensity > 0.1f,
                country = currentCountry,
                timeSeconds = timeSeconds
            )
        }

        // 2. TOP HUD: LIVE GPS ROUTE MINI-MAP, DEDICATED SPEED TRACKER HUD & COMPANION
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // 1. LIVE ON-SCREEN GPS ROUTE MINI-MAP WIDGET
                Surface(
                    color = Color(0xFF0F172A).copy(alpha = 0.94f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, currentCountry.primaryColor.copy(alpha = 0.85f)),
                    modifier = Modifier
                        .weight(1.18f)
                        .padding(end = 6.dp)
                        .clickable { showMapDialog = true }
                        .testTag("live_gps_minimap")
                ) {
                    Column(modifier = Modifier.padding(7.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(currentCountry.flagEmoji, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "LIVE GPS MAP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = currentCountry.primaryColor
                                )
                            }
                            Text(
                                "TAP FULL MAP ↗",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (cityDrawableId != 0) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp, 36.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .border(1.dp, currentCountry.primaryColor, RoundedCornerShape(6.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = cityDrawableId),
                                        contentDescription = activeCity.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "📍 ${activeCity.name.uppercase()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "(${activeCity.localName.take(12)})",
                                        fontSize = 9.sp,
                                        color = Color(0xFFFDE047),
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Text(
                                    "➔ Next: ${nextCity.name} (${"%.1f".format(cityProgress.remainingKmToNext)} KM)",
                                    fontSize = 9.sp,
                                    color = Color(0xFFCBD5E1),
                                    maxLines = 1
                                )
                                Text(
                                    activeCity.landmarkName,
                                    fontSize = 8.5.sp,
                                    color = Color(0xFF94A3B8),
                                    maxLines = 1
                                )
                            }
                        }

                        // Highway GPS mini-progress route track
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF1E293B))
                        ) {
                            val routeProgress = cityProgress.overallProgress
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(routeProgress)
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(currentCountry.primaryColor, Color(0xFF38BDF8))
                                        )
                                    )
                            )
                        }
                    }
                }

                // 2. DEDICATED SPEED TRACKER HUD
                Surface(
                    color = Color(0xFF090D16).copy(alpha = 0.94f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, carColor.glowColor.copy(alpha = 0.85f)),
                    modifier = Modifier.testTag("speed_tracker_hud")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        // Hypercar Aero Telemetry Status
                        val aeroStatusText = when {
                            physics.isFlightMode -> "⚡ AERO-FLIGHT: ACTIVE"
                            physics.brakeInput > 0.05f || physics.isBrakingPressed -> "🚨 AIRBRAKE: DEPLOYED"
                            physics.isBoosting -> "🚀 DRS: SUPERSPEED"
                            else -> "⚡ ACTIVE DOWNFORCE"
                        }
                        val aeroColor = when {
                            physics.isFlightMode -> Color(0xFF00E5FF)
                            physics.brakeInput > 0.05f || physics.isBrakingPressed -> Color(0xFFFF1744)
                            physics.isBoosting -> Color(0xFFFACC15)
                            else -> carColor.glowColor
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "CYBER-PHANTOM",
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF67E8F9)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("•", fontSize = 7.sp, color = Color(0xFF64748B))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                "SPEED TRACKER",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = carColor.glowColor
                            )
                        }

                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                "${physics.speedKmH.roundToInt()}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = when {
                                    physics.speedKmH > 300f -> Color(0xFFEF4444)
                                    physics.speedKmH > 180f -> Color(0xFFF59E0B)
                                    else -> Color.White
                                }
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                "KM/H",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Black
                            )
                        }

                        // Dynamic Color-Coded Speed Gauge Bar
                        val speedRatio = (physics.speedKmH / 360f).coerceIn(0.04f, 1f)
                        Box(
                            modifier = Modifier
                                .width(94.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF1E293B))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(speedRatio)
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            when {
                                                physics.speedKmH > 300f -> listOf(Color(0xFFEF4444), Color(0xFFF43F5E))
                                                physics.speedKmH > 180f -> listOf(Color(0xFF06B6D4), Color(0xFFF59E0B))
                                                else -> listOf(Color(0xFF10B981), Color(0xFF38BDF8))
                                            }
                                        )
                                    )
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            aeroStatusText,
                            fontSize = 7.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = aeroColor
                        )
                        Text(
                            "GEAR [ ${physics.currentGear.label} ] • ${physics.rpm.toInt()} RPM",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFCBD5E1)
                        )
                        Text(
                            "TRIP: ${"%.1f".format(physics.tripDistanceKm)} KM",
                            fontSize = 8.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // COMPANION CO-PILOT CHAT BANNER + IN-GAME SWITCHER
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable {
                        companionQuoteIndex++
                    }
                    .testTag("companion_chat_bubble"),
                color = Color(0xFF0F172A).copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(companion.personalityEmoji, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "${companion.displayName}: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        "\"$activeQuote\"",
                        fontSize = 11.sp,
                        color = Color(0xFFF1F5F9),
                        maxLines = 1,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0284C7).copy(alpha = 0.85f))
                            .clickable { showCompanionDialog = true }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .testTag("switch_companion_btn")
                    ) {
                        Text("Switch", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }
            }

            // Milestone Banner Alert
            AnimatedVisibility(
                visible = milestoneMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                milestoneMessage?.let { msg ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        color = Color(0xFF0284C7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(msg, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // 3. BOTTOM COCKPIT CONTROLS
        DashboardControls(
            modifier = Modifier.align(Alignment.BottomCenter),
            speedKmH = physics.speedKmH,
            rpm = physics.rpm,
            currentGear = physics.currentGear,
            carColor = carColor,
            isFlightUnlocked = (step1Completed && step2Completed) || currentStage == StepStage.STEP_3,
            isFlightMode = physics.isFlightMode,
            isBoosting = physics.isBoosting,
            boostEnergy = physics.boostEnergy,
            headlightsMode = headlightsMode,
            cameraView = cameraView,
            handbrakeActive = physics.handbrakeActive,
            soundEnabled = soundEnabled,
            radioStation = radioStation,
            onTapAccelerate = { physics.startAccelerating() },
            onReleaseAccelerate = { physics.stopAccelerating() },
            onTapBrake = { physics.startBraking() },
            onReleaseBrake = { physics.stopBraking() },
            onSteerLeft = { physics.startSteeringLeft() },
            onSteerRight = { physics.startSteeringRight() },
            onReleaseSteer = { physics.stopSteering() },
            onNitroBoost = { physics.activateNitro(it) },
            onHandbrakeToggle = {
                physics.handbrakeActive = !physics.handbrakeActive
                handbrakeActive = physics.handbrakeActive
            },
            onGearChange = { physics.currentGear = it },
            onFlightToggle = {
                physics.isFlightMode = !physics.isFlightMode
                if (physics.isFlightMode && physics.flightAltitude == 0f) {
                    physics.flightAltitude = 12f
                }
            },
            onAscendChange = { physics.flightAscendInput = it },
            onHornTrigger = { audioSynth.isHornActive = it },
            onHeadlightsCycle = { headlightsMode = (headlightsMode + 1) % 3 },
            onCameraCycle = {
                val views = CameraView.entries
                cameraView = views[(cameraView.ordinal + 1) % views.size]
            },
            onSoundToggle = {
                soundEnabled = !soundEnabled
                repository.soundEnabled = soundEnabled
                audioSynth.isEnabled = soundEnabled
            },
            onRadioCycle = {
                radioStation = (radioStation + 1) % 3
                repository.radioIndex = radioStation
                audioSynth.radioStation = radioStation
            },
            onBackToMenu = {
                saveCountryOdometer(currentCountryType, physics.totalDistanceKm)
                onBackToMenu()
            },
            onOpenCountrySelect = { showCountryDialog = true },
            onOpenStages = { showStagesDialog = true },
            onOpenGarage = { showGarageDialog = true },
            onOpenRouteMap = { showMapDialog = true },
            onCycleWeather = { weatherIndex++ },
            onCycleTimeOfDay = { timeOfDayIndex++ }
        )

        // 4. DIALOGS
        if (showCountryDialog) {
            CountrySelectionDialog(
                currentCountry = currentCountryType,
                onCountrySelected = { selected ->
                    saveCountryOdometer(currentCountryType, physics.totalDistanceKm)
                    currentCountryType = selected
                    repository.currentCountry = selected
                    physics.resetTrip()
                    physics.totalDistanceKm = if (selected == CountryType.TURKEY) {
                        repository.totalKmTurkey
                    } else {
                        repository.totalKmBangladesh
                    }
                    if (currentStage == StepStage.STEP_3 && step1Completed && step2Completed) {
                        physics.isFlightMode = true
                        physics.flightAltitude = 15f
                    }
                },
                onDismiss = { showCountryDialog = false }
            )
        }

        if (showStagesDialog) {
            StepProgressionDialog(
                currentStage = currentStage,
                step1Completed = step1Completed,
                step2Completed = step2Completed,
                currentTripKm = physics.tripDistanceKm,
                onSelectStage = { stage ->
                    currentStage = stage
                    repository.currentStageNumber = stage.stageNumber
                    physics.resetTrip()
                    if (stage == StepStage.STEP_3) {
                        physics.isFlightMode = true
                        physics.flightAltitude = 15f
                    }
                },
                onDismiss = { showStagesDialog = false }
            )
        }

        if (showGarageDialog) {
            GarageColorDialog(
                selectedColor = carColor,
                onColorSelected = { color ->
                    carColor = color
                    repository.selectedCarColor = color
                },
                onDismiss = { showGarageDialog = false }
            )
        }

        if (showMapDialog) {
            RouteMapDialog(
                country = currentCountry,
                currentOdometerKm = physics.totalDistanceKm,
                onDismiss = { showMapDialog = false }
            )
        }

        if (showCompanionDialog) {
            CompanionSelectionDialog(
                selectedCompanion = companion,
                onCompanionSelected = { comp ->
                    companion = comp
                    repository.selectedCompanion = comp
                    companionQuoteIndex = 0
                },
                onDismiss = { showCompanionDialog = false }
            )
        }
    }
}
