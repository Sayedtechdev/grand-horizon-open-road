package com.example.model

import androidx.compose.ui.graphics.Color

enum class CarColor(
    val id: String,
    val displayName: String,
    val primaryColor: Color,
    val accentColor: Color,
    val rimColor: Color,
    val glowColor: Color
) {
    CYBER_CYAN(
        id = "cyber_cyan",
        displayName = "Cyber Cyan",
        primaryColor = Color(0xFF00E5FF),
        accentColor = Color(0xFF0891B2),
        rimColor = Color(0xFF1E293B),
        glowColor = Color(0xFF38BDF8)
    ),
    MIDNIGHT_ONYX(
        id = "midnight_onyx",
        displayName = "Midnight Onyx",
        primaryColor = Color(0xFF18181B),
        accentColor = Color(0xFF3F3F46),
        rimColor = Color(0xFFE4E4E7),
        glowColor = Color(0xFFA1A1AA)
    ),
    CRIMSON_BLAZE(
        id = "crimson_blaze",
        displayName = "Crimson Blaze",
        primaryColor = Color(0xFFE11D48),
        accentColor = Color(0xFF9F1239),
        rimColor = Color(0xFF18181B),
        glowColor = Color(0xFFFB7185)
    ),
    SUNSET_AMBER(
        id = "sunset_amber",
        displayName = "Sunset Amber",
        primaryColor = Color(0xFFF59E0B),
        accentColor = Color(0xFFB45309),
        rimColor = Color(0xFF27272A),
        glowColor = Color(0xFFFCD34D)
    ),
    EMERALD_PULSE(
        id = "emerald_pulse",
        displayName = "Emerald Pulse",
        primaryColor = Color(0xFF10B981),
        accentColor = Color(0xFF047857),
        rimColor = Color(0xFF0F172A),
        glowColor = Color(0xFF6EE7B7)
    ),
    PEARL_WHITE(
        id = "pearl_white",
        displayName = "Pearl White",
        primaryColor = Color(0xFFF8FAFC),
        accentColor = Color(0xFFCBD5E1),
        rimColor = Color(0xFF0F172A),
        glowColor = Color(0xFF93C5FD)
    ),
    ROYAL_VIOLET(
        id = "royal_violet",
        displayName = "Royal Violet",
        primaryColor = Color(0xFF8B5CF6),
        accentColor = Color(0xFF5B21B6),
        rimColor = Color(0xFF18181B),
        glowColor = Color(0xFFC084FC)
    ),
    TITANIUM_BLUE(
        id = "titanium_blue",
        displayName = "Titanium Blue",
        primaryColor = Color(0xFF2563EB),
        accentColor = Color(0xFF1E40AF),
        rimColor = Color(0xFF0284C7),
        glowColor = Color(0xFF60A5FA)
    )
}

enum class DriveGear(val label: String, val ratio: Float) {
    PARK("P", 0.0f),
    REVERSE("R", -1.8f),
    NEUTRAL("N", 0.0f),
    DRIVE("D", 3.2f),
    SPORT("S", 4.0f)
}

enum class CameraView(val label: String) {
    CHASE("Chase View"),
    COCKPIT("Cockpit / Dash"),
    DRONE("Drone Orbit")
}

data class TrafficCar(
    val id: Long,
    val countryType: CountryType,
    val typeName: String,
    var roadSegmentIndex: Float, // distance ahead on road
    var laneIndex: Int,          // -1 (left), 0 (center), 1 (right)
    var lateralOffset: Float,    // fine offset -1f to 1f
    var speedKmH: Float,
    val baseColor: Color,
    val isBraking: Boolean = false,
    val isSpecialVehicle: Boolean = false // e.g. CNG auto-rickshaw or Istanbul taxi
)
