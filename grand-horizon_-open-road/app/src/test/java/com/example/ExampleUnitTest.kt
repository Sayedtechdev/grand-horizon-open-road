package com.example

import com.example.model.CarColor
import com.example.model.CountryRoute
import com.example.model.CountryType
import com.example.model.DriveGear
import com.example.model.StepStage
import com.example.model.TimeOfDay
import com.example.model.WeatherCondition
import com.example.physics.VehiclePhysicsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun turkeyRoute_startsAtIstanbul_andHasAllMajorCities() {
        val turkey = CountryRoute.TURKEY
        assertEquals("Turkey", turkey.displayName)
        assertEquals("Istanbul", turkey.startCity)
        assertEquals("Istanbul", turkey.cities.first().name)
        assertEquals(10, turkey.cities.size)
        assertTrue(turkey.cities.any { it.name.contains("Ankara") })
        assertTrue(turkey.cities.any { it.name.contains("Cappadocia") })
        assertTrue(turkey.cities.any { it.name.contains("Antalya") })
        assertTrue(turkey.cities.any { it.name.contains("İzmir") })
        assertTrue(turkey.cities.any { it.name.contains("Bursa") })
    }

    @Test
    fun bangladeshRoute_startsAtChittagong_andHasAllMajorCities() {
        val bangladesh = CountryRoute.BANGLADESH
        assertEquals("Bangladesh", bangladesh.displayName)
        assertEquals("Chittagong", bangladesh.startCity)
        assertEquals("Chittagong", bangladesh.cities.first().name)
        assertEquals(10, bangladesh.cities.size)
        assertTrue(bangladesh.cities.any { it.name.contains("Cox's Bazar") })
        assertTrue(bangladesh.cities.any { it.name.contains("Dhaka") })
        assertTrue(bangladesh.cities.any { it.name.contains("Sylhet") })
        assertTrue(bangladesh.cities.any { it.name.contains("Jamuna") })
        assertTrue(bangladesh.cities.any { it.name.contains("Rajshahi") })
    }

    @Test
    fun stages_unlockFlyingCarInStep3AfterSteps1And2() {
        val step1 = StepStage.STEP_1
        val step2 = StepStage.STEP_2
        val step3 = StepStage.STEP_3

        // Step 1: initial stage
        assertTrue(step1.isUnlocked(step1Completed = false, step2Completed = false))
        assertFalse(step1.unlocksFlyingCar)

        // Step 2: unlocked after step 1
        assertFalse(step2.isUnlocked(step1Completed = false, step2Completed = false))
        assertTrue(step2.isUnlocked(step1Completed = true, step2Completed = false))
        assertFalse(step2.unlocksFlyingCar)

        // Step 3: unlocked only when both step 1 and step 2 are complete
        assertFalse(step3.isUnlocked(step1Completed = false, step2Completed = false))
        assertFalse(step3.isUnlocked(step1Completed = true, step2Completed = false))
        assertFalse(step3.isUnlocked(step1Completed = false, step2Completed = true))
        assertTrue(step3.isUnlocked(step1Completed = true, step2Completed = true))

        // Flying car is available in Step 3!
        assertTrue(step3.unlocksFlyingCar)
    }

    @Test
    fun vehiclePhysics_smoothAccelerationAndBraking() {
        val physics = VehiclePhysicsEngine()
        val turkey = CountryRoute.TURKEY
        val stage = StepStage.STEP_1

        physics.currentGear = DriveGear.DRIVE
        physics.throttleInput = 1.0f

        // 10 simulation ticks
        for (i in 0 until 10) {
            physics.update(0.016f, turkey, stage)
        }

        assertTrue("Car should accelerate with throttle", physics.speedKmH > 0f)
        assertTrue("RPM should rise", physics.rpm > 850f)

        // Apply brake
        physics.throttleInput = 0f
        physics.brakeInput = 1.0f
        val speedBeforeBrake = physics.speedKmH

        for (i in 0 until 10) {
            physics.update(0.016f, turkey, stage)
        }

        assertTrue("Speed should drop with brake applied", physics.speedKmH < speedBeforeBrake)
    }

    @Test
    fun vehiclePhysics_handbrakeEnablesDriftSlip() {
        val physics = VehiclePhysicsEngine()
        val turkey = CountryRoute.TURKEY
        val stage = StepStage.STEP_1

        physics.currentGear = DriveGear.DRIVE
        physics.speedKmH = 80f
        physics.steeringInput = 0.8f
        physics.handbrakeActive = true

        physics.update(0.016f, turkey, stage)
        assertTrue("Handbrake at speed should engage drift slip", physics.driftSlip > 0f)
    }

    @Test
    fun flyingCar_aeroFlightModeInStep3() {
        val physics = VehiclePhysicsEngine()
        val bangladesh = CountryRoute.BANGLADESH
        val stage = StepStage.STEP_3

        physics.isFlightMode = true
        physics.flightAscendInput = 1.0f
        physics.throttleInput = 1.0f

        for (i in 0 until 20) {
            physics.update(0.016f, bangladesh, stage)
        }

        assertTrue("Altitude should increase in flight mode", physics.flightAltitude > 0f)
        assertTrue("Flight speed should increase", physics.speedKmH > 0f)
    }

    @Test
    fun carColors_andCustomizationOptionsAvailable() {
        val colors = CarColor.entries
        assertTrue(colors.size >= 8)
        assertTrue(colors.any { it.id == "cyber_cyan" })
        assertTrue(colors.any { it.id == "midnight_onyx" })
        assertTrue(colors.any { it.id == "crimson_blaze" })
        assertTrue(colors.any { it.id == "sunset_amber" })
    }

    @Test
    fun weatherAndDayNight_presetsValid() {
        val weathers = WeatherCondition.entries
        assertTrue(weathers.contains(WeatherCondition.CLEAR))
        assertTrue(weathers.contains(WeatherCondition.GOLDEN_HOUR))
        assertTrue(weathers.contains(WeatherCondition.MONSOON_RAIN))
        assertTrue(weathers.contains(WeatherCondition.THUNDERSTORM))

        val timesOfDay = TimeOfDay.entries
        assertTrue(timesOfDay.contains(TimeOfDay.DAWN))
        assertTrue(timesOfDay.contains(TimeOfDay.MIDDAY))
        assertTrue(timesOfDay.contains(TimeOfDay.SUNSET))
        assertTrue(timesOfDay.contains(TimeOfDay.MIDNIGHT))
    }

    @Test
    fun companions_allCharactersAvailable() {
        val companions = com.example.model.TravelCompanion.entries
        assertEquals(3, companions.size)
        val sayed = companions.find { it.displayName == "Sayed" }!!
        assertEquals("Male", sayed.gender)

        val hena = companions.find { it.displayName == "Hena" }!!
        assertEquals("Female", hena.gender)

        val afia = companions.find { it.displayName == "Afia" }!!
        assertEquals("Female", afia.gender)
    }

    @Test
    fun interactiveControls_startAndStopAcceleratingAndBraking() {
        val physics = VehiclePhysicsEngine()
        val turkey = CountryRoute.TURKEY
        val stage = StepStage.STEP_1

        val initialSpeed = physics.speedKmH
        physics.startAccelerating()
        assertTrue("isAcceleratingPressed should be true", physics.isAcceleratingPressed)
        assertTrue("Speed should increase immediately on accelerate", physics.speedKmH > initialSpeed)
        assertEquals(1.0f, physics.throttleInput, 0.01f)
        assertEquals(0f, physics.brakeInput, 0.01f)

        physics.stopAccelerating()
        assertFalse("isAcceleratingPressed should be false after release", physics.isAcceleratingPressed)

        // Test startBraking and stopBraking
        val speedBeforeBrake = physics.speedKmH
        physics.startBraking()
        assertTrue("isBrakingPressed should be true", physics.isBrakingPressed)
        assertEquals(1.0f, physics.brakeInput, 0.01f)
        assertEquals(0f, physics.throttleInput, 0.01f)
        assertTrue("Speed should drop on brake", physics.speedKmH < speedBeforeBrake)

        physics.stopBraking()
        assertFalse("isBrakingPressed should be false after release", physics.isBrakingPressed)
        assertEquals(0f, physics.brakeInput, 0.01f)

        // Test Steering
        physics.startSteeringLeft()
        assertTrue("isSteeringLeftPressed should be true", physics.isSteeringLeftPressed)
        assertEquals(-1.0f, physics.steeringInput, 0.01f)

        physics.startSteeringRight()
        assertTrue("isSteeringRightPressed should be true", physics.isSteeringRightPressed)
        assertEquals(1.0f, physics.steeringInput, 0.01f)

        physics.stopSteering()
        assertFalse("isSteeringLeftPressed should be false", physics.isSteeringLeftPressed)
        assertFalse("isSteeringRightPressed should be false", physics.isSteeringRightPressed)
        assertEquals(0f, physics.steeringInput, 0.01f)

        // Test Nitro Booster
        val speedBeforeNitro = physics.speedKmH
        physics.activateNitro(true)
        assertTrue("isBoosting should be true", physics.isBoosting)
        assertTrue("Nitro should boost speed significantly", physics.speedKmH > speedBeforeNitro)
    }

    @Test
    fun neutralGear_coastsWithoutAutoAccelerating() {
        val physics = VehiclePhysicsEngine()
        val turkey = CountryRoute.TURKEY
        val stage = StepStage.STEP_1

        physics.currentGear = DriveGear.NEUTRAL
        physics.speedKmH = 40f
        physics.throttleInput = 0f
        physics.brakeInput = 0f

        for (i in 0 until 20) {
            physics.update(0.016f, turkey, stage)
        }

        assertTrue("Car in Neutral should coast down cleanly, not auto-accelerate", physics.speedKmH < 40f)
    }

    @Test
    fun reverseGear_deceleratesSmoothlyThenReverses() {
        val physics = VehiclePhysicsEngine()
        val bangladesh = CountryRoute.BANGLADESH
        val stage = StepStage.STEP_1

        physics.speedKmH = 50f
        physics.currentGear = DriveGear.REVERSE
        physics.throttleInput = 0.8f

        // First frames should decelerate smoothly from 50 km/h rather than snapping to 0 in one frame
        physics.update(0.016f, bangladesh, stage)
        assertTrue("Reverse shift should decelerate smoothly instead of snapping", physics.speedKmH in 1f..49.9f)

        // Continue updating until speed drops to 0 and enters negative reverse speed
        for (i in 0 until 60) {
            physics.update(0.016f, bangladesh, stage)
        }
        assertTrue("Car should move in reverse once stopped", physics.speedKmH < 0f)
    }

    @Test
    fun cityProgress_synchronizedBetweenHudAndMap() {
        val turkeyStart = CountryRoute.TURKEY.findCityProgress(0f)
        assertEquals("Istanbul", turkeyStart.activeCity.name)
        assertEquals("Kocaeli & İzmit", turkeyStart.nextCity.name)

        // At 100 * 0.05 = 5.0 km, player enters Kocaeli & Izmit
        val turkeySecond = CountryRoute.TURKEY.findCityProgress(5.2f)
        assertEquals("Kocaeli & İzmit", turkeySecond.activeCity.name)
        assertEquals("Bursa", turkeySecond.nextCity.name)

        val bdStart = CountryRoute.BANGLADESH.findCityProgress(0f)
        assertEquals("Chittagong", bdStart.activeCity.name)
        assertEquals("Cox's Bazar", bdStart.nextCity.name)
    }
}
