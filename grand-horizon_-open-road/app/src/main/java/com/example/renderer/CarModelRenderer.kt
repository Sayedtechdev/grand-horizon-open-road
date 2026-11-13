package com.example.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import com.example.model.CameraView
import com.example.model.CarColor
import com.example.model.CountryRoute
import com.example.physics.VehiclePhysicsEngine
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sin

class CarModelRenderer {
    private var wiperAngle = 0f
    private var wiperDirection = 1f

    private val cockpitSpeedPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        textAlign = android.graphics.Paint.Align.CENTER
    }
    private val cockpitSubPaint = android.graphics.Paint().apply {
        color = android.graphics.Color.parseColor("#38BDF8")
        isAntiAlias = true
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        textAlign = android.graphics.Paint.Align.CENTER
    }

    fun drawCar(
        drawScope: DrawScope,
        physics: VehiclePhysicsEngine,
        carColor: CarColor,
        cameraView: CameraView,
        headlightsMode: Int,
        isRaining: Boolean,
        country: CountryRoute,
        timeSeconds: Float
    ) {
        with(drawScope) {
            when (cameraView) {
                CameraView.CHASE -> drawChaseViewCar(physics, carColor, headlightsMode, timeSeconds)
                CameraView.COCKPIT -> drawCockpitView(physics, carColor, isRaining, timeSeconds)
                CameraView.DRONE -> drawDroneViewCar(physics, carColor, headlightsMode, timeSeconds)
            }
        }
    }

    private fun DrawScope.drawChaseViewCar(
        physics: VehiclePhysicsEngine,
        carColor: CarColor,
        headlightsMode: Int,
        timeSeconds: Float
    ) {
        // Lateral lane shift harmonized with RoadRenderer (-0.12f road shift + 0.18f car shift)
        val carLateralShift = (physics.carX * size.width * 0.18f).coerceIn(-size.width * 0.32f, size.width * 0.32f)
        val centerX = size.width / 2f + carLateralShift
        // Dynamic suspension: Squat under acceleration, dive under braking, and high-speed asphalt rumble
        val flightLift = physics.flightAltitude * 1.8f
        val accelerationSquat = if (physics.isAcceleratingPressed || physics.isBoosting) 5.5f else 0f
        val brakeDive = (physics.brakeInput * 8f) + (if (physics.isBrakingPressed) 5f else 0f)
        val highSpeedRumble = if (physics.speedKmH > 70f && !physics.isFlightMode) sin(timeSeconds * 45f) * 1.5f else 0f
        val carBaseY = size.height * 0.81f - flightLift + brakeDive - accelerationSquat + highSpeedRumble

        val carWidth = size.width * 0.32f
        val carHeight = carWidth * 0.52f

        // Dynamic roll/banking angle from steering & flight
        val rollAngle = physics.steeringInput * (if (physics.isFlightMode) 14f else 5.5f)

        // 1. High-Tech Cyber Underglow Neon Ground Illumination (Projected directly onto real road)
        val underglowAlpha = if (physics.flightAltitude > 40f) 0.1f else (0.75f + sin(timeSeconds * 8f) * 0.15f)
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(carColor.glowColor.copy(alpha = underglowAlpha), Color.Transparent),
                center = Offset(centerX, carBaseY + carHeight * 0.45f),
                radius = carWidth * 0.85f
            ),
            topLeft = Offset(centerX - carWidth * 0.85f, carBaseY + carHeight * 0.15f),
            size = Size(carWidth * 1.7f, carHeight * 0.65f)
        )

        // 2. Ground Shadow (firmly grounds car on asphalt, fades when flying high)
        if (physics.flightAltitude < 70f) {
            val shadowAlpha = ((70f - physics.flightAltitude) / 70f * 0.65f).coerceIn(0.08f, 0.65f)
            val shadowScale = (1f - physics.flightAltitude * 0.007f).coerceIn(0.5f, 1f)
            drawOval(
                color = Color.Black.copy(alpha = shadowAlpha),
                topLeft = Offset(centerX - (carWidth * 0.62f * shadowScale), carBaseY + carHeight * 0.42f),
                size = Size(carWidth * 1.24f * shadowScale, carHeight * 0.38f * shadowScale)
            )
        }

        // 3. Handbrake Drift Tire Smoke & Velocity Mist
        if (physics.driftSlip > 0.1f && !physics.isFlightMode) {
            val smokeAlpha = (physics.driftSlip * 0.65f).coerceIn(0f, 0.65f)
            val smokeOffset = if (physics.steeringInput > 0) -carWidth * 0.45f else carWidth * 0.45f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = smokeAlpha), Color.Transparent),
                    center = Offset(centerX + smokeOffset, carBaseY + carHeight * 0.3f),
                    radius = carWidth * 0.65f
                ),
                radius = carWidth * 0.65f,
                center = Offset(centerX + smokeOffset, carBaseY + carHeight * 0.3f)
            )
        }

        // 4. Render Advanced Cyber-Phantom Hypercar
        rotate(degrees = rollAngle, pivot = Offset(centerX, carBaseY)) {
            val left = centerX - carWidth / 2f
            val top = carBaseY - carHeight / 2f

            // AERO-FLIGHT MODE: Extended Wings & Plasma Ion Thrusters (Step 3)
            if (physics.isFlightMode) {
                drawAeroFlightWingsAndThrusters(
                    centerX = centerX,
                    carBaseY = carBaseY,
                    carWidth = carWidth,
                    carHeight = carHeight,
                    isBoosting = physics.isBoosting,
                    timeSeconds = timeSeconds
                )
            }

            // Wheels & Tires (Wide Hypercar Racing Slicks with Rotating Turbine Blades)
            val tireWidth = carWidth * 0.18f
            val tireHeight = carHeight * 0.44f
            val tireY = top + carHeight * 0.58f
            val wheelRotation = timeSeconds * (physics.speedKmH * 3.5f + 10f)

            // Distinct black tire contact patches directly grounding rubber to asphalt
            if (physics.flightAltitude < 25f) {
                val tirePatchAlpha = ((25f - physics.flightAltitude) / 25f * 0.85f).coerceIn(0f, 0.85f)
                drawOval(
                    color = Color.Black.copy(alpha = tirePatchAlpha),
                    topLeft = Offset(left + carWidth * 0.01f, tireY + tireHeight * 0.80f),
                    size = Size(tireWidth * 1.12f, tireHeight * 0.32f)
                )
                drawOval(
                    color = Color.Black.copy(alpha = tirePatchAlpha),
                    topLeft = Offset(left + carWidth * 0.79f, tireY + tireHeight * 0.80f),
                    size = Size(tireWidth * 1.12f, tireHeight * 0.32f)
                )
            }

            // Left Tire & Wheel
            drawRoundRect(
                color = Color(0xFF111827),
                topLeft = Offset(left + carWidth * 0.02f, tireY),
                size = Size(tireWidth, tireHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
            )
            // Left Wheel Rim & Rotating Turbine Blades
            val leftWheelCenter = Offset(left + carWidth * 0.11f, tireY + tireHeight * 0.5f)
            rotate(degrees = wheelRotation, pivot = leftWheelCenter) {
                drawCircle(
                    color = carColor.glowColor.copy(alpha = 0.85f),
                    radius = tireWidth * 0.45f,
                    center = leftWheelCenter,
                    style = Stroke(width = 2.5f)
                )
                for (spoke in 0..4) {
                    val angle = spoke * (PI.toFloat() * 2f / 5f)
                    drawLine(
                        color = carColor.rimColor,
                        start = leftWheelCenter,
                        end = Offset(leftWheelCenter.x + kotlin.math.cos(angle) * (tireWidth * 0.42f), leftWheelCenter.y + sin(angle) * (tireHeight * 0.42f)),
                        strokeWidth = 2.5f
                    )
                }
            }

            // Right Tire & Wheel
            drawRoundRect(
                color = Color(0xFF111827),
                topLeft = Offset(left + carWidth * 0.80f, tireY),
                size = Size(tireWidth, tireHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
            )
            // Right Wheel Rim & Rotating Turbine Blades
            val rightWheelCenter = Offset(left + carWidth * 0.89f, tireY + tireHeight * 0.5f)
            rotate(degrees = wheelRotation, pivot = rightWheelCenter) {
                drawCircle(
                    color = carColor.glowColor.copy(alpha = 0.85f),
                    radius = tireWidth * 0.45f,
                    center = rightWheelCenter,
                    style = Stroke(width = 2.5f)
                )
                for (spoke in 0..4) {
                    val angle = spoke * (PI.toFloat() * 2f / 5f)
                    drawLine(
                        color = carColor.rimColor,
                        start = rightWheelCenter,
                        end = Offset(rightWheelCenter.x + kotlin.math.cos(angle) * (tireWidth * 0.42f), rightWheelCenter.y + sin(angle) * (tireHeight * 0.42f)),
                        strokeWidth = 2.5f
                    )
                }
            }

            // Aerodynamic Rear Carbon Diffuser & Vortex Fins
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(left + carWidth * 0.18f, top + carHeight * 0.76f),
                size = Size(carWidth * 0.64f, carHeight * 0.26f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
            )
            // 4 Vertical Diffuser Strakes / Fins
            for (fin in 0..3) {
                val finX = left + carWidth * (0.24f + fin * 0.17f)
                drawLine(
                    color = Color(0xFF38BDF8),
                    start = Offset(finX, top + carHeight * 0.78f),
                    end = Offset(finX, top + carHeight * 0.98f),
                    strokeWidth = 3f
                )
            }

            // Main Sculpted Aerodynamic Hypercar Body Shell
            val bodyPath = Path().apply {
                moveTo(left + carWidth * 0.10f, top + carHeight * 0.90f) // bottom left
                lineTo(left + carWidth * 0.02f, top + carHeight * 0.60f) // wide flank flare
                lineTo(left + carWidth * 0.14f, top + carHeight * 0.32f) // shoulder intake
                lineTo(left + carWidth * 0.28f, top + carHeight * 0.04f) // roof canopy left
                lineTo(left + carWidth * 0.72f, top + carHeight * 0.04f) // roof canopy right
                lineTo(left + carWidth * 0.86f, top + carHeight * 0.32f) // shoulder intake
                lineTo(left + carWidth * 0.98f, top + carHeight * 0.60f) // wide flank flare
                lineTo(left + carWidth * 0.90f, top + carHeight * 0.90f) // bottom right
                close()
            }

            // Carbon-Metallic multi-tone paint
            val bodyBrush = Brush.verticalGradient(
                colors = listOf(
                    carColor.primaryColor,
                    carColor.accentColor,
                    Color(0xFF090D16)
                ),
                startY = top,
                endY = top + carHeight
            )
            drawPath(path = bodyPath, brush = bodyBrush)

            // Aerodynamic Central Shark Fin (LMP1 Racecar Fin)
            val sharkFinPath = Path().apply {
                moveTo(centerX, top + carHeight * 0.06f)
                lineTo(centerX, top + carHeight * 0.44f)
                lineTo(centerX + 3f, top + carHeight * 0.44f)
                lineTo(centerX + 3f, top + carHeight * 0.06f)
                close()
            }
            drawPath(path = sharkFinPath, color = Color(0xFF090D16))
            drawLine(
                color = carColor.glowColor,
                start = Offset(centerX + 1.5f, top + carHeight * 0.06f),
                end = Offset(centerX + 1.5f, top + carHeight * 0.44f),
                strokeWidth = 2f
            )

            // Cyberpunk Cockpit Canopy (Tinted Mirror-Finish Glass)
            val cabinPath = Path().apply {
                moveTo(left + carWidth * 0.30f, top + carHeight * 0.10f)
                lineTo(left + carWidth * 0.70f, top + carHeight * 0.10f)
                lineTo(left + carWidth * 0.78f, top + carHeight * 0.42f)
                lineTo(left + carWidth * 0.22f, top + carHeight * 0.42f)
                close()
            }
            drawPath(
                path = cabinPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF020617), Color(0xFF0F172A), Color(0xFF1E293B)),
                    startY = top,
                    endY = top + carHeight * 0.42f
                )
            )
            // Cyber HUD interior pilot glow
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.45f),
                radius = carWidth * 0.12f,
                center = Offset(centerX, top + carHeight * 0.26f)
            )

            // ACTIVE AERO SPOILER (AIRBRAKE & DRS MODES)
            val isBraking = physics.brakeInput > 0.05f || physics.isBrakingPressed
            val isBoosting = physics.isBoosting
            // When braking, the wing elevates high into vertical Airbrake position
            // When boosting, the wing drops flat into DRS (Drag Reduction System) mode
            val wingY = when {
                isBraking -> top - carHeight * 0.14f // Airbrake elevated!
                isBoosting -> top + carHeight * 0.08f // DRS flat wing!
                else -> top + carHeight * 0.01f // Standard downforce
            }
            val wingWidth = carWidth * 0.86f
            val wingLeft = centerX - wingWidth / 2f

            // Dual Carbon Wing Pylons / Struts
            drawLine(
                color = Color(0xFF18181B),
                start = Offset(left + carWidth * 0.28f, top + carHeight * 0.28f),
                end = Offset(left + carWidth * 0.28f, wingY + 10f),
                strokeWidth = 5f
            )
            drawLine(
                color = Color(0xFF18181B),
                start = Offset(left + carWidth * 0.72f, top + carHeight * 0.28f),
                end = Offset(left + carWidth * 0.72f, wingY + 10f),
                strokeWidth = 5f
            )

            // Wing Blade
            drawRoundRect(
                color = Color(0xFF090D16),
                topLeft = Offset(wingLeft, wingY),
                size = Size(wingWidth, if (isBraking) carHeight * 0.14f else carHeight * 0.08f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )
            // Wing Endplates in Primary Hypercar Color
            drawRoundRect(
                color = carColor.primaryColor,
                topLeft = Offset(wingLeft - 5f, wingY - 6f),
                size = Size(8f, carHeight * 0.18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )
            drawRoundRect(
                color = carColor.primaryColor,
                topLeft = Offset(wingLeft + wingWidth - 3f, wingY - 6f),
                size = Size(8f, carHeight * 0.18f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
            )

            // Active Airbrake Warning LED Lightbar (Flashes Red during hard braking)
            if (isBraking) {
                drawRoundRect(
                    color = Color(0xFFFF0033),
                    topLeft = Offset(wingLeft + 12f, wingY + 4f),
                    size = Size(wingWidth - 24f, 6f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
                )
            }

            // DRS High-Speed Wake Vortex Streamers (When boosting)
            if (isBoosting) {
                drawLine(
                    color = Color(0xFF38BDF8).copy(alpha = 0.8f),
                    start = Offset(wingLeft - 2f, wingY + 4f),
                    end = Offset(wingLeft - 30f, wingY - 20f),
                    strokeWidth = 2.5f
                )
                drawLine(
                    color = Color(0xFF38BDF8).copy(alpha = 0.8f),
                    start = Offset(wingLeft + wingWidth + 2f, wingY + 4f),
                    end = Offset(wingLeft + wingWidth + 30f, wingY - 20f),
                    strokeWidth = 2.5f
                )
            }

            // FULL-WIDTH CYBER OLED MATRIX TAILLIGHT with SEQUENTIAL TURN SIGNALS
            val taillightY = top + carHeight * 0.48f
            val taillightColor = if (isBraking) Color(0xFFFF0033) else Color(0xFFDC2626)

            // Taillight Neon Bloom
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(if (isBraking) Color(0xFFFF3366).copy(alpha = 0.9f) else Color(0xFFEF4444).copy(alpha = 0.45f), Color.Transparent),
                    center = Offset(centerX, taillightY + 4f),
                    radius = carWidth * 0.60f
                ),
                topLeft = Offset(left + carWidth * 0.05f, taillightY - 16f),
                size = Size(carWidth * 0.90f, 36f)
            )

            // Main OLED Lightbar
            drawRoundRect(
                color = taillightColor,
                topLeft = Offset(left + carWidth * 0.12f, taillightY),
                size = Size(carWidth * 0.76f, if (isBraking) 11f else 6f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            // Sequential Amber Turn Signals when steering
            val isSteeringLeft = physics.steeringInput < -0.2f
            val isSteeringRight = physics.steeringInput > 0.2f
            val turnBlink = ((timeSeconds * 8f).toInt() % 2 == 0)

            if (isSteeringLeft && turnBlink) {
                drawRoundRect(
                    color = Color(0xFFFACC15),
                    topLeft = Offset(left + carWidth * 0.12f, taillightY - 2f),
                    size = Size(carWidth * 0.24f, 9f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
            }
            if (isSteeringRight && turnBlink) {
                drawRoundRect(
                    color = Color(0xFFFACC15),
                    topLeft = Offset(left + carWidth * 0.64f, taillightY - 2f),
                    size = Size(carWidth * 0.24f, 9f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
                )
            }

            // Hypercar Central Model Emblem (Glowing Cyber Crystal)
            drawCircle(
                color = carColor.glowColor,
                radius = 6f,
                center = Offset(centerX, top + carHeight * 0.64f)
            )

            // QUAD TITANIUM PLASMA EXHAUST AFTERBURNERS
            val exhaustY = top + carHeight * 0.82f
            val exhaustWidth = carWidth * 0.08f
            val exhaustHeight = 12f

            val exhaustPositions = listOf(
                left + carWidth * 0.22f,
                left + carWidth * 0.32f,
                left + carWidth * 0.60f,
                left + carWidth * 0.70f
            )

            for (exX in exhaustPositions) {
                drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(exX, exhaustY),
                    size = Size(exhaustWidth, exhaustHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
                )
                drawRoundRect(
                    color = Color(0xFF67E8F9),
                    topLeft = Offset(exX + 1.5f, exhaustY + 2f),
                    size = Size(exhaustWidth - 3f, exhaustHeight - 4f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f),
                    style = Stroke(width = 1.5f)
                )

                // Animated Plasma Jet Fire Plumes (Dual-stage cyan & violet)
                if (physics.rpm > 5500f || isBoosting || physics.isAcceleratingPressed) {
                    val flameLength = (if (isBoosting) 48f else 24f) + (sin(timeSeconds * 30f + exX) * 6f)
                    drawOval(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF67E8F9), Color(0xFF818CF8), Color.Transparent),
                            startY = exhaustY + exhaustHeight,
                            endY = exhaustY + exhaustHeight + flameLength
                        ),
                        topLeft = Offset(exX + 1f, exhaustY + exhaustHeight),
                        size = Size(exhaustWidth - 2f, flameLength)
                    )
                }
            }
        }
    }

    private fun DrawScope.drawAeroFlightWingsAndThrusters(
        centerX: Float,
        carBaseY: Float,
        carWidth: Float,
        carHeight: Float,
        isBoosting: Boolean,
        timeSeconds: Float
    ) {
        val wingSpan = carWidth * 0.65f
        val wingY = carBaseY - carHeight * 0.1f

        // Left Wing
        val leftWingPath = Path().apply {
            moveTo(centerX - carWidth * 0.45f, wingY)
            lineTo(centerX - carWidth * 0.45f - wingSpan, wingY - carHeight * 0.15f)
            lineTo(centerX - carWidth * 0.45f - wingSpan * 0.95f, wingY + carHeight * 0.25f)
            lineTo(centerX - carWidth * 0.35f, wingY + carHeight * 0.22f)
            close()
        }
        drawPath(
            path = leftWingPath,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF00E5FF), Color(0xFF0F172A))
            )
        )

        // Right Wing
        val rightWingPath = Path().apply {
            moveTo(centerX + carWidth * 0.45f, wingY)
            lineTo(centerX + carWidth * 0.45f + wingSpan, wingY - carHeight * 0.15f)
            lineTo(centerX + carWidth * 0.45f + wingSpan * 0.95f, wingY + carHeight * 0.25f)
            lineTo(centerX + carWidth * 0.35f, wingY + carHeight * 0.22f)
            close()
        }
        drawPath(
            path = rightWingPath,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF00E5FF))
            )
        )

        // Wingtip Plasma Beacons
        drawCircle(
            color = Color(0xFF00E5FF),
            radius = 6f,
            center = Offset(centerX - carWidth * 0.45f - wingSpan, wingY - carHeight * 0.15f)
        )
        drawCircle(
            color = Color(0xFF00E5FF),
            radius = 6f,
            center = Offset(centerX + carWidth * 0.45f + wingSpan, wingY - carHeight * 0.15f)
        )

        // Dual Ion Hover Plasma Thrusters (Downward & Backward)
        val flameLength = (if (isBoosting) 70f else 35f) + sin(timeSeconds * 20f) * 8f
        val thrusterColor = if (isBoosting) Color(0xFF67E8F9) else Color(0xFF38BDF8)

        // Left thruster plume
        drawOval(
            brush = Brush.verticalGradient(
                colors = listOf(thrusterColor, Color.Transparent),
                startY = carBaseY + carHeight * 0.4f,
                endY = carBaseY + carHeight * 0.4f + flameLength
            ),
            topLeft = Offset(centerX - carWidth * 0.35f, carBaseY + carHeight * 0.35f),
            size = Size(carWidth * 0.16f, flameLength)
        )
        // Right thruster plume
        drawOval(
            brush = Brush.verticalGradient(
                colors = listOf(thrusterColor, Color.Transparent),
                startY = carBaseY + carHeight * 0.4f,
                endY = carBaseY + carHeight * 0.4f + flameLength
            ),
            topLeft = Offset(centerX + carWidth * 0.19f, carBaseY + carHeight * 0.35f),
            size = Size(carWidth * 0.16f, flameLength)
        )
    }

    private fun DrawScope.drawCockpitView(
        physics: VehiclePhysicsEngine,
        carColor: CarColor,
        isRaining: Boolean,
        timeSeconds: Float
    ) {
        val width = size.width
        val height = size.height

        // 1. Windshield Pillars & Frame
        val pillarWidth = width * 0.12f
        drawPath(
            path = Path().apply {
                moveTo(0f, 0f)
                lineTo(pillarWidth, 0f)
                lineTo(pillarWidth * 1.5f, height * 0.65f)
                lineTo(0f, height * 0.65f)
                close()
            },
            color = Color(0xFF090D16)
        )
        drawPath(
            path = Path().apply {
                moveTo(width, 0f)
                lineTo(width - pillarWidth, 0f)
                lineTo(width - pillarWidth * 1.5f, height * 0.65f)
                lineTo(width, height * 0.65f)
                close()
            },
            color = Color(0xFF090D16)
        )

        // 2. Windshield Raindrops & Wiper Sweep (if raining)
        if (isRaining) {
            for (drop in 0..18) {
                val dropX = ((drop * 97 + (timeSeconds * 40f).toInt()) % 100) / 100f * width
                val dropY = ((drop * 61 + (timeSeconds * 120f).toInt()) % 65) / 100f * height
                drawCircle(
                    color = Color(0xFFBAE6FD).copy(alpha = 0.45f),
                    radius = 2.5f + (drop % 3),
                    center = Offset(dropX, dropY)
                )
            }

            wiperAngle += wiperDirection * 2.8f
            if (wiperAngle > 65f) wiperDirection = -1f
            if (wiperAngle < -15f) wiperDirection = 1f

            rotate(degrees = wiperAngle, pivot = Offset(width * 0.25f, height * 0.72f)) {
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(width * 0.25f, height * 0.72f),
                    end = Offset(width * 0.25f, height * 0.22f),
                    strokeWidth = 7f,
                    cap = StrokeCap.Round
                )
            }
        }

        // 2b. Augmented Reality Windshield Hologram HUD (Projected onto glass)
        val hudCenterY = height * 0.42f
        val hudCenterX = width * 0.5f

        // Holographic Horizon Line with Banking Angle
        val horizonTilt = physics.steeringInput * -18f
        rotate(degrees = horizonTilt, pivot = Offset(hudCenterX, hudCenterY)) {
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.45f),
                start = Offset(hudCenterX - 65f, hudCenterY),
                end = Offset(hudCenterX - 20f, hudCenterY),
                strokeWidth = 2f
            )
            drawLine(
                color = Color(0xFF00E5FF).copy(alpha = 0.45f),
                start = Offset(hudCenterX + 20f, hudCenterY),
                end = Offset(hudCenterX + 65f, hudCenterY),
                strokeWidth = 2f
            )
            drawCircle(
                color = Color(0xFF00E5FF).copy(alpha = 0.6f),
                radius = 5f,
                center = Offset(hudCenterX, hudCenterY),
                style = Stroke(width = 1.5f)
            )
        }

        // Holographic Target Vector & Waypoint Indicator
        val wayArrowX = hudCenterX + (physics.roadCurveOffset * 40f)
        drawLine(
            color = Color(0xFFFDE047).copy(alpha = 0.7f),
            start = Offset(wayArrowX, hudCenterY - 30f),
            end = Offset(wayArrowX, hudCenterY - 14f),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        // 3. Realistic Cockpit Dashboard Lower Deck
        val dashTop = height * 0.68f
        val dashPath = Path().apply {
            moveTo(0f, dashTop)
            quadraticTo(width * 0.5f, dashTop - 25f, width, dashTop)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = dashPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF111827), Color(0xFF030712)),
                startY = dashTop,
                endY = height
            )
        )

        // Leather stitch line / neon ambient light strip across dashboard
        drawLine(
            color = carColor.glowColor.copy(alpha = 0.85f),
            start = Offset(width * 0.05f, dashTop + 8f),
            end = Offset(width * 0.95f, dashTop + 8f),
            strokeWidth = 3f
        )

        // 4. Center Digital Instrument Cluster HUD
        val clusterWidth = width * 0.52f
        val clusterHeight = height * 0.22f
        val clusterLeft = width * 0.24f
        val clusterTop = dashTop + 20f

        drawRoundRect(
            color = Color(0xFF090D16),
            topLeft = Offset(clusterLeft, clusterTop),
            size = Size(clusterWidth, clusterHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
            style = Fill
        )
        drawRoundRect(
            color = carColor.accentColor.copy(alpha = 0.5f),
            topLeft = Offset(clusterLeft, clusterTop),
            size = Size(clusterWidth, clusterHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
            style = Stroke(width = 2f)
        )

        // Digital Speedometer Text & RPM Gauge Arc
        val rpmRatio = (physics.rpm / 8500f).coerceIn(0.1f, 1.0f)
        val arcRect = Rect(
            Offset(clusterLeft + clusterWidth * 0.08f, clusterTop + 15f),
            Size(clusterWidth * 0.35f, clusterHeight * 0.7f)
        )
        // Background gauge track
        drawArc(
            color = Color(0xFF1E293B),
            startAngle = 140f,
            sweepAngle = 260f,
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = Stroke(width = 10f, cap = StrokeCap.Round)
        )
        // Active RPM sweep
        val rpmColor = if (physics.rpm > 7000f) Color(0xFFEF4444) else carColor.glowColor
        drawArc(
            color = rpmColor,
            startAngle = 140f,
            sweepAngle = 260f * rpmRatio,
            useCenter = false,
            topLeft = arcRect.topLeft,
            size = arcRect.size,
            style = Stroke(width = 10f, cap = StrokeCap.Round)
        )

        // Live Digital Speedometer & Gear Readout on Cockpit Cluster
        val readCenterX = clusterLeft + clusterWidth * 0.68f
        val readCenterY = clusterTop + clusterHeight * 0.45f
        cockpitSpeedPaint.textSize = (clusterHeight * 0.34f).coerceAtLeast(18f)
        cockpitSubPaint.textSize = (clusterHeight * 0.14f).coerceAtLeast(10f)
        drawContext.canvas.nativeCanvas.drawText(
            "${physics.speedKmH.roundToInt()}",
            readCenterX,
            readCenterY,
            cockpitSpeedPaint
        )
        drawContext.canvas.nativeCanvas.drawText(
            "KM/H • GEAR ${physics.currentGear.label}",
            readCenterX,
            readCenterY + cockpitSubPaint.textSize * 1.45f,
            cockpitSubPaint
        )

        // 5. Rotating Sports Steering Wheel with F1 Shift Lights
        val wheelRadius = width * 0.22f
        val wheelCenter = Offset(width * 0.5f, height * 0.88f)
        val wheelSteerAngle = physics.steeringInput * 70f

        rotate(degrees = wheelSteerAngle, pivot = wheelCenter) {
            // Wheel Rim
            drawCircle(
                color = Color(0xFF18181B),
                radius = wheelRadius,
                center = wheelCenter,
                style = Stroke(width = 22f)
            )
            // Top center alignment marker
            drawArc(
                color = carColor.primaryColor,
                startAngle = 260f,
                sweepAngle = 20f,
                useCenter = false,
                topLeft = Offset(wheelCenter.x - wheelRadius, wheelCenter.y - wheelRadius),
                size = Size(wheelRadius * 2f, wheelRadius * 2f),
                style = Stroke(width = 24f)
            )

            // F1 RPM Shift Light Matrix (5 LED lights)
            val shiftStage = ((physics.rpm - 2000f) / 6000f * 5f).toInt().coerceIn(0, 5)
            for (led in 0..4) {
                val ledAngle = (245f + led * 12f) * (PI.toFloat() / 180f)
                val ledX = wheelCenter.x + kotlin.math.cos(ledAngle) * (wheelRadius - 6f)
                val ledY = wheelCenter.y + sin(ledAngle) * (wheelRadius - 6f)
                val isLit = led < shiftStage
                val ledColor = when {
                    !isLit -> Color(0xFF334155)
                    led < 2 -> Color(0xFF10B981) // Green
                    led < 4 -> Color(0xFFF59E0B) // Amber
                    else -> Color(0xFFEF4444) // Redline
                }
                drawCircle(
                    color = ledColor,
                    radius = 3.5f,
                    center = Offset(ledX, ledY)
                )
            }
            // Center Hub
            drawCircle(
                color = Color(0xFF0F172A),
                radius = wheelRadius * 0.45f,
                center = wheelCenter
            )
            // Center Emblem
            drawCircle(
                color = carColor.glowColor,
                radius = 8f,
                center = wheelCenter
            )
        }
    }

    private fun DrawScope.drawDroneViewCar(
        physics: VehiclePhysicsEngine,
        carColor: CarColor,
        headlightsMode: Int,
        timeSeconds: Float
    ) {
        val carLateralShift = (physics.carX * size.width * 0.18f).coerceIn(-size.width * 0.32f, size.width * 0.32f)
        val centerX = size.width / 2f + carLateralShift
        val carBaseY = size.height * 0.72f - (physics.flightAltitude * 1.2f)
        val carLength = size.width * 0.32f
        val carWidth = carLength * 0.52f

        val carHeading = physics.steeringInput * 18f

        rotate(degrees = carHeading, pivot = Offset(centerX, carBaseY)) {
            // Aero-Flight Mode Wings & Thruster Plumes from Drone View
            if (physics.isFlightMode) {
                val wingSpan = carWidth * 0.85f
                drawPath(
                    path = Path().apply {
                        moveTo(centerX - carWidth * 0.4f, carBaseY - carLength * 0.15f)
                        lineTo(centerX - carWidth * 0.4f - wingSpan, carBaseY + carLength * 0.10f)
                        lineTo(centerX - carWidth * 0.4f - wingSpan * 0.9f, carBaseY + carLength * 0.25f)
                        lineTo(centerX - carWidth * 0.4f, carBaseY + carLength * 0.18f)
                        close()
                    },
                    color = Color(0xFF00E5FF).copy(alpha = 0.85f)
                )
                drawPath(
                    path = Path().apply {
                        moveTo(centerX + carWidth * 0.4f, carBaseY - carLength * 0.15f)
                        lineTo(centerX + carWidth * 0.4f + wingSpan, carBaseY + carLength * 0.10f)
                        lineTo(centerX + carWidth * 0.4f + wingSpan * 0.9f, carBaseY + carLength * 0.25f)
                        lineTo(centerX + carWidth * 0.4f, carBaseY + carLength * 0.18f)
                        close()
                    },
                    color = Color(0xFF00E5FF).copy(alpha = 0.85f)
                )
            }

            // Vehicle from above
            drawRoundRect(
                color = carColor.primaryColor,
                topLeft = Offset(centerX - carWidth / 2f, carBaseY - carLength / 2f),
                size = Size(carWidth, carLength),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
            )
            // Roof Glass
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(centerX - carWidth * 0.36f, carBaseY - carLength * 0.22f),
                size = Size(carWidth * 0.72f, carLength * 0.45f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )
            // Headlights beam shooting forward
            if (headlightsMode > 0) {
                drawOval(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFFEF08A).copy(alpha = 0.5f), Color.Transparent),
                        startY = carBaseY - carLength * 1.5f,
                        endY = carBaseY - carLength * 0.5f
                    ),
                    topLeft = Offset(centerX - carWidth * 0.6f, carBaseY - carLength * 1.5f),
                    size = Size(carWidth * 1.2f, carLength)
                )
            }
        }
    }
}
