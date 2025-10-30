package com.plantakwatix.numberup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.plantakwatix.numberup.ui.GameScreen
import com.plantakwatix.numberup.ui.HomeScreen
import com.plantakwatix.numberup.ui.HowToPlayScreen
import com.plantakwatix.numberup.ui.theme.NumberUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NumberUpTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = AppRoutes.HOME_SCREEN
                    ) {
                        composable(AppRoutes.HOME_SCREEN) {
                            HomeScreen(navController = navController)
                        }
                        composable(AppRoutes.GAME_SCREEN) {
                            GameScreen(navController = navController)
                        }
                        composable(AppRoutes.HOW_TO_PLAY_SCREEN) {
                            HowToPlayScreen(navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NumberUpTheme {
        Greeting("Android")
    }
}