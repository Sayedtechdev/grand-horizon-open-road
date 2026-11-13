package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.data.GamePreferencesRepository
import com.example.model.CountryType
import com.example.ui.DriveGameScreen
import com.example.ui.MainMenuScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppScreen {
    MENU,
    DRIVE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF090D16)
                ) {
                    val repository = remember { GamePreferencesRepository(this) }
                    var currentScreen by remember { mutableStateOf(AppScreen.MENU) }
                    var activeCountry by remember { mutableStateOf(repository.currentCountry) }

                    when (currentScreen) {
                        AppScreen.MENU -> {
                            MainMenuScreen(
                                repository = repository,
                                onEnterCountryRoad = { country ->
                                    activeCountry = country
                                    currentScreen = AppScreen.DRIVE
                                }
                            )
                        }
                        AppScreen.DRIVE -> {
                            DriveGameScreen(
                                initialCountry = activeCountry,
                                onBackToMenu = {
                                    currentScreen = AppScreen.MENU
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
