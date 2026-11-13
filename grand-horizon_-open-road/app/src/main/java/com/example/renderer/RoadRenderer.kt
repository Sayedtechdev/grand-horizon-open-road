package com.example.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.example.model.CountryRoute
import com.example.model.CountryType
import com.example.model.TimeOfDay
import com.example.model.TrafficCar
import com.example.model.WeatherCondition
import com.example.physics.VehiclePhysicsEngine
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class RoadRenderer {
    private val rainParticles = FloatArray(80) { Random.nextFloat() }
    private var lightningAlpha = 0f

    private val gantryRoutePaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        textAlign = android.graphics.Paint.Align.LEFT
    }
    private val gantryCityPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        textAlign = android.graphics.Paint.Align.CENTER
    }
    private val gantrySpeedPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLACK
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        textAlign = android.graphics.Paint.Align.CENTER
    }

    fun drawEnvironmentAndRoad(
        drawScope: DrawScope,
        physics: VehiclePhysicsEngine,
        country: CountryRoute,
        weather: WeatherCondition,
        timeOfDay: TimeOfDay,
        headlightsMode: Int,
        timeSeconds: Float,
        activeCityName: String = country.startCity,
        nextCityName: String = ""
    ) {
        with(drawScope) {
            val width = size.width
            val height = size.height
            val horizonY = height * 0.45f + (physics.currentHillPitch * 40f)

            // 1. Draw Sky Gradient & Celestial Body (Sun / Moon)
            drawSkyAndSunMoon(width, height, horizonY, weather, timeOfDay, timeSeconds)

            // 2. Draw Distant Parallax Mountain Ridge & Iconic Landmarks
            drawParallaxHorizonAndLandmarks(width, horizonY, country, weather, timeOfDay, physics)

            // 3. Draw Curvilinear Pseudo-3D Highway Road
            drawCurvilinearRoad(
                width = width,
                height = height,
                horizonY = horizonY,
                country = country,
                weather = weather,
                timeOfDay = timeOfDay,
                physics = physics,
                headlightsMode = headlightsMode,
                activeCityName = activeCityName,
                nextCityName = nextCityName
            )

            // 4. Draw Traffic Cars on Road
            drawTrafficVehicles(width, height, horizonY, country, weather, timeOfDay, physics)

            // 5. Draw Dynamic Weather Overlays (Rain, Fog, Headlight Beams, Lightning)
            drawWeatherOverlays(width, height, weather, timeOfDay, headlightsMode, physics, timeSeconds)
        }
    }

    private fun DrawScope.drawSkyAndSunMoon(
        width: Float,
        height: Float,
        horizonY: Float,
        weather: WeatherCondition,
        timeOfDay: TimeOfDay,
        timeSeconds: Float
    ) {
        // Dynamic lightning trigger for thunderstorm
        if (weather == WeatherCondition.THUNDERSTORM && Random.nextFloat() < 0.015f) {
            lightningAlpha = 0.85f
        } else {
            lightningAlpha = (lightningAlpha - 0.05f).coerceAtLeast(0f)
        }

        // Sky atmospheric tint overlay (allows underlying realistic city photography to be visible)
        val skyAlpha = when {
            lightningAlpha > 0.1f -> lightningAlpha
            timeOfDay.isNight -> 0.45f
            weather == WeatherCondition.MONSOON_RAIN -> 0.35f
            weather == WeatherCondition.DENSE_MIST -> 0.55f
            else -> 0.15f
        }
        val skyTop = if (lightningAlpha > 0.1f) Color.White.copy(alpha = lightningAlpha) else timeOfDay.skyTopColor.copy(alpha = skyAlpha)
        val skyBottom = if (lightningAlpha > 0.1f) Color(0xFFE2E8F0).copy(alpha = lightningAlpha) else timeOfDay.skyBottomColor.copy(alpha = skyAlpha * 0.7f)

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(skyTop, skyBottom),
                startY = 0f,
                endY = horizonY
            ),
            size = Size(width, horizonY)
        )

        // Sun or Moon position
        val sunX = width * 0.72f
        val sunY = horizonY * 0.38f
        val sunRadius = if (timeOfDay.isNight) 24f else 36f

        // Sun / Moon Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    timeOfDay.sunOrMoonColor.copy(alpha = if (weather == WeatherCondition.DENSE_MIST) 0.2f else 0.5f),
                    Color.Transparent
                ),
                center = Offset(sunX, sunY),
                radius = sunRadius * 3.5f
            ),
            radius = sunRadius * 3.5f,
            center = Offset(sunX, sunY)
        )

        // Solid Sun or Moon Core
        drawCircle(
            color = timeOfDay.sunOrMoonColor.copy(alpha = if (timeOfDay.isNight) 0.85f else 0.75f),
            radius = sunRadius,
            center = Offset(sunX, sunY)
        )
    }

    private fun DrawScope.drawAtmosphericBalloonsOrClouds(width: Float, horizonY: Float, timeSeconds: Float) {
        // Floating distant hot air balloons (iconic Cappadocia)
        val balloonX1 = (width * 0.22f + sin(timeSeconds * 0.2f) * 15f)
        val balloonY1 = horizonY * 0.28f
        drawOval(
            color = Color(0xFFEF4444),
            topLeft = Offset(balloonX1 - 10f, balloonY1 - 14f),
            size = Size(20f, 26f)
        )
        drawLine(
            color = Color.DarkGray,
            start = Offset(balloonX1, balloonY1 + 12f),
            end = Offset(balloonX1, balloonY1 + 18f),
            strokeWidth = 1.5f
        )

        val balloonX2 = (width * 0.36f + cos(timeSeconds * 0.15f) * 12f)
        val balloonY2 = horizonY * 0.22f
        drawOval(
            color = Color(0xFFF59E0B),
            topLeft = Offset(balloonX2 - 8f, balloonY2 - 11f),
            size = Size(16f, 22f)
        )
    }

    private fun DrawScope.drawParallaxHorizonAndLandmarks(
        width: Float,
        horizonY: Float,
        country: CountryRoute,
        weather: WeatherCondition,
        timeOfDay: TimeOfDay,
        physics: VehiclePhysicsEngine
    ) {
        val parallaxOffset = (physics.roadSegmentIndex * 0.05f) % width

        // 1. Distant Mountain Silhouette Layer
        val mountainPath = Path().apply {
            moveTo(0f, horizonY)
            var currentX = 0f
            val step = width / 12f
            var index = 0
            while (currentX <= width + step) {
                val peakHeight = if (country.type == CountryType.TURKEY) {
                    // Rugged Anatolian / Taurus mountains & rocky peaks
                    45f + sin((index + parallaxOffset * 0.02f) * 0.8f) * 28f + (index % 3) * 18f
                } else {
                    // Gentle Chittagong hills & river delta knolls
                    25f + sin((index + parallaxOffset * 0.02f) * 0.6f) * 15f
                }
                lineTo(currentX, horizonY - peakHeight)
                currentX += step
                index++
            }
            lineTo(width, horizonY)
            close()
        }

        val mountainColor = if (timeOfDay.isNight) {
            Color(0xFF0F172A).copy(alpha = 0.9f)
        } else if (country.type == CountryType.TURKEY) {
            Color(0xFF78716C) // Anatolian ochre stone
        } else {
            Color(0xFF065F46) // Lush green Bengal hills
        }
        drawPath(path = mountainPath, color = mountainColor)

        // 2. Modern City Skyscraper Skyline Layer (Illuminated Glass Towers & Windows)
        drawCitySkylineClusters(width, horizonY, country, timeOfDay, parallaxOffset)

        // 3. Country-Specific Iconic Landmark Silhouettes
        if (country.type == CountryType.TURKEY) {
            // Istanbul Bosphorus Bridge Towers & Cables silhouette on horizon
            drawBosphorusBridgeSilhouette(width, horizonY, timeOfDay)
        } else {
            // Bangladesh Padma Mega Bridge Pylons & River estuary
            drawPadmaBridgeSilhouette(width, horizonY, timeOfDay)
        }
    }

    private fun DrawScope.drawCitySkylineClusters(
        width: Float,
        horizonY: Float,
        country: CountryRoute,
        timeOfDay: TimeOfDay,
        parallaxOffset: Float
    ) {
        val buildingCount = 18
        val buildingStep = width / (buildingCount - 2)

        for (b in 0 until buildingCount) {
            val bx = (b * buildingStep + parallaxOffset * 0.4f) % (width + buildingStep) - (buildingStep * 0.5f)
            val bWidth = buildingStep * 0.72f
            val bHeight = 35f + ((b * 37) % 65) + sin(b * 1.5f) * 15f
            val by = horizonY - bHeight

            val buildingColor = if (timeOfDay.isNight) {
                if (b % 2 == 0) Color(0xFF0B1120) else Color(0xFF0F172A)
            } else {
                if (country.type == CountryType.TURKEY) Color(0xFF334155) else Color(0xFF1E293B)
            }

            // Skyscraper tower body
            drawRect(
                color = buildingColor,
                topLeft = Offset(bx, by),
                size = Size(bWidth, bHeight)
            )

            // Rooftop antenna spire
            if (b % 3 == 0) {
                val spireX = bx + bWidth * 0.5f
                drawLine(
                    color = Color(0xFF94A3B8),
                    start = Offset(spireX, by),
                    end = Offset(spireX, by - 16f),
                    strokeWidth = 2f
                )
                // Red aviation beacon
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 2.5f,
                    center = Offset(spireX, by - 16f)
                )
            }

            // Illuminated window matrix (warm amber & cyan windows)
            val windowRows = (bHeight / 9f).toInt()
            val windowCols = (bWidth / 7f).toInt().coerceAtLeast(2)

            for (r in 0 until windowRows) {
                for (c in 0 until windowCols) {
                    val isLightOn = ((b * 17 + r * 7 + c * 13) % 5 != 0)
                    if (isLightOn) {
                        val winX = bx + 3f + (c * 6.5f)
                        val winY = by + 4f + (r * 8.5f)
                        if (winX + 4f < bx + bWidth && winY + 5f < horizonY) {
                            val winColor = when ((b + r + c) % 4) {
                                0 -> Color(0xFFFEF08A) // Warm gold
                                1 -> Color(0xFF67E8F9) // Cyan glass
                                2 -> Color(0xFFFDE047) // Amber
                                else -> Color(0xFFFFFFFF) // Crisp white
                            }
                            val winAlpha = if (timeOfDay.isNight) 0.85f else 0.45f
                            drawRect(
                                color = winColor.copy(alpha = winAlpha),
                                topLeft = Offset(winX, winY),
                                size = Size(3.5f, 4.5f)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun DrawScope.drawBosphorusBridgeSilhouette(width: Float, horizonY: Float, timeOfDay: TimeOfDay) {
        val bridgeTowerX = width * 0.42f
        val towerHeight = 75f
        val towerColor = if (timeOfDay.isNight) Color(0xFFE11D48) else Color(0xFF1E293B) // Red illuminated towers at night

        // Tower A
        drawRect(
            color = towerColor,
            topLeft = Offset(bridgeTowerX - 8f, horizonY - towerHeight),
            size = Size(8f, towerHeight)
        )
        // Tower B
        drawRect(
            color = towerColor,
            topLeft = Offset(bridgeTowerX + 45f, horizonY - towerHeight),
            size = Size(8f, towerHeight)
        )
        // Suspension Main Cable
        val cablePath = Path().apply {
            moveTo(bridgeTowerX - 60f, horizonY - 10f)
            quadraticTo(bridgeTowerX + 22.5f, horizonY - 15f, bridgeTowerX + 110f, horizonY - 10f)
        }
        drawPath(path = cablePath, color = towerColor.copy(alpha = 0.6f), style = Stroke(width = 2f))

        // Minaret & Mosque dome silhouette in Istanbul
        val mosqueX = width * 0.14f
        drawOval(
            color = Color(0xFF1E293B),
            topLeft = Offset(mosqueX - 16f, horizonY - 28f),
            size = Size(32f, 28f)
        )
        drawRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(mosqueX - 22f, horizonY - 45f),
            size = Size(4f, 45f)
        )
        drawRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(mosqueX + 18f, horizonY - 45f),
            size = Size(4f, 45f)
        )
    }

    private fun DrawScope.drawPadmaBridgeSilhouette(width: Float, horizonY: Float, timeOfDay: TimeOfDay) {
        val bridgeX = width * 0.35f
        val pierColor = if (timeOfDay.isNight) Color(0xFF00E5FF) else Color(0xFF334155) // Cyan architectural illumination at night

        // Grand Two-Tier Truss Arches
        for (i in 0..3) {
            val pierX = bridgeX + (i * 45f)
            drawRect(
                color = pierColor,
                topLeft = Offset(pierX, horizonY - 35f),
                size = Size(7f, 35f)
            )
            // Cable Stay Fan lines
            drawLine(
                color = pierColor.copy(alpha = 0.6f),
                start = Offset(pierX + 3.5f, horizonY - 35f),
                end = Offset(pierX - 20f, horizonY - 8f),
                strokeWidth = 1.5f
            )
            drawLine(
                color = pierColor.copy(alpha = 0.6f),
                start = Offset(pierX + 3.5f, horizonY - 35f),
                end = Offset(pierX + 25f, horizonY - 8f),
                strokeWidth = 1.5f
            )
        }

        // River water reflective band
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0284C7).copy(alpha = 0.5f), Color(0xFF0369A1).copy(alpha = 0.8f)),
                startY = horizonY - 6f,
                endY = horizonY
            ),
            topLeft = Offset(width * 0.2f, horizonY - 6f),
            size = Size(width * 0.6f, 6f)
        )
    }

    private fun DrawScope.drawCurvilinearRoad(
        width: Float,
        height: Float,
        horizonY: Float,
        country: CountryRoute,
        weather: WeatherCondition,
        timeOfDay: TimeOfDay,
        physics: VehiclePhysicsEngine,
        headlightsMode: Int,
        activeCityName: String,
        nextCityName: String
    ) {
        val roadSegments = 50
        val baseRoadWidthBottom = width * 0.90f
        val baseRoadWidthTop = width * 0.07f

        // Nighttime atmospheric depth scrim while preserving city landmark lights
        if (timeOfDay.isNight) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFF030712).copy(alpha = 0.35f)),
                    startY = horizonY,
                    endY = height
                ),
                topLeft = Offset(0f, horizonY),
                size = Size(width, height - horizonY)
            )
        }

        // Reverse-safe continuous fractional scroll offset from car motion
        val scrollOffset = ((physics.roadSegmentIndex % 1.0f) + 1.0f) % 1.0f

        // Precompute segments from horizon (depth=1f) to camera foreground (depth=0f)
        var prevLeft = 0f
        var prevRight = 0f
        var prevY = horizonY

        for (i in roadSegments downTo 1) {
            // Apply continuous sub-segment scrollOffset so lines and curbs glide fluidly
            val segmentPos = i.toFloat() - scrollOffset
            val depth = (segmentPos / roadSegments).coerceIn(0.001f, 1f)
            val y = horizonY + (height - horizonY) * ((1f - depth) * (1f - depth))

            // Curve & harmonized lateral shift (-0.12f road shift paired with +0.18f car shift)
            val curveFactor = (1f - depth) * (1f - depth)
            val currentRoadCenterX = (width / 2f) + (physics.roadCurveOffset * width * 0.45f * curveFactor) - (physics.carX * width * 0.12f * (1f - depth))

            val halfWidth = (baseRoadWidthTop + (baseRoadWidthBottom - baseRoadWidthTop) * (1f - depth)) / 2f
            val roadLeft = currentRoadCenterX - halfWidth
            val roadRight = currentRoadCenterX + halfWidth

            if (i < roadSegments) {
                // Reverse-safe alternating rumble curbs & asphalt segments
                val rawCycle = physics.roadSegmentIndex.toInt() + i
                val segmentCycle = ((rawCycle % 160) + 160) % 160
                val isAltCurb = (segmentCycle % 2 == 0)

                // 1. Feathered Translucent Roadside Shoulders (keeps real city scenery visible!)
                val curbWidth = (roadRight - roadLeft) * 0.045f
                val terrainColor = if (country.type == CountryType.BANGLADESH) {
                    if (isAltCurb) Color(0xFF064E3B) else Color(0xFF042F2E)
                } else {
                    if (isAltCurb) Color(0xFF1E3A1E) else Color(0xFF142414)
                }

                // Left shoulder fades softly into real city backdrop
                val leftEdge = (roadLeft - curbWidth).coerceAtLeast(1f)
                drawPath(
                    path = Path().apply {
                        moveTo(0f, prevY)
                        lineTo(prevLeft - curbWidth, prevY)
                        lineTo(roadLeft - curbWidth, y)
                        lineTo(0f, y)
                        close()
                    },
                    brush = Brush.horizontalGradient(
                        colors = listOf(terrainColor.copy(alpha = 0.05f), terrainColor.copy(alpha = 0.56f)),
                        startX = 0f,
                        endX = leftEdge
                    )
                )
                // Right shoulder fades softly into real city backdrop
                val rightStart = (roadRight + curbWidth).coerceAtMost(width - 1f)
                drawPath(
                    path = Path().apply {
                        moveTo(prevRight + curbWidth, prevY)
                        lineTo(width, prevY)
                        lineTo(width, y)
                        lineTo(roadRight + curbWidth, y)
                        close()
                    },
                    brush = Brush.horizontalGradient(
                        colors = listOf(terrainColor.copy(alpha = 0.56f), terrainColor.copy(alpha = 0.05f)),
                        startX = rightStart,
                        endX = width
                    )
                )

                // 2. Grounded Semi-Translucent Multi-Lane Highway Asphalt Pavement
                val asphaltPath = Path().apply {
                    moveTo(prevLeft, prevY)
                    lineTo(prevRight, prevY)
                    lineTo(roadRight, y)
                    lineTo(roadLeft, y)
                    close()
                }
                val baseAsphalt = if (isAltCurb) Color(0xFF1E232B) else Color(0xFF14171E)
                drawPath(path = asphaltPath, color = baseAsphalt.copy(alpha = 0.80f))

                // Wet Road Reflection / Specular Sheen (when raining)
                if (weather.wetRoadReflection > 0.2f) {
                    val wetSheen = Color(0xFF38BDF8).copy(
                        alpha = 0.14f + (sin((physics.roadSegmentIndex * 0.05f) + i * 0.4f) * 0.05f)
                    )
                    drawPath(path = asphaltPath, color = wetSheen)
                }

                // 2b. Painted White Road Speed Stencil ("100" / "80" on asphalt)
                if (segmentCycle % 16 == 0 && depth < 0.82f) {
                    val markWidth = (roadRight - roadLeft) * 0.08f
                    val markHeight = (1f - depth) * 10f + 2f
                    val roadCenter = (roadLeft + roadRight) / 2f
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.70f),
                        topLeft = Offset(roadCenter - markWidth / 2f, y - markHeight),
                        size = Size(markWidth, markHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                    )
                }

                // 2c. Painted White Forward Directional Arrows ( ⇡ ) on Highway Lanes
                if (segmentCycle % 8 == 4 && depth < 0.78f) {
                    val arrowWidth = (roadRight - roadLeft) * 0.035f
                    val arrowLen = (1f - depth) * 14f + 3f
                    val leftLaneCenter = (roadLeft * 0.75f + roadRight * 0.25f)
                    val rightLaneCenter = (roadLeft * 0.25f + roadRight * 0.75f)

                    // Left lane arrow
                    drawLine(
                        color = Color.White.copy(alpha = 0.65f),
                        start = Offset(leftLaneCenter, y),
                        end = Offset(leftLaneCenter, y - arrowLen),
                        strokeWidth = (1f - depth) * 2.5f + 1f
                    )
                    // Right lane arrow
                    drawLine(
                        color = Color.White.copy(alpha = 0.65f),
                        start = Offset(rightLaneCenter, y),
                        end = Offset(rightLaneCenter, y - arrowLen),
                        strokeWidth = (1f - depth) * 2.5f + 1f
                    )
                }

                // 2d. Roadside 3D City Highway Billboards (Passing on left shoulder)
                if (segmentCycle % 20 == 10 && depth < 0.85f) {
                    val boardWidth = (1f - depth) * 85f + 18f
                    val boardHeight = boardWidth * 0.52f
                    val boardPoleX = roadLeft - curbWidth * 2.8f
                    val boardTopY = y - boardHeight - ((1f - depth) * 35f)

                    // Steel billboard pillar
                    drawLine(
                        color = Color(0xFF475569),
                        start = Offset(boardPoleX, y),
                        end = Offset(boardPoleX, boardTopY),
                        strokeWidth = (1f - depth) * 4f + 1.5f
                    )
                    // Billboard display board
                    drawRoundRect(
                        color = if (country.type == CountryType.BANGLADESH) Color(0xFF065F46) else Color(0xFF1E3A8A),
                        topLeft = Offset(boardPoleX - boardWidth * 0.7f, boardTopY),
                        size = Size(boardWidth, boardHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                    )
                    // White border
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(boardPoleX - boardWidth * 0.7f, boardTopY),
                        size = Size(boardWidth, boardHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
                        style = Stroke(width = 1.5f)
                    )
                }

                // 2. Striped Rumble Curbs / City Sidewalk Borders (Left & Right)
                val curbColor = if (isAltCurb) country.curbColorA else country.curbColorB

                // Left curb
                drawPath(
                    path = Path().apply {
                        moveTo(prevLeft - curbWidth, prevY)
                        lineTo(prevLeft, prevY)
                        lineTo(roadLeft, y)
                        lineTo(roadLeft - curbWidth, y)
                        close()
                    },
                    color = curbColor
                )

                // Right curb
                drawPath(
                    path = Path().apply {
                        moveTo(prevRight, prevY)
                        lineTo(prevRight + curbWidth, prevY)
                        lineTo(roadRight + curbWidth, y)
                        lineTo(roadRight, y)
                        close()
                    },
                    color = curbColor
                )

                // 3. Modern Highway Crash Barriers / Guardrails (Metallic rail with reflective posts)
                val guardrailHeight = (1f - depth) * 14f
                val barrierColor = if (timeOfDay.isNight) Color(0xFF475569) else Color(0xFF94A3B8)

                // Left metallic guardrail beam
                drawLine(
                    color = barrierColor,
                    start = Offset(prevLeft - curbWidth * 1.2f, prevY - guardrailHeight * 0.8f),
                    end = Offset(roadLeft - curbWidth * 1.2f, y - guardrailHeight),
                    strokeWidth = (1f - depth) * 3f + 1f
                )

                // Right metallic guardrail beam
                drawLine(
                    color = barrierColor,
                    start = Offset(prevRight + curbWidth * 1.2f, prevY - guardrailHeight * 0.8f),
                    end = Offset(roadRight + curbWidth * 1.2f, y - guardrailHeight),
                    strokeWidth = (1f - depth) * 3f + 1f
                )

                // Guardrail reflective posts every 4 segments
                if (segmentCycle % 4 == 0) {
                    val postColor = if (isAltCurb) Color(0xFFF97316) else Color.White
                    drawCircle(
                        color = postColor,
                        radius = (1f - depth) * 2.5f + 1f,
                        center = Offset(roadLeft - curbWidth * 1.2f, y - guardrailHeight)
                    )
                    drawCircle(
                        color = postColor,
                        radius = (1f - depth) * 2.5f + 1f,
                        center = Offset(roadRight + curbWidth * 1.2f, y - guardrailHeight)
                    )
                }

                // 4. Illuminated City Streetlights (High-Mast LED Poles) passing past the road
                if (segmentCycle % 8 == 0 && depth < 0.85f) {
                    val poleHeight = (1f - depth) * 55f + 8f
                    val poleWidth = (1f - depth) * 2.5f + 1f
                    val lampArmLen = (1f - depth) * 18f + 4f
                    val poleBaseX = roadRight + curbWidth * 2.2f
                    val poleBaseY = y

                    // Pole Mast
                    drawLine(
                        color = Color(0xFF64748B),
                        start = Offset(poleBaseX, poleBaseY),
                        end = Offset(poleBaseX, poleBaseY - poleHeight),
                        strokeWidth = poleWidth
                    )
                    // Arched Arm bending over highway
                    drawLine(
                        color = Color(0xFF64748B),
                        start = Offset(poleBaseX, poleBaseY - poleHeight),
                        end = Offset(poleBaseX - lampArmLen, poleBaseY - poleHeight + 4f),
                        strokeWidth = poleWidth
                    )
                    // Streetlamp LED Lantern & Road Light Cone
                    val lampX = poleBaseX - lampArmLen
                    val lampY = poleBaseY - poleHeight + 4f
                    val lightColor = if (timeOfDay.isNight) Color(0xFFFEF08A) else Color.White
                    val lightRadius = (1f - depth) * 5f + 1.5f

                    drawCircle(
                        color = lightColor,
                        radius = lightRadius,
                        center = Offset(lampX, lampY)
                    )

                    // Night road illumination pool on road surface
                    if (timeOfDay.isNight) {
                        drawOval(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFEF08A).copy(alpha = 0.25f * (1f - depth)), Color.Transparent),
                                center = Offset(lampX, poleBaseY),
                                radius = (1f - depth) * 45f + 10f
                            ),
                            topLeft = Offset(lampX - ((1f - depth) * 45f + 10f), poleBaseY - 8f),
                            size = Size(((1f - depth) * 90f + 20f), 16f)
                        )
                    }
                }

                // 4b. Roadside Trees / Palms passing along the road shoulder
                if (segmentCycle % 6 == 3 && depth < 0.88f) {
                    val treeHeight = (1f - depth) * 65f + 10f
                    val treeBaseX = roadLeft - curbWidth * 2.5f
                    val treeBaseY = y

                    if (country.type == CountryType.BANGLADESH) {
                        // Tropical Bengal Coconut Palm
                        drawLine(
                            color = Color(0xFF78350F),
                            start = Offset(treeBaseX, treeBaseY),
                            end = Offset(treeBaseX - ((1f - depth) * 8f), treeBaseY - treeHeight),
                            strokeWidth = (1f - depth) * 4f + 1.5f
                        )
                        val palmCrownY = treeBaseY - treeHeight
                        val frondRadius = (1f - depth) * 22f + 5f
                        val leafColor = if (timeOfDay.isNight) Color(0xFF064E3B) else Color(0xFF10B981)
                        drawCircle(
                            color = leafColor,
                            radius = frondRadius,
                            center = Offset(treeBaseX - ((1f - depth) * 8f), palmCrownY)
                        )
                    } else {
                        // Mediterranean / Anatolian Cypress
                        val trunkHeight = treeHeight * 0.3f
                        drawLine(
                            color = Color(0xFF451A03),
                            start = Offset(treeBaseX, treeBaseY),
                            end = Offset(treeBaseX, treeBaseY - trunkHeight),
                            strokeWidth = (1f - depth) * 4f + 1.5f
                        )
                        val foliageTop = treeBaseY - treeHeight
                        val foliageWidth = (1f - depth) * 20f + 6f
                        val cypressColor = if (timeOfDay.isNight) Color(0xFF064E3B) else Color(0xFF047857)
                        val treePath = Path().apply {
                            moveTo(treeBaseX, foliageTop)
                            lineTo(treeBaseX - foliageWidth * 0.5f, treeBaseY - trunkHeight)
                            lineTo(treeBaseX + foliageWidth * 0.5f, treeBaseY - trunkHeight)
                            close()
                        }
                        drawPath(path = treePath, color = cypressColor)
                    }
                }

                // 5. Dashed Lane Lines & Glowing Retro-Reflective Cats-Eyes
                val laneLineWidth = (roadRight - roadLeft) * 0.018f
                val centerLineX = (roadLeft + roadRight) / 2f
                val prevCenterLineX = (prevLeft + prevRight) / 2f

                if (segmentCycle % 4 < 2) {
                    // Center dash
                    drawLine(
                        color = Color.White.copy(alpha = if (timeOfDay.isNight) 0.95f else 0.85f),
                        start = Offset(prevCenterLineX, prevY),
                        end = Offset(centerLineX, y),
                        strokeWidth = laneLineWidth
                    )

                    // Secondary Lane Dividers for 4-lane city highway
                    val leftLaneX = (roadLeft + centerLineX) / 2f
                    val prevLeftLaneX = (prevLeft + prevCenterLineX) / 2f
                    drawLine(
                        color = Color.White.copy(alpha = 0.7f),
                        start = Offset(prevLeftLaneX, prevY),
                        end = Offset(leftLaneX, y),
                        strokeWidth = laneLineWidth * 0.8f
                    )

                    val rightLaneX = (centerLineX + roadRight) / 2f
                    val prevRightLaneX = (prevCenterLineX + prevRight) / 2f
                    drawLine(
                        color = Color.White.copy(alpha = 0.7f),
                        start = Offset(prevRightLaneX, prevY),
                        end = Offset(rightLaneX, y),
                        strokeWidth = laneLineWidth * 0.8f
                    )
                }

                // Glowing Retro-Reflective Road Studs (Cat's Eyes) that flash past!
                if (segmentCycle % 4 == 0) {
                    val studRadius = (1f - depth) * 3f + 1f
                    val studGlowColor = if (timeOfDay.isNight) Color(0xFFFDE047) else Color.White
                    // Center white stud
                    drawCircle(
                        color = studGlowColor,
                        radius = studRadius,
                        center = Offset(centerLineX, y)
                    )
                    // Left edge amber stud
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = studRadius * 0.9f,
                        center = Offset(roadLeft, y)
                    )
                    // Right edge amber stud
                    drawCircle(
                        color = Color(0xFFF59E0B),
                        radius = studRadius * 0.9f,
                        center = Offset(roadRight, y)
                    )
                }
            }

            prevLeft = roadLeft
            prevRight = roadRight
            prevY = y
        }

        // Draw Overhead Country Highway Gantry Sign periodically
        drawOverheadHighwaySign(width, height, horizonY, country, physics, activeCityName, nextCityName)
    }

    private fun DrawScope.drawOverheadHighwaySign(
        width: Float,
        height: Float,
        horizonY: Float,
        country: CountryRoute,
        physics: VehiclePhysicsEngine,
        activeCityName: String,
        nextCityName: String
    ) {
        val signCycle = (((physics.roadSegmentIndex * 0.008f) % 1.0f) + 1.0f) % 1.0f
        if (signCycle > 0.40f && signCycle < 0.95f) {
            val t = (signCycle - 0.40f) / 0.55f // 0f (distant) to 1f (overhead)
            val depth = 1f - (0.35f + t * 0.62f).coerceIn(0.05f, 0.97f)
            val invDepth = 1f - depth
            val roadGroundY = horizonY + (height - horizonY) * (invDepth * invDepth)
            val curveFactor = invDepth * invDepth

            val gantryCenterX = (width / 2f) + (physics.roadCurveOffset * width * 0.45f * curveFactor) - (physics.carX * width * 0.12f * invDepth)
            val baseRoadWidthBottom = width * 0.90f
            val baseRoadWidthTop = width * 0.07f
            val halfRoadWidthAtGantry = (baseRoadWidthTop + (baseRoadWidthBottom - baseRoadWidthTop) * invDepth) / 2f

            val signWidth = (halfRoadWidthAtGantry * 1.95f).coerceIn(width * 0.28f, width * 0.90f)
            val signHeight = signWidth * 0.24f
            val pillarHeight = signHeight * 1.55f + (invDepth * 34f)
            val signY = (roadGroundY - pillarHeight - signHeight).coerceAtLeast(horizonY * 0.45f)
            val signX = gantryCenterX - signWidth / 2f

            // Steel truss gantry support pillars anchored cleanly to road shoulders at depth Y
            val pillarColor = Color(0xFF64748B)
            val pillarThickness = 3f + invDepth * 5f
            val leftPillarX = (gantryCenterX - halfRoadWidthAtGantry * 1.05f).coerceAtMost(signX + 4f)
            val rightPillarX = (gantryCenterX + halfRoadWidthAtGantry * 1.05f).coerceAtLeast(signX + signWidth - 4f)
            drawLine(
                color = pillarColor,
                start = Offset(leftPillarX, roadGroundY),
                end = Offset(leftPillarX, signY + signHeight * 0.5f),
                strokeWidth = pillarThickness
            )
            drawLine(
                color = pillarColor,
                start = Offset(rightPillarX, roadGroundY),
                end = Offset(rightPillarX, signY + signHeight * 0.5f),
                strokeWidth = pillarThickness
            )
            // Overhead crossbeam truss
            drawLine(
                color = pillarColor,
                start = Offset(leftPillarX - 4f, signY + 2f),
                end = Offset(rightPillarX + 4f, signY + 2f),
                strokeWidth = pillarThickness * 1.1f
            )

            // Green Highway Gantry Signboard (RHD Bangladesh or Karayolları Turkey)
            val boardColor = if (country.type == CountryType.BANGLADESH) Color(0xFF065F46) else Color(0xFF15803D)
            val cornerPx = 6f * (0.5f + t * 0.5f)
            drawRoundRect(
                color = boardColor,
                topLeft = Offset(signX, signY),
                size = Size(signWidth, signHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx)
            )
            // Reflective white outer border
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(signX, signY),
                size = Size(signWidth, signHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerPx, cornerPx),
                style = Stroke(width = 2.2f * (0.5f + t * 0.5f))
            )

            // Route Code Badge (N1 or O-4)
            val badgeWidth = signWidth * 0.22f
            val badgeHeight = signHeight * 0.34f
            val badgeLeft = signX + signWidth * 0.03f
            val badgeTop = signY + signHeight * 0.12f
            drawRoundRect(
                color = if (country.type == CountryType.TURKEY) Color(0xFF1D4ED8) else Color(0xFF047857),
                topLeft = Offset(badgeLeft, badgeTop),
                size = Size(badgeWidth, badgeHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
            )

            // Speed Limit Circle Pill ( [100] or [80] )
            val speedCircleRadius = signHeight * 0.24f
            val speedCenterX = signX + signWidth - speedCircleRadius - (signWidth * 0.04f)
            val speedCenterY = signY + signHeight * 0.42f
            drawCircle(
                color = Color.White,
                radius = speedCircleRadius,
                center = Offset(speedCenterX, speedCenterY)
            )
            drawCircle(
                color = Color(0xFFDC2626),
                radius = speedCircleRadius,
                center = Offset(speedCenterX, speedCenterY),
                style = Stroke(width = 2f + t * 1.5f)
            )

            // City Route Text & Route Code via cached native Paint
            val routeLabel = if (country.type == CountryType.TURKEY) "O-4 / E80" else "N1 / AH41"
            val speedLabel = if (country.type == CountryType.TURKEY) "120" else "80"
            val cityLabel = if (nextCityName.isNotBlank()) {
                "${activeCityName.uppercase()} ➔ ${nextCityName.uppercase()}"
            } else {
                activeCityName.uppercase()
            }

            gantryRoutePaint.textSize = (badgeHeight * 0.58f).coerceAtLeast(8f)
            gantryCityPaint.textSize = (signHeight * 0.25f).coerceAtLeast(9f)
            gantrySpeedPaint.textSize = (speedCircleRadius * 0.95f).coerceAtLeast(8f)

            drawContext.canvas.nativeCanvas.drawText(
                routeLabel,
                badgeLeft + badgeWidth * 0.10f,
                badgeTop + badgeHeight * 0.72f,
                gantryRoutePaint
            )
            drawContext.canvas.nativeCanvas.drawText(
                cityLabel,
                signX + signWidth * 0.50f,
                signY + signHeight * 0.58f,
                gantryCityPaint
            )
            drawContext.canvas.nativeCanvas.drawText(
                speedLabel,
                speedCenterX,
                speedCenterY + (gantrySpeedPaint.textSize * 0.35f),
                gantrySpeedPaint
            )

            // Downward Lane Arrows (Pointing to highway lanes)
            val arrowSpacing = signWidth / 4f
            for (lane in 1..3) {
                val arrowX = signX + (lane * arrowSpacing)
                val arrowY = signY + signHeight * 0.88f
                val arrowLen = signHeight * 0.16f
                drawLine(
                    color = Color.White,
                    start = Offset(arrowX, arrowY - arrowLen),
                    end = Offset(arrowX, arrowY),
                    strokeWidth = 2.5f * (0.5f + t * 0.5f),
                    cap = StrokeCap.Round
                )
            }
        }
    }

    private fun DrawScope.drawTrafficVehicles(
        width: Float,
        height: Float,
        horizonY: Float,
        country: CountryRoute,
        weather: WeatherCondition,
        timeOfDay: TimeOfDay,
        physics: VehiclePhysicsEngine
    ) {
        val baseRoadWidthBottom = width * 0.90f
        val baseRoadWidthTop = width * 0.07f

        for (traffic in physics.trafficVehicles) {
            val dist = traffic.roadSegmentIndex
            if (dist < 2f || dist > 200f) continue

            // Compute depth: 0f is very close to player, 1f is at horizon
            val depth = (dist / 200f).coerceIn(0.01f, 1f)
            val invDepth = 1f - depth
            val y = horizonY + (height - horizonY) * (invDepth * invDepth)

            val curveFactor = invDepth * invDepth
            val currentRoadCenterX = (width / 2f) + (physics.roadCurveOffset * width * 0.45f * curveFactor) - (physics.carX * width * 0.12f * invDepth)
            val halfRoadWidth = (baseRoadWidthTop + (baseRoadWidthBottom - baseRoadWidthTop) * invDepth) / 2f

            // Position traffic car within lane (harmonized with player physics)
            val trafficX = currentRoadCenterX + (traffic.lateralOffset * halfRoadWidth * 0.65f)

            // Vehicle Category: 0=City Taxi/CNG, 1=City Express Bus/Coach, 2=Sedan
            val vehicleType = (traffic.id % 3L).toInt()
            val carScale = (invDepth * invDepth).coerceIn(0.06f, 1.15f)

            val (carWidth, carHeight) = when (vehicleType) {
                0 -> {
                    // CNG or Taxi
                    if (country.type == CountryType.BANGLADESH) Pair(width * 0.17f * carScale, width * 0.17f * carScale * 0.85f)
                    else Pair(width * 0.21f * carScale, width * 0.21f * carScale * 0.58f)
                }
                1 -> {
                    // Express Bus or Luxury Coach
                    Pair(width * 0.25f * carScale, width * 0.25f * carScale * 0.85f)
                }
                else -> {
                    // Sedan
                    Pair(width * 0.22f * carScale, width * 0.22f * carScale * 0.60f)
                }
            }

            val left = trafficX - carWidth / 2f
            val top = y - carHeight

            // Traffic Car Ground Shadow
            drawOval(
                color = Color.Black.copy(alpha = 0.50f),
                topLeft = Offset(left, y - carHeight * 0.15f),
                size = Size(carWidth, carHeight * 0.35f)
            )

            // Distinct City Vehicle Rendering
            when {
                country.type == CountryType.BANGLADESH && vehicleType == 0 -> {
                    // Green CNG Auto-Rickshaw (Dhaka & Chittagong icon)
                    drawRoundRect(
                        color = Color(0xFF059669), // Green body
                        topLeft = Offset(left, top + carHeight * 0.25f),
                        size = Size(carWidth, carHeight * 0.75f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * carScale, 4f * carScale)
                    )
                    // Yellow/black canvas roof
                    drawRoundRect(
                        color = Color(0xFF0F172A),
                        topLeft = Offset(left + carWidth * 0.08f, top),
                        size = Size(carWidth * 0.84f, carHeight * 0.35f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * carScale, 6f * carScale)
                    )
                    // Yellow Dhaka/Ctg registration plate
                    drawRoundRect(
                        color = Color(0xFFFACC15),
                        topLeft = Offset(left + carWidth * 0.32f, top + carHeight * 0.82f),
                        size = Size(carWidth * 0.36f, carHeight * 0.14f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5f, 1.5f)
                    )
                }
                country.type == CountryType.TURKEY && vehicleType == 0 -> {
                    // Istanbul Yellow Taxi ("TAKSİ")
                    drawRoundRect(
                        color = Color(0xFFEAB308), // Bright Istanbul Taxi yellow
                        topLeft = Offset(left, top),
                        size = Size(carWidth, carHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(5f * carScale, 5f * carScale)
                    )
                    // Tinted Cabin Glass
                    drawRoundRect(
                        color = Color(0xFF0F172A),
                        topLeft = Offset(left + carWidth * 0.14f, top + carHeight * 0.12f),
                        size = Size(carWidth * 0.72f, carHeight * 0.42f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * carScale, 3f * carScale)
                    )
                    // Illuminated Yellow Roof TAKSİ Lightbar
                    val roofLightWidth = carWidth * 0.34f
                    val roofLightHeight = carHeight * 0.16f
                    drawRoundRect(
                        color = Color(0xFFFEF08A),
                        topLeft = Offset(left + (carWidth - roofLightWidth) / 2f, top - roofLightHeight * 0.7f),
                        size = Size(roofLightWidth, roofLightHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                    )
                }
                vehicleType == 1 -> {
                    // Express Bus / Luxury Coach
                    val busBaseColor = if (country.type == CountryType.BANGLADESH) Color(0xFF047857) else Color(0xFFE2E8F0)
                    drawRoundRect(
                        color = busBaseColor,
                        topLeft = Offset(left, top),
                        size = Size(carWidth, carHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * carScale, 4f * carScale)
                    )
                    // Panoramic rear window
                    drawRoundRect(
                        color = Color(0xFF020617),
                        topLeft = Offset(left + carWidth * 0.08f, top + carHeight * 0.10f),
                        size = Size(carWidth * 0.84f, carHeight * 0.40f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                    )
                    // Vibrant decorative livery belt
                    val beltColor = if (country.type == CountryType.BANGLADESH) Color(0xFFF59E0B) else Color(0xFFDC2626)
                    drawRect(
                        color = beltColor,
                        topLeft = Offset(left, top + carHeight * 0.55f),
                        size = Size(carWidth, carHeight * 0.10f)
                    )
                }
                else -> {
                    // Standard Sedan / SUV
                    drawRoundRect(
                        color = traffic.baseColor,
                        topLeft = Offset(left, top),
                        size = Size(carWidth, carHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * carScale, 6f * carScale)
                    )
                    drawRoundRect(
                        color = Color(0xFF0F172A),
                        topLeft = Offset(left + carWidth * 0.15f, top + carHeight * 0.15f),
                        size = Size(carWidth * 0.7f, carHeight * 0.4f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f * carScale, 4f * carScale)
                    )
                }
            }

            // Taillights (red) with dynamic braking glow
            val isBraking = traffic.isBraking || (physics.speedKmH > traffic.speedKmH && dist < 30f)
            val lightColor = if (isBraking) Color(0xFFFF1744) else Color(0xFFDC2626)
            val lightSize = carWidth * 0.18f

            drawRoundRect(
                color = lightColor,
                topLeft = Offset(left + carWidth * 0.08f, top + carHeight * 0.65f),
                size = Size(lightSize, carHeight * 0.22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
            drawRoundRect(
                color = lightColor,
                topLeft = Offset(left + carWidth * 0.74f, top + carHeight * 0.65f),
                size = Size(lightSize, carHeight * 0.22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )

            // Night taillight road glow
            if (timeOfDay.isNight) {
                drawCircle(
                    color = lightColor.copy(alpha = 0.5f),
                    radius = lightSize * 1.6f,
                    center = Offset(left + carWidth * 0.17f, top + carHeight * 0.76f)
                )
                drawCircle(
                    color = lightColor.copy(alpha = 0.5f),
                    radius = lightSize * 1.6f,
                    center = Offset(left + carWidth * 0.83f, top + carHeight * 0.76f)
                )
            }
        }
    }

    private fun DrawScope.drawWeatherOverlays(
        width: Float,
        height: Float,
        weather: WeatherCondition,
        timeOfDay: TimeOfDay,
        headlightsMode: Int,
        physics: VehiclePhysicsEngine,
        timeSeconds: Float
    ) {
        val speedKmH = physics.speedKmH

        // 1. Volumetric Headlight Beams aligned with Car Steering & Altitude
        if (headlightsMode > 0) {
            val beamAlpha = when {
                timeOfDay.isNight -> if (headlightsMode == 2) 0.45f else 0.30f
                weather == WeatherCondition.DENSE_MIST -> 0.40f
                weather == WeatherCondition.MONSOON_RAIN -> 0.25f
                else -> 0.12f
            }

            val carLateralShift = (physics.carX * width * 0.18f).coerceIn(-width * 0.32f, width * 0.32f)
            val carCenterX = width / 2f + carLateralShift
            val curveLookOffset = physics.roadCurveOffset * width * 0.12f
            val beamBottomY = (height * 0.78f - physics.flightAltitude * 1.8f).coerceIn(height * 0.48f, height * 0.84f)
            val beamTopY = height * 0.45f

            // Left headlight cone
            val leftBeamPath = Path().apply {
                moveTo(carCenterX - width * 0.12f, beamBottomY)
                lineTo(carCenterX - width * 0.05f, beamBottomY)
                lineTo(carCenterX - width * 0.14f + curveLookOffset, beamTopY)
                lineTo(carCenterX - width * 0.30f + curveLookOffset, beamTopY)
                close()
            }
            drawPath(
                path = leftBeamPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFFFEF08A).copy(alpha = beamAlpha)),
                    startY = beamTopY,
                    endY = beamBottomY
                )
            )

            // Right headlight cone
            val rightBeamPath = Path().apply {
                moveTo(carCenterX + width * 0.05f, beamBottomY)
                lineTo(carCenterX + width * 0.12f, beamBottomY)
                lineTo(carCenterX + width * 0.30f + curveLookOffset, beamTopY)
                lineTo(carCenterX + width * 0.14f + curveLookOffset, beamTopY)
                close()
            }
            drawPath(
                path = rightBeamPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFFFEF08A).copy(alpha = beamAlpha)),
                    startY = beamTopY,
                    endY = beamBottomY
                )
            )
        }

        // 2. Dense Mist / Fog Layer
        if (weather.fogDensity > 0.1f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFCBD5E1).copy(alpha = weather.fogDensity * 0.65f),
                        Color(0xFF94A3B8).copy(alpha = weather.fogDensity * 0.3f),
                        Color.Transparent
                    ),
                    startY = height * 0.35f,
                    endY = height * 0.85f
                ),
                size = Size(width, height)
            )
        }

        // 3. Monsoon Rain Streaks
        if (weather.rainIntensity > 0.05f) {
            val speedTilt = (speedKmH * 0.15f).coerceIn(0f, 25f)
            for (i in rainParticles.indices) {
                val rx = (rainParticles[i] * width + (timeSeconds * (180f + speedKmH * 2f))) % width
                val ry = ((rainParticles[(i + 13) % rainParticles.size] * height) + (timeSeconds * 1200f)) % height
                val streakLength = 25f + (weather.rainIntensity * 20f)

                drawLine(
                    color = Color(0xFFBAE6FD).copy(alpha = weather.rainIntensity * 0.7f),
                    start = Offset(rx, ry),
                    end = Offset(rx - speedTilt, ry + streakLength),
                    strokeWidth = 2.0f
                )
            }
        }

        // 4. Nitro Booster & High Speed Radial Warp Streaks
        if (speedKmH > 200f) {
            val boostAlpha = ((speedKmH - 200f) / 190f).coerceIn(0f, 0.7f)
            val centerX = width * 0.5f
            val centerY = height * 0.45f

            for (i in 0..15) {
                val angle = (i * (2f * PI.toFloat() / 16f)) + (timeSeconds * 6f)
                val innerDist = width * 0.25f
                val outerDist = width * 0.75f
                val cosA = cos(angle)
                val sinA = sin(angle)

                drawLine(
                    color = Color(0xFF00E5FF).copy(alpha = boostAlpha * (0.3f + (i % 3) * 0.25f)),
                    start = Offset(centerX + cosA * innerDist, centerY + sinA * innerDist),
                    end = Offset(centerX + cosA * outerDist, centerY + sinA * outerDist),
                    strokeWidth = 2.5f
                )
            }
        }
    }
}
