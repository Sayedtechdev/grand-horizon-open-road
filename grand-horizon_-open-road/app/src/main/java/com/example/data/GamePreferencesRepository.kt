package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.CarColor
import com.example.model.CountryType
import com.example.model.StepStage

class GamePreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("grand_horizon_prefs", Context.MODE_PRIVATE)

    var currentCountry: CountryType
        get() {
            val raw = prefs.getString("current_country", CountryType.TURKEY.id)
            return if (raw == CountryType.BANGLADESH.id) CountryType.BANGLADESH else CountryType.TURKEY
        }
        set(value) = prefs.edit().putString("current_country", value.id).apply()

    var currentStageNumber: Int
        get() = prefs.getInt("current_stage_number", 1)
        set(value) = prefs.edit().putInt("current_stage_number", value).apply()

    var isStep1Completed: Boolean
        get() = prefs.getBoolean("step_1_completed", false)
        set(value) = prefs.edit().putBoolean("step_1_completed", value).apply()

    var isStep2Completed: Boolean
        get() = prefs.getBoolean("step_2_completed", false)
        set(value) = prefs.edit().putBoolean("step_2_completed", value).apply()

    val isFlyingCarUnlocked: Boolean
        get() = isStep1Completed && isStep2Completed

    var selectedCarColor: CarColor
        get() {
            val id = prefs.getString("selected_car_color", CarColor.CYBER_CYAN.id)
            return CarColor.entries.find { it.id == id } ?: CarColor.CYBER_CYAN
        }
        set(value) = prefs.edit().putString("selected_car_color", value.id).apply()

    var selectedCompanion: com.example.model.TravelCompanion
        get() {
            val id = prefs.getString("selected_companion", com.example.model.TravelCompanion.SAYED.id)
            return com.example.model.TravelCompanion.entries.find { it.id == id } ?: com.example.model.TravelCompanion.SAYED
        }
        set(value) = prefs.edit().putString("selected_companion", value.id).apply()

    var totalKmTurkey: Float
        get() = prefs.getFloat("total_km_turkey", 0.0f)
        set(value) = prefs.edit().putFloat("total_km_turkey", value).apply()

    var totalKmBangladesh: Float
        get() = prefs.getFloat("total_km_bangladesh", 0.0f)
        set(value) = prefs.edit().putFloat("total_km_bangladesh", value).apply()

    var soundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

    var radioIndex: Int
        get() = prefs.getInt("radio_index", 0)
        set(value) = prefs.edit().putInt("radio_index", value).apply()

    fun markStepCompleted(stage: StepStage) {
        when (stage) {
            StepStage.STEP_1 -> isStep1Completed = true
            StepStage.STEP_2 -> isStep2Completed = true
            StepStage.STEP_3 -> { /* infinite open exploration */ }
        }
    }
}
