package com.example.physics

import androidx.compose.ui.graphics.Color
import com.example.model.CountryRoute
import com.example.model.CountryType
import com.example.model.DriveGear
import com.example.model.StepStage
import com.example.model.TrafficCar
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

class VehiclePhysicsEngine {
    // Inputs from user controls
    var throttleInput: Float = 0.4f // starts with positive throttle for immediate motion!
    var brakeInput: Float = 0f    // 0f to 1f
    var handbrakeActive: Boolean = false
    var steeringInput: Float = 0f // -1f to 1f
    var currentGear: DriveGear = DriveGear.DRIVE

    // Nitro Speed Booster (Ground & Flight)
    var isBoosting: Boolean = false
    var boostEnergy: Float = 1.0f // 0f to 1f

    // Flight controls (Step 3)
    var isFlightMode: Boolean = false
    var flightAltitude: Float = 0f // 0 to 120 meters
    var flightAscendInput: Float = 0f // -1f (descend) to 1f (ascend)

    // Pressed state flags for smooth continuous touch input
    var isAcceleratingPressed: Boolean = false
    var isBrakingPressed: Boolean = false
    var isSteeringLeftPressed: Boolean = false
    var isSteeringRightPressed: Boolean = false

    // Output vehicle states - starts with cruising motion!
    var speedKmH: Float = 75f
    var rpm: Float = 2400f
    var carX: Float = 0f // -1f (far left road line) to +1f (far right road line)
    var driftSlip: Float = 0f
    var currentGearIndex: Int = 3 // 1 to 7 in Drive
    var totalDistanceKm: Float = 0f
    var tripDistanceKm: Float = 0f

    // Road curve & elevation physics
    var roadCurveOffset: Float = 0f
    var roadSegmentIndex: Float = 0f
    var currentHillPitch: Float = 0f

    // Dynamic Traffic
    val trafficVehicles = mutableListOf<TrafficCar>()
    private var trafficSpawnTimer = 0f
    private var nextTrafficId = 1L

    // Constants
    private val maxSpeedGroundKmH = 340f
    private val maxSpeedBoostKmH = 390f
    private val maxSpeedFlightKmH = 460f
    private val groundAcceleration = 90f // snappy responsiveness
    private val brakeDeceleration = 135f
    private val handbrakeDecel = 85f
    private val naturalFriction = 8f

    fun resetTrip() {
        speedKmH = 75f
        rpm = 2400f
        carX = 0f
        driftSlip = 0f
        tripDistanceKm = 0f
        isFlightMode = false
        flightAltitude = 0f
        trafficVehicles.clear()
        isAcceleratingPressed = false
        isBrakingPressed = false
        isSteeringLeftPressed = false
        isSteeringRightPressed = false
    }

    fun tapAccelerate(boostAmount: Float = 30f) {
        speedKmH = (speedKmH + boostAmount).coerceAtMost(if (isBoosting) maxSpeedBoostKmH else maxSpeedGroundKmH)
        throttleInput = 1.0f
        brakeInput = 0f
    }

    fun startAccelerating() {
        isAcceleratingPressed = true
        isBrakingPressed = false
        speedKmH = (speedKmH + 20f).coerceAtMost(if (isBoosting) maxSpeedBoostKmH else maxSpeedGroundKmH)
        throttleInput = 1.0f
        brakeInput = 0f
    }

    fun stopAccelerating() {
        isAcceleratingPressed = false
        throttleInput = 0.40f
    }

    fun tapBrake(brakeAmount: Float = 40f) {
        speedKmH = (speedKmH - brakeAmount).coerceAtLeast(0f)
        throttleInput = 0f
        brakeInput = 1.0f
    }

    fun startBraking() {
        isBrakingPressed = true
        isAcceleratingPressed = false
        speedKmH = (speedKmH - 30f).coerceAtLeast(0f)
        brakeInput = 1.0f
        throttleInput = 0f
    }

    fun stopBraking() {
        isBrakingPressed = false
        brakeInput = 0f
        throttleInput = 0.40f
    }

    fun steerLeft(strength: Float = 0.9f) {
        steeringInput = -strength
    }

