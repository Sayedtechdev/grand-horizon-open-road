package com.example.model

enum class TravelCompanion(
    val id: String,
    val displayName: String,
    val gender: String,
    val title: String,
    val hometown: String,
    val bio: String,
    val favoriteCountry: CountryType,
    val personalityEmoji: String,
    val dialogueQuotes: List<String>
) {
    SAYED(
        id = "sayed",
        displayName = "Sayed",
        gender = "Male",
        title = "Handsome Grand Tour Driver",
        hometown = "Chittagong & Istanbul Traveler",
        bio = "Handsome, confident expedition leader and master driver. Loves high-speed straightaways, technical mountain passes, and pushing the AeroDrive hypercar to its peak.",
        favoriteCountry = CountryType.BANGLADESH,
        personalityEmoji = "⚡",
        dialogueQuotes = listOf(
            "The road is wide open! Let's press the throttle and feel the fusion power.",
            "Look at that suspension bridge ahead—what a magnificent structure!",
            "Chittagong to Istanbul, this journey is going to be legendary.",
            "Step 3 flight thrusters are ready whenever you want to take to the skies!",
            "Perfect drift on that corner! This machine handles like pure magic."
        )
    ),
    HENA(
        id = "hena",
        displayName = "Hena",
        gender = "Female",
        title = "Beautiful Scenic Navigator",
        hometown = "Istanbul & Dhaka Wanderer",
        bio = "Beautiful and elegant travel companion. Enjoys golden sunset horizons, coastal sea breezes, and peaceful open-world highway exploration.",
        favoriteCountry = CountryType.TURKEY,
        personalityEmoji = "✨",
        dialogueQuotes = listOf(
            "The golden sunset lighting over the highway looks so magical!",
            "Look at the hot air balloons floating over Cappadocia's horizon!",
            "Just listen to that smooth engine hum... this drive is so peaceful.",
            "The Mediterranean ocean breeze feels so refreshing today.",
            "Let's cruise along the waterfront promenade and take in the view."
        )
    ),
    AFIA(
        id = "afia",
        displayName = "Afia",
        gender = "Female",
        title = "Beautiful Highway Explorer",
        hometown = "Chittagong & Antalya Traveler",
        bio = "Beautiful, vibrant and cheerful co-pilot. Always spots the most scenic landmarks, river bridges, and tea estates across the country.",
        favoriteCountry = CountryType.BANGLADESH,
        personalityEmoji = "💫",
        dialogueQuotes = listOf(
            "Welcome to the open road! The coastal view from here is breathtaking.",
            "Approaching the Padma River mega-bridge, get ready for an epic view!",
            "Monsoon rain on the windshield looks so cinematic and cozy.",
            "The emerald green tea garden hills look endless from here!",
            "Flying car mode in Step 3 lets us soar right above the clouds!"
        )
    )
}
