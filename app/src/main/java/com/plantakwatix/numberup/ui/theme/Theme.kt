package com.plantakwatix.numberup.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Thème Clair "Crème & Corail"
private val AppLightColorScheme = lightColorScheme(
    primary = BrightCoral,           // Boutons et accents principaux
    onPrimary = OnCoral,             // Texte sur les boutons
    secondary = SoftBlue,            // Accents secondaires
    onSecondary = OnSoftBlue,        // Texte sur les accents secondaires

    background = CreamBackground,    // Fond général de l'app
    onBackground = DarkCharcoalText, // Texte principal

    surface = CardSurface,           // Fond des cartes, dialogues
    onSurface = DarkCharcoalText,    // Texte sur les cartes

    outline = SubtleBorder,          // Bordures (ex: grille de jeu)
    // ... les autres couleurs sont générées par défaut, ce qui est suffisant pour commencer
)

// Thème Sombre "Ardoise"
private val AppDarkColorScheme = darkColorScheme(
    primary = BrightCoral,           // Le corail ressort bien sur un fond sombre
    onPrimary = OnCoral,
    secondary = SoftBlue,
    onSecondary = OnSoftBlue,

    background = Color(0xFF1B262C), // Un bleu-gris très foncé
    onBackground = Color(0xFFE0E0E0), // Texte blanc cassé

    surface = Color(0xFF233138),     // Surface légèrement plus claire
    onSurface = Color(0xFFE0E0E0),

    outline = Color(0xFF3F4E56),     // Bordures plus sombres
)

@Composable
fun NumberUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> AppDarkColorScheme
        else -> AppLightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Les icônes de la barre de statut doivent être sombres sur fond clair, et claires sur fond sombre
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Nous utiliserons la typo par défaut pour l'instant
        content = content
    )
}