    fun startSteeringLeft(strength: Float = 1.0f) {
        isSteeringLeftPressed = true
        isSteeringRightPressed = false
        steeringInput = -strength
    }

    fun steerRight(strength: Float = 0.9f) {
        steeringInput = strength
    }

    fun startSteeringRight(strength: Float = 1.0f) {
        isSteeringRightPressed = true
        isSteeringLeftPressed = false
        steeringInput = strength
    }

    fun stopSteering() {
        isSteeringLeftPressed = false
        isSteeringRightPressed = false
        steeringInput = 0f
    }

    fun activateNitro(active: Boolean) {
        isBoosting = active
        if (active) {
            speedKmH = (speedKmH + 50f).coerceAtMost(maxSpeedBoostKmH)
            throttleInput = 1.0f
            brakeInput = 0f
        }
    }

    fun update(
        deltaTime: Float,
        country: CountryRoute,
        stage: StepStage
    ) {
        val dt = deltaTime.coerceIn(0.001f, 0.05f)

        // Process continuous touch inputs
        if (isAcceleratingPressed) {
            throttleInput = 1.0f
            brakeInput = 0f
        } else if (isBrakingPressed) {
            brakeInput = 1.0f
            throttleInput = 0f
        }

        if (isSteeringLeftPressed) {
            steeringInput = -1.0f
        } else if (isSteeringRightPressed) {
            steeringInput = 1.0f
        }

        // 1. Boost energy recovery / consumption
        if (isBoosting) {
            boostEnergy = (boostEnergy - dt * 0.30f).coerceAtLeast(0f)
            if (boostEnergy <= 0.02f) {
                isBoosting = false
            }
        } else {
            boostEnergy = (boostEnergy + dt * 0.20f).coerceAtMost(1f)
        }

        // 2. Flight Mode Physics (Step 3)
        if (isFlightMode) {
            val targetAltChange = flightAscendInput * 30f * dt
            flightAltitude = (flightAltitude + targetAltChange).coerceIn(0f, 120f)
            if (flightAltitude <= 0.5f && flightAscendInput < 0f) {
                flightAltitude = 0f
            }

            val topSpeed = if (isBoosting) maxSpeedFlightKmH else 360f
            val thrust = if (throttleInput > 0.05f || isBoosting) {
                val boostMultiplier = if (isBoosting) 2.2f else 1.2f
                (throttleInput.coerceAtLeast(0.5f)) * 90f * boostMultiplier * dt
            } else {
                -naturalFriction * 0.5f * dt
            }

            if (brakeInput > 0.05f) {
                speedKmH = (speedKmH - brakeDeceleration * brakeInput * dt).coerceAtLeast(0f)
            } else {
                speedKmH = (speedKmH + thrust).coerceIn(0f, topSpeed)
            }

            // Flight steering (smooth banking)
            carX += steeringInput * 2.2f * dt
            carX = carX.coerceIn(-2.2f, 2.2f)

            rpm = 3000f + (speedKmH / topSpeed) * 5000f
            driftSlip = 0f
        } else {
            // Ground Driving Physics
            if (flightAltitude > 0f) {
                flightAltitude = (flightAltitude - dt * 40f).coerceAtLeast(0f)
            }

            val effectiveGearRatio = when (currentGear) {
                DriveGear.PARK -> 0f
                DriveGear.NEUTRAL -> 0f
                DriveGear.REVERSE -> -0.45f
                DriveGear.DRIVE -> 1.0f
                DriveGear.SPORT -> 1.35f
            }

            // Automatic multi-gear simulation in Drive / Sport
            if (currentGear == DriveGear.DRIVE || currentGear == DriveGear.SPORT) {
                val speed = abs(speedKmH)
                currentGearIndex = when {
                    speed < 40f -> 1
                    speed < 80f -> 2
                    speed < 130f -> 3
                    speed < 190f -> 4
                    speed < 250f -> 5
                    speed < 310f -> 6
                    else -> 7
                }
            } else {
                currentGearIndex = 1
            }

            // Engine RPM
            if (currentGear == DriveGear.PARK || currentGear == DriveGear.NEUTRAL) {
                rpm = (850f + throttleInput * 6800f).coerceIn(850f, 8000f)
            } else {
                val gearBaseSpeed = (currentGearIndex - 1) * 45f
                val gearRelSpeed = (abs(speedKmH) - gearBaseSpeed).coerceAtLeast(0f)
                val targetRpm = 1400f + (gearRelSpeed * 75f) + (throttleInput * 2200f)
                rpm = targetRpm.coerceIn(850f, 8500f)
            }

            // Acceleration calculation
            val topGroundSpeed = if (isBoosting) maxSpeedBoostKmH else maxSpeedGroundKmH
            val gearAccFactor = max(0.5f, 1.4f - (currentGearIndex * 0.10f))
            val boostBonus = if (isBoosting) 140f else 0f
            val effectiveThrottle = if (isBoosting) 1.0f else throttleInput

            val driveForce = if (effectiveThrottle > 0.02f || isBoosting) {
                (effectiveThrottle * groundAcceleration * gearAccFactor * effectiveGearRatio + boostBonus) * dt
            } else {
                0f
            }

            // Deceleration: Brakes & Handbrake & Aerodynamic Drag
            val brakeForce = brakeInput * brakeDeceleration * dt
            val handbrakeForce = if (handbrakeActive) handbrakeDecel * dt else 0f
            val dragForce = (naturalFriction + (speedKmH * speedKmH * 0.00025f)) * dt

            // Road shoulder slowdown if off-road
            val isOffRoad = abs(carX) > 1.25f
            val offRoadDrag = if (isOffRoad) 40f * dt else 0f

            if (currentGear == DriveGear.REVERSE) {
                if (speedKmH > 0f) {
                    // Decelerate smoothly to 0 before engaging reverse motion
                    speedKmH = max(0f, speedKmH - (brakeDeceleration * 1.4f + brakeForce + handbrakeForce) * dt)
                } else {
                    val reverseForce = if (isAcceleratingPressed || throttleInput > 0.05f) {
                        -(throttleInput.coerceAtLeast(0.6f) * groundAcceleration * 0.55f) * dt
                    } else {
                        0f
                    }
                    val reverseDrag = (naturalFriction * 0.6f) * dt
                    val targetSpeed = speedKmH + reverseForce + reverseDrag + brakeForce + handbrakeForce
                    speedKmH = targetSpeed.coerceIn(-65f, 0f)
                }
            } else if (currentGear == DriveGear.PARK) {
                speedKmH = max(0f, speedKmH - (brakeDeceleration * dt))
            } else if (currentGear == DriveGear.NEUTRAL) {
                // Coast naturally in Neutral without engine drive or auto-cruise
                val totalSlowdown = brakeForce + handbrakeForce + dragForce + offRoadDrag
                speedKmH = (speedKmH - totalSlowdown).coerceIn(0f, topGroundSpeed)
            } else {
                // DRIVE or SPORT gear
                speedKmH += driveForce
                val totalSlowdown = brakeForce + handbrakeForce + dragForce + offRoadDrag
                speedKmH = (speedKmH - totalSlowdown).coerceIn(0f, topGroundSpeed)

                // If not braking and in Drive/Sport, maintain highway cruising speed (never stuck at 0)
                if (!isBrakingPressed && brakeInput == 0f && speedKmH < 75f) {
                    speedKmH = (speedKmH + 35f * dt).coerceAtMost(75f)
                }
            }

            // Steering & Centrifugal Drift (allows gentle steering while reversing too)
            val steerMultiplier = if (handbrakeActive && abs(speedKmH) > 35f) 2.5f else 1.6f
            val speedFactor = (abs(speedKmH) / 100f).coerceIn(0.2f, 1.2f)
            val steerDirectionSign = if (speedKmH < -1f) -1f else 1f
            val steerDelta = steeringInput * steerDirectionSign * steerMultiplier * speedFactor * dt * 2.2f
            carX += steerDelta

            // Centrifugal curve drift pull
            carX -= roadCurveOffset * (speedKmH / 240f) * dt * 1.2f
            carX = carX.coerceIn(-2.2f, 2.2f)

            // Handbrake drift slip calculation
            if (handbrakeActive && abs(speedKmH) > 30f) {
                driftSlip = (driftSlip + dt * 5f).coerceAtMost(1.0f)
            } else {
                driftSlip = (driftSlip - dt * 3.5f).coerceAtLeast(0f)
            }
        }

        // 3. Distance & Odometer update
        val speedMps = (abs(speedKmH) * 1000f) / 3600f
        val distanceTraveledKm = (speedMps * dt) / 1000f
        totalDistanceKm += distanceTraveledKm
        tripDistanceKm += distanceTraveledKm
        roadSegmentIndex += (speedKmH * dt * 0.45f)

        // 4. Procedural Road Curves & Hills
        val curveCycle = (roadSegmentIndex * 0.0018f)
        roadCurveOffset = sin(curveCycle) * 0.85f + cos(curveCycle * 0.5f) * 0.35f
        currentHillPitch = sin(roadSegmentIndex * 0.0025f) * 0.5f

        // 5. Dynamic Traffic updates & spawning
        updateTraffic(dt, country.type, stage.trafficFactor)
    }

