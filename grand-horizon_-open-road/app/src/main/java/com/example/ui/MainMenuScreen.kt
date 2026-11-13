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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirplanemodeActive
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GamePreferencesRepository
import com.example.model.CarColor
import com.example.model.CountryRoute
import com.example.model.CountryType
import com.example.model.StepStage
import com.example.model.TravelCompanion

@Composable
fun MainMenuScreen(
    repository: GamePreferencesRepository,
    onEnterCountryRoad: (CountryType) -> Unit
) {
    val context = LocalContext.current
    var selectedColor by remember { mutableStateOf(repository.selectedCarColor) }
    var selectedCompanion by remember { mutableStateOf(repository.selectedCompanion) }
    var currentStageNumber by remember { mutableStateOf(repository.currentStageNumber) }

    var showCompanionDialog by remember { mutableStateOf(false) }
    var showStagesDialog by remember { mutableStateOf(false) }
    var showGarageDialog by remember { mutableStateOf(false) }

    val turkeyDrawableId = context.resources.getIdentifier("img_turkey_highway", "drawable", context.packageName)
    val bdDrawableId = context.resources.getIdentifier("img_bangladesh_highway", "drawable", context.packageName)
    val companionDrawableId = context.resources.getIdentifier("img_companion_copilot", "drawable", context.packageName)
    val flyingCarDrawableId = context.resources.getIdentifier("img_flying_car", "drawable", context.packageName)

    val isFlyingCarUnlocked = repository.isFlyingCarUnlocked

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        color = Color(0xFF090D16)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // APP TOP HEADER
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "SAYED GRAND CAR",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(selectedColor.primaryColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("DRIVE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = Color.Black)
                        }
                    }
                    Text(
                        "Atmospheric Open-World Highway Simulator",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }

                // Step status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isFlyingCarUnlocked) Color(0xFF0284C7) else Color(0xFF1E293B))
                        .border(1.dp, if (isFlyingCarUnlocked) Color(0xFF00E5FF) else Color(0xFF334155), RoundedCornerShape(12.dp))
                        .clickable { showStagesDialog = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("menu_stages_badge")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isFlyingCarUnlocked) Icons.Default.AirplanemodeActive else Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isFlyingCarUnlocked) Color(0xFF00E5FF) else Color(0xFFFDE047),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (isFlyingCarUnlocked) "STEP 3: AERO FLY" else "STEP $currentStageNumber",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 1: TWO COUNTRY ROAD MODULES (BANGLADESH & TURKEY)
            Text(
                "SELECT EXPLORATION ROUTE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFDE047),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // OPTION 1: BANGLADESH ROAD MODULE
            CountrySelectionModuleCard(
                country = CountryRoute.BANGLADESH,
                imageResId = bdDrawableId,
                startCityNotice = "Starts from Chittagong • Covers Entire Country",
                buttonText = "ENTER BANGLADESH ROAD",
                testTag = "enter_bangladesh_road_btn",
                onEnter = {
                    repository.currentCountry = CountryType.BANGLADESH
                    onEnterCountryRoad(CountryType.BANGLADESH)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // OPTION 2: TURKEY ROAD MODULE
            CountrySelectionModuleCard(
                country = CountryRoute.TURKEY,
                imageResId = turkeyDrawableId,
                startCityNotice = "Starts from Istanbul • Covers Entire Country",
                buttonText = "ENTER TURKEY ROAD",
                testTag = "enter_turkey_road_btn",
                onEnter = {
                    repository.currentCountry = CountryType.TURKEY
                    onEnterCountryRoad(CountryType.TURKEY)
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            // SECTION 2: OUTSIDE OPTIONS (CHOOSE GIRL / COMPANION & CAR COLOR / GARAGE)
            Text(
                "CUSTOMIZE EXPEDITION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF38BDF8),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // OPTION: CHOOSE PERSON / TRAVEL COMPANION
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        .clickable { showCompanionDialog = true }
                        .testTag("choose_person_card_btn"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("CHOOSE PERSON", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF38BDF8))
                            Text(selectedCompanion.personalityEmoji, fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Companion Photo / Avatar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            if (companionDrawableId != 0) {
                                Image(
                                    painter = painterResource(id = companionDrawableId),
                                    contentDescription = "Companion Co-Pilot",
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(95.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(Color.Transparent, Color(0xFF0F172A).copy(alpha = 0.9f))
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(8.dp)
                            ) {
                                Text(selectedCompanion.displayName, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                                Text(selectedCompanion.title, fontSize = 9.sp, color = Color(0xFFFDE047), maxLines = 1)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showCompanionDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Text("Choose Person", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // OPTION: CAR COLOR & GARAGE
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.5.dp, selectedColor.primaryColor.copy(alpha = 0.8f), RoundedCornerShape(18.dp))
                        .clickable { showGarageDialog = true }
                        .testTag("choose_car_color_card_btn"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("CAR COLOR", fontSize = 11.sp, fontWeight = FontWeight.Black, color = selectedColor.glowColor)
                            Icon(Icons.Default.Palette, contentDescription = null, tint = selectedColor.primaryColor, modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Car Silhouette & Active Color Visual
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            // Underglow
                            Box(
                                modifier = Modifier
                                    .size(90.dp, 35.dp)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(selectedColor.glowColor.copy(alpha = 0.8f), Color.Transparent)
                                        )
                                    )
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.DirectionsCar,
                                    contentDescription = "Hypercar",
                                    tint = selectedColor.primaryColor,
                                    modifier = Modifier.size(42.dp)
                                )
                                Text(
                                    selectedColor.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = { showGarageDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(34.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                        ) {
                            Text("Change Color", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // SECTION 3: STAGES & FLYING CAR STATUS CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .border(
                        1.5.dp,
                        if (isFlyingCarUnlocked) Color(0xFF00E5FF) else Color(0xFF334155),
                        RoundedCornerShape(18.dp)
                    )
                    .clickable { showStagesDialog = true }
                    .testTag("menu_stages_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isFlyingCarUnlocked) Color(0xFF00E5FF) else Color(0xFF334155)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (isFlyingCarUnlocked) Icons.Default.AirplanemodeActive else Icons.Default.Speed,
                                contentDescription = null,
                                tint = if (isFlyingCarUnlocked) Color.Black else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                "3 JOURNEY STAGES (STEPS 1, 2, 3)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                if (isFlyingCarUnlocked) "★ Step 3 Flying Car Unlocked & Ready!" else "Step 3 unlocks the Flying Car after Steps 1 & 2",
                                fontSize = 11.sp,
                                color = if (isFlyingCarUnlocked) Color(0xFF00E5FF) else Color(0xFFFDE047),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { showStagesDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("View", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // DIALOGS
        if (showCompanionDialog) {
            CompanionSelectionDialog(
                selectedCompanion = selectedCompanion,
                onCompanionSelected = { companion ->
                    selectedCompanion = companion
                    repository.selectedCompanion = companion
                },
                onDismiss = { showCompanionDialog = false }
            )
        }

        if (showGarageDialog) {
            GarageColorDialog(
                selectedColor = selectedColor,
                onColorSelected = { color ->
                    selectedColor = color
                    repository.selectedCarColor = color
                },
                onDismiss = { showGarageDialog = false }
            )
        }

        if (showStagesDialog) {
            StepProgressionDialog(
                currentStage = StepStage.entries.find { it.stageNumber == currentStageNumber } ?: StepStage.STEP_1,
                step1Completed = repository.isStep1Completed,
                step2Completed = repository.isStep2Completed,
                currentTripKm = 0f,
                onSelectStage = { stage ->
                    currentStageNumber = stage.stageNumber
                    repository.currentStageNumber = stage.stageNumber
                },
                onDismiss = { showStagesDialog = false }
            )
        }
    }
}

@Composable
private fun CountrySelectionModuleCard(
    country: CountryRoute,
    imageResId: Int,
    startCityNotice: String,
    buttonText: String,
    testTag: String,
    onEnter: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, country.primaryColor, RoundedCornerShape(20.dp))
            .clickable { onEnter() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Scenic Banner Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
            ) {
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = country.displayName,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(135.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(country.primaryColor, Color(0xFF0F172A))
                                )
                            )
                    )
                }

                // Overlay gradient for readable text
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(135.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xFF1E293B))
                            )
                        )
                )

                // Country Flag & Name Badge
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(country.flagEmoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            country.displayName.uppercase(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            country.localName,
                            fontSize = 12.sp,
                            color = Color(0xFFFDE047),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Route Details & Start City Confirmation
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = country.primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            startCityNotice,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        "${country.cities.size} Cities",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    country.description,
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1),
                    lineHeight = 15.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Big Action Button: ENTER THIS COUNTRY ROAD
                Button(
                    onClick = onEnter,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag(testTag),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = country.primaryColor,
                        contentColor = Color.White
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            buttonText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}
