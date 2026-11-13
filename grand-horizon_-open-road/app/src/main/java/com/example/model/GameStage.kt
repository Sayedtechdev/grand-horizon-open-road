package com.example.model

enum class StepStage(
    val stageNumber: Int,
    val title: String,
    val subtitle: String,
    val targetKm: Float,
    val unlocksFlyingCar: Boolean,
    val description: String,
    val trafficFactor: Float
) {
    STEP_1(
        stageNumber = 1,
        title = "Step 1: Regional Discovery",
        subtitle = "Metropolitan Highways & Expressways",
        targetKm = 12.0f,
        unlocksFlyingCar = false,
        description = "Begin your journey across the first major cities. Master smooth highway cruising, lane changes, and throttle control through sunny and golden hour conditions.",
        trafficFactor = 0.8f
    ),
    STEP_2(
        stageNumber = 2,
        title = "Step 2: Grand Traverse",
        subtitle = "Mountain Curves & Monsoon Night Trails",
        targetKm = 28.0f,
        unlocksFlyingCar = false,
        description = "Push into deeper country routes crossing epic mountain passes, suspension bridge cables, and wet highway asphalt under mist, rainstorms, and starry skies.",
        trafficFactor = 1.3f
    ),
    STEP_3(
        stageNumber = 3,
        title = "Step 3: AeroDrive Zenith (Flying Car)",
        subtitle = "Full Country Access & Supersonic Flight",
        targetKm = 50.0f,
        unlocksFlyingCar = true,
        description = "FLYING CAR UNLOCKED! Transform your hypercar with deployable aero-wings and dual plasma hover thrusters. Fly freely over all major cities from coast to coast with unrestricted altitude!",
        trafficFactor = 1.6f
    );

    fun isUnlocked(step1Completed: Boolean, step2Completed: Boolean): Boolean {
        return when (this) {
            STEP_1 -> true
            STEP_2 -> step1Completed
            STEP_3 -> step1Completed && step2Completed
        }
    }
}