    private fun updateTraffic(dt: Float, country: CountryType, trafficFactor: Float) {
        trafficSpawnTimer += dt
        val spawnInterval = 3.5f / trafficFactor

        if (trafficSpawnTimer > spawnInterval && trafficVehicles.size < 8) {
            trafficSpawnTimer = 0f
            spawnTrafficCar(country)
        }

        val iterator = trafficVehicles.iterator()
        while (iterator.hasNext()) {
            val traffic = iterator.next()
            val relSpeed = (traffic.speedKmH - speedKmH)
            val distanceDelta = (relSpeed * 1000f / 3600f) * dt
            traffic.roadSegmentIndex += distanceDelta

            val targetOffset = traffic.laneIndex * 0.55f
            traffic.lateralOffset += (targetOffset - traffic.lateralOffset) * 2f * dt

            if (flightAltitude < 15f && abs(traffic.roadSegmentIndex) < 4.5f && abs(carX - traffic.lateralOffset) < 0.42f) {
                if (speedKmH > traffic.speedKmH) {
                    speedKmH = max(traffic.speedKmH * 0.85f, speedKmH - 30f)
                    traffic.speedKmH = min(120f, traffic.speedKmH + 15f)
                }
            }

            if (traffic.roadSegmentIndex < -50f || traffic.roadSegmentIndex > 260f) {
                iterator.remove()
            }
        }
    }

