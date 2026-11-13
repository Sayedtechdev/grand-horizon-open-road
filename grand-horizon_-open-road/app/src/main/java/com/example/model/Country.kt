package com.example.model

import androidx.compose.ui.graphics.Color

enum class CountryType(val id: String) {
    TURKEY("turkey"),
    BANGLADESH("bangladesh")
}

data class CityWaypoint(
    val id: String,
    val name: String,
    val localName: String,
    val distanceKm: Float,
    val landmarkName: String,
    val landmarkDescription: String,
    val scenicType: ScenicType,
    val imageResName: String
)

enum class ScenicType {
    SUSPENSION_BRIDGE,
    COASTAL_STRAIT,
    MOUNTAIN_PASS,
    FAIRY_CHIMNEYS,
    HISTORIC_CASTLE,
    PORT_HARBOR,
    TEA_ESTATE,
    RIVER_DELTA,
    MARINE_DRIVE,
    MEGA_BRIDGE
}

data class CityProgress(
    val currentCityIndex: Int,
    val nextCityIndex: Int,
    val activeCity: CityWaypoint,
    val nextCity: CityWaypoint,
    val remainingKmToNext: Float,
    val overallProgress: Float,
    val scaledOdometerKm: Float
)

data class CountryRoute(
    val type: CountryType,
    val displayName: String,
    val localName: String,
    val flagEmoji: String,
    val startCity: String,
    val routeCode: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val asphaltColor: Color,
    val curbColorA: Color,
    val curbColorB: Color,
    val signBackgroundColor: Color,
    val scenicHeroResName: String,
    val description: String,
    val cities: List<CityWaypoint>
) {
    fun findCityProgress(odometerKm: Float): CityProgress {
        val lastScaledKm = (cities.last().distanceKm * 0.05f) + 15f
        val effectiveKm = if (lastScaledKm > 0f && odometerKm >= lastScaledKm) {
            odometerKm % lastScaledKm
        } else {
            odometerKm.coerceAtLeast(0f)
        }
        val currentIdx = cities.indexOfLast { effectiveKm >= (it.distanceKm * 0.05f) }.coerceAtLeast(0)
        val nextIdx = (currentIdx + 1) % cities.size
        val nextThresholdKm = if (nextIdx == 0) lastScaledKm else (cities[nextIdx].distanceKm * 0.05f)
        val remaining = (nextThresholdKm - effectiveKm).coerceAtLeast(0f)
        val overall = (effectiveKm / lastScaledKm.coerceAtLeast(1f)).coerceIn(0.05f, 1f)
        return CityProgress(
            currentCityIndex = currentIdx,
            nextCityIndex = nextIdx,
            activeCity = cities[currentIdx],
            nextCity = cities[nextIdx],
            remainingKmToNext = remaining,
            overallProgress = overall,
            scaledOdometerKm = effectiveKm
        )
    }

    companion object {
        val TURKEY = CountryRoute(
            type = CountryType.TURKEY,
            displayName = "Turkey",
            localName = "Türkiye",
            flagEmoji = "🇹🇷",
            startCity = "Istanbul",
            routeCode = "O-4 / D100 Anatolian Highway",
            primaryColor = Color(0xFFE11D48),
            secondaryColor = Color(0xFFFDE047),
            asphaltColor = Color(0xFF1E2229),
            curbColorA = Color(0xFFE11D48),
            curbColorB = Color(0xFFFFFFFF),
            signBackgroundColor = Color(0xFF15803D), // Karayolları green
            scenicHeroResName = "img_city_istanbul",
            description = "Start from the historic Bosphorus suspension bridges in Istanbul, crossing the Marmara Sea, the high Anatolian steppes of Ankara, the magical fairy chimneys of Cappadocia, and downward to the turquoise Mediterranean cliffs of Antalya and the Aegean coast of Izmir.",
            cities = listOf(
                CityWaypoint(
                    id = "tr_istanbul",
                    name = "Istanbul",
                    localName = "İstanbul",
                    distanceKm = 0f,
                    landmarkName = "Bosphorus Bridge & Strait",
                    landmarkDescription = "Continent-spanning suspension bridge connecting Europe & Asia over turquoise waters.",
                    scenicType = ScenicType.SUSPENSION_BRIDGE,
                    imageResName = "img_city_istanbul"
                ),
                CityWaypoint(
                    id = "tr_kocaeli",
                    name = "Kocaeli & İzmit",
                    localName = "İzmit Körfezi",
                    distanceKm = 100f,
                    landmarkName = "Osmangazi Suspension Bridge",
                    landmarkDescription = "Fourth-longest suspension bridge crossing the shimmering Gulf of İzmit.",
                    scenicType = ScenicType.COASTAL_STRAIT,
                    imageResName = "img_turkey_highway"
                ),
                CityWaypoint(
                    id = "tr_bursa",
                    name = "Bursa",
                    localName = "Yeşil Bursa",
                    distanceKm = 155f,
                    landmarkName = "Uludağ Mountain Peaks",
                    landmarkDescription = "Ancient Ottoman capital nestled beneath snow-dusted pine mountains.",
                    scenicType = ScenicType.MOUNTAIN_PASS,
                    imageResName = "img_turkey_highway"
                ),
                CityWaypoint(
                    id = "tr_eskisehir",
                    name = "Eskişehir",
                    localName = "Eskişehir",
                    distanceKm = 310f,
                    landmarkName = "Porsuk River Gondolas",
                    landmarkDescription = "Vibrant canal bridges and modern Anatolian boulevard culture.",
                    scenicType = ScenicType.RIVER_DELTA,
                    imageResName = "img_turkey_highway"
                ),
                CityWaypoint(
                    id = "tr_ankara",
                    name = "Ankara",
                    localName = "Ankara",
                    distanceKm = 450f,
                    landmarkName = "Anıtkabir & Capital Citadel",
                    landmarkDescription = "Modern capital rising above the vast Anatolian high plateau.",
                    scenicType = ScenicType.HISTORIC_CASTLE,
                    imageResName = "img_turkey_highway"
                ),
                CityWaypoint(
                    id = "tr_cappadocia",
                    name = "Cappadocia",
                    localName = "Kapadokya / Göreme",
                    distanceKm = 730f,
                    landmarkName = "Fairy Chimneys & Hot Air Balloons",
                    landmarkDescription = "Surreal volcanic tuff stone towers with dozens of colorful balloons floating at sunrise.",
                    scenicType = ScenicType.FAIRY_CHIMNEYS,
                    imageResName = "img_city_cappadocia"
                ),
                CityWaypoint(
                    id = "tr_konya",
                    name = "Konya",
                    localName = "Konya",
                    distanceKm = 960f,
                    landmarkName = "Mevlana Rumi Sanctuary",
                    landmarkDescription = "Sprawling central Anatolian plains and historic spiritual crossroads.",
                    scenicType = ScenicType.MOUNTAIN_PASS,
                    imageResName = "img_turkey_highway"
                ),
                CityWaypoint(
                    id = "tr_antalya",
                    name = "Antalya",
                    localName = "Antalya",
                    distanceKm = 1260f,
                    landmarkName = "Taurus Cliffs & Turquoise Sea",
                    landmarkDescription = "Breathtaking Mediterranean coastal highway lined with palm trees and ocean bluffs.",
                    scenicType = ScenicType.MARINE_DRIVE,
                    imageResName = "img_turkey_highway"
                ),
                CityWaypoint(
                    id = "tr_pamukkale",
                    name = "Pamukkale",
                    localName = "Pamukkale / Denizli",
                    distanceKm = 1500f,
                    landmarkName = "White Travertine Terraces",
                    landmarkDescription = "Glistening snow-white mineral pools cascading down thermal hillsides.",
                    scenicType = ScenicType.FAIRY_CHIMNEYS,
                    imageResName = "img_turkey_highway"
                ),
                CityWaypoint(
                    id = "tr_izmir",
                    name = "İzmir",
                    localName = "İzmir",
                    distanceKm = 1740f,
                    landmarkName = "Aegean Kordon & Clock Tower",
                    landmarkDescription = "Sunny seaside coastal expressway bordering the Aegean maritime horizon.",
                    scenicType = ScenicType.COASTAL_STRAIT,
                    imageResName = "img_turkey_highway"
                )
            )
        )

        val BANGLADESH = CountryRoute(
            type = CountryType.BANGLADESH,
            displayName = "Bangladesh",
            localName = "বাংলাদেশ",
            flagEmoji = "🇧🇩",
            startCity = "Chittagong",
            routeCode = "N1 / N2 Grand Asian Highway",
            primaryColor = Color(0xFF059669),
            secondaryColor = Color(0xFFE11D48),
            asphaltColor = Color(0xFF1B1F26),
            curbColorA = Color(0xFF059669),
            curbColorB = Color(0xFFFDE047),
            signBackgroundColor = Color(0xFF065F46), // Bangladeshi RHD green
            scenicHeroResName = "img_city_chittagong",
            description = "Start from the bustling port city of Chittagong and Karnaphuli river, tracing the world-famous Cox's Bazar marine drive, crossing into the grand capital of Dhaka across the historic Padma Bridge, exploring Sylhet's rolling emerald tea gardens, and spanning across to the Jamuna River and the Sundarbans.",
            cities = listOf(
                CityWaypoint(
                    id = "bd_chittagong",
                    name = "Chittagong",
                    localName = "চট্টগ্রাম (Chattogram)",
                    distanceKm = 0f,
                    landmarkName = "Karnaphuli River & Port Harbor",
                    landmarkDescription = "Gateway port city with towering shipping cranes, Patenga beach marine drive, and river estuary.",
                    scenicType = ScenicType.PORT_HARBOR,
                    imageResName = "img_city_chittagong"
                ),
                CityWaypoint(
                    id = "bd_coxsbazar",
                    name = "Cox's Bazar",
                    localName = "কক্সবাজার",
                    distanceKm = 150f,
                    landmarkName = "Marine Drive Oceanway",
                    landmarkDescription = "World's longest continuous natural beach highway flanked by roaring sea waves and green hills.",
                    scenicType = ScenicType.MARINE_DRIVE,
                    imageResName = "img_bangladesh_highway"
                ),
                CityWaypoint(
                    id = "bd_feni",
                    name = "Feni & Muhuri",
                    localName = "ফেনী",
                    distanceKm = 240f,
                    landmarkName = "Muhuri River Estuary",
                    landmarkDescription = "Lush green floodplains dotted with palm trees and water lilies.",
                    scenicType = ScenicType.RIVER_DELTA,
                    imageResName = "img_bangladesh_highway"
                ),
                CityWaypoint(
                    id = "bd_comilla",
                    name = "Comilla",
                    localName = "কুমিল্লা",
                    distanceKm = 340f,
                    landmarkName = "Mainamati Ancient Stupas",
                    landmarkDescription = "Famous highway food oasis and historic red-clay archaeological valleys.",
                    scenicType = ScenicType.HISTORIC_CASTLE,
                    imageResName = "img_bangladesh_highway"
                ),
                CityWaypoint(
                    id = "bd_dhaka",
                    name = "Dhaka",
                    localName = "ঢাকা",
                    distanceKm = 440f,
                    landmarkName = "Padma Mega Bridge & Hatirjheel",
                    landmarkDescription = "Monumental two-level truss bridge over the mighty Padma River connecting the nation.",
                    scenicType = ScenicType.MEGA_BRIDGE,
                    imageResName = "img_city_dhaka"
                ),
                CityWaypoint(
                    id = "bd_gazipur",
                    name = "Gazipur & Bhawal",
                    localName = "গাজীপুর",
                    distanceKm = 480f,
                    landmarkName = "Bhawal Sal Forest Highway",
                    landmarkDescription = "Dense tropical deciduous canopy with modern elevated expressway flyovers.",
                    scenicType = ScenicType.MOUNTAIN_PASS,
                    imageResName = "img_bangladesh_highway"
                ),
                CityWaypoint(
                    id = "bd_sylhet",
                    name = "Sylhet",
                    localName = "সিলেট",
                    distanceKm = 720f,
                    landmarkName = "Jaflong & Rolling Tea Estates",
                    landmarkDescription = "Endless undulating emerald green tea hill slopes enveloped in soft misty rain.",
                    scenicType = ScenicType.TEA_ESTATE,
                    imageResName = "img_bangladesh_highway"
                ),
                CityWaypoint(
                    id = "bd_mymensingh",
                    name = "Mymensingh",
                    localName = "ময়মনসিংহ",
                    distanceKm = 940f,
                    landmarkName = "Old Brahmaputra Riverbank",
                    landmarkDescription = "Serene agricultural heartland lined with banyan trees and yellow mustard fields.",
                    scenicType = ScenicType.RIVER_DELTA,
                    imageResName = "img_bangladesh_highway"
                ),
                CityWaypoint(
                    id = "bd_jamuna",
                    name = "Jamuna / Sirajganj",
                    localName = "যমুনা সেতু",
                    distanceKm = 1120f,
                    landmarkName = "Bangabandhu Jamuna Bridge",
                    landmarkDescription = "Epic 4.8 km bridge spanning one of the widest braided riverbeds in Asia.",
                    scenicType = ScenicType.MEGA_BRIDGE,
                    imageResName = "img_city_dhaka"
                ),
                CityWaypoint(
                    id = "bd_rajshahi",
                    name = "Rajshahi & Khulna",
                    localName = "রাজশাহী ও সুন্দরবন",
                    distanceKm = 1380f,
                    landmarkName = "Sundarbans Delta Mangrove Gateway",
                    landmarkDescription = "Pristine mangrove waterways, peaceful silk city boulevards, and magnificent river sunsets.",
                    scenicType = ScenicType.RIVER_DELTA,
                    imageResName = "img_bangladesh_highway"
                )
            )
        )

        fun getRoute(type: CountryType): CountryRoute = when (type) {
            CountryType.TURKEY -> TURKEY
            CountryType.BANGLADESH -> BANGLADESH
        }
    }
}
