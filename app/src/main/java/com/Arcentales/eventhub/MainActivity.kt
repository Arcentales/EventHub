package com.Arcentales.eventhub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.Arcentales.eventhub.data.preferences.ThemePreferences
import com.Arcentales.eventhub.navigation.NavGraph
import com.Arcentales.eventhub.ui.theme.EventHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themePreferences = remember { ThemePreferences(applicationContext) }
            val dynamicColorEnabled by themePreferences.dynamicColorEnabled.collectAsState(initial = false)
            val darkModePref by themePreferences.darkModeEnabled.collectAsState(initial = null)
            
            val isDarkTheme = when (darkModePref) {
                true -> true
                false -> false
                null -> isSystemInDarkTheme()
            }

            EventHubTheme(
                darkTheme = isDarkTheme,
                dynamicColor = dynamicColorEnabled
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavGraph(navController = navController)
                }
            }
        }
    }
}
