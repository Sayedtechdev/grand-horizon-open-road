package com.example.model

import androidx.compose.ui.graphics.Color

enum class WeatherCondition(
    val displayName: String,
    val icon: String,
    val rainIntensity: Float, // 0f to 1f
    val fogDensity: Float,     // 0f to 1f
    val wetRoadReflection: Float,
    val cloudCover: Float
) {
    CLEAR(
        displayName = "Clear Sky",
        icon = "☀️",
        rainIntensity = 0f,
        fogDensity = 0.05f,
        wetRoadReflection = 0.1f,
        cloudCover = 0.2f
    ),
    GOLDEN_HOUR(
        displayName = "Golden Sunset",
        icon = "🌅",
        rainIntensity = 0f,
        fogDensity = 0.1f,
        wetRoadReflection = 0.25f,
        cloudCover = 0.35f
    ),
    MONSOON_RAIN(
        displayName = "Monsoon Rain",
        icon = "🌧️",
        rainIntensity = 0.85f,
        fogDensity = 0.35f,
        wetRoadReflection = 0.85f,
        cloudCover = 0.9f
    ),
    DENSE_MIST(
        displayName = "Dense Mist",
        icon = "🌫️",
        rainIntensity = 0.1f,
        fogDensity = 0.75f,
        wetRoadReflection = 0.4f,
        cloudCover = 0.8f
    ),
    THUNDERSTORM(
        displayName = "Thunderstorm",
        icon = "⛈️",
        rainIntensity = 1.0f,
        fogDensity = 0.45f,
        wetRoadReflection = 0.95f,
        cloudCover = 1.0f
    ),
    STARRY_NIGHT(
        displayName = "Starry Night",
        icon = "✨",
        rainIntensity = 0f,
        fogDensity = 0.15f,
        wetRoadReflection = 0.3f,
        cloudCover = 0.15f
    )
}

enum class TimeOfDay(
    val displayName: String,
    val hourOfDay: Float,
    val ambientBrightness: Float,
    val skyTopColor: Color,
    val skyBottomColor: Color,
    val sunOrMoonColor: Color,
    val isNight: Boolean
) {
    DAWN(
        displayName = "Dawn / Morning",
        hourOfDay = 6.5f,
        ambientBrightness = 0.65f,
        skyTopColor = Color(0xFF1E1B4B),
        skyBottomColor = Color(0xFFFB923C),
        sunOrMoonColor = Color(0xFFFDE047),
        isNight = false
    ),
    MIDDAY(
        displayName = "Sunny Midday",
        hourOfDay = 12.0f,
        ambientBrightness = 1.0f,
        skyTopColor = Color(0xFF0284C7),
        skyBottomColor = Color(0xFFBAE6FD),
        sunOrMoonColor = Color(0xFFFEF08A),
        isNight = false
    ),
    SUNSET(
        displayName = "Golden Sunset",
        hourOfDay = 18.5f,
        ambientBrightness = 0.75f,
        skyTopColor = Color(0xFF4C0519),
        skyBottomColor = Color(0xFFF97316),
        sunOrMoonColor = Color(0xFFEA580C),
        isNight = false
    ),
    TWILIGHT(
        displayName = "Twilight Dusk",
        hourOfDay = 20.0f,
        ambientBrightness = 0.45f,
        skyTopColor = Color(0xFF0F172A),
        skyBottomColor = Color(0xFF6B21A8),
        sunOrMoonColor = Color(0xFFE2E8F0),
        isNight = true
    ),
    MIDNIGHT(
        displayName = "Deep Midnight",
        hourOfDay = 0.0f,
        ambientBrightness = 0.25f,
        skyTopColor = Color(0xFF030712),
        skyBottomColor = Color(0xFF1E293B),
        sunOrMoonColor = Color(0xFFF1F5F9),
        isNight = true
    )
}