    private fun spawnTrafficCar(country: CountryType) {
        val lane = Random.nextInt(-1, 2)
        val isSpecial = Random.nextFloat() < 0.4f
        val carType = if (country == CountryType.BANGLADESH) {
            if (isSpecial) "CNG Auto-Rickshaw" else if (Random.nextBoolean()) "Dhaka Express Bus" else "Green Taxi"
        } else {
            if (isSpecial) "Istanbul Yellow Taxi" else if (Random.nextBoolean()) "Anatolian Coach" else "Sedan"
        }

        val baseColor = when {
            carType.contains("Yellow Taxi") -> Color(0xFFFACC15)
            carType.contains("CNG") -> Color(0xFF16A34A)
            carType.contains("Bus") || carType.contains("Coach") -> Color(0xFFDC2626)
            else -> listOf(
                Color(0xFF3B82F6),
                Color(0xFF64748B),
                Color(0xFFE2E8F0),
                Color(0xFF1E293B),
                Color(0xFFF97316)
            ).random()
        }

        val speed = Random.nextFloat() * 40f + 65f
        trafficVehicles.add(
            TrafficCar(
                id = nextTrafficId++,
                countryType = country,
                typeName = carType,
                roadSegmentIndex = Random.nextFloat() * 60f + 120f,
                laneIndex = lane,
                lateralOffset = lane * 0.55f,
                speedKmH = speed,
                baseColor = baseColor,
                isSpecialVehicle = isSpecial
            )
        )
    }
}
