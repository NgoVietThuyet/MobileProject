package com.example.test.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object AppGradient {
    // Ngang
    val BluePurple = Brush.horizontalGradient(
        listOf(Color(0xFF4C80FF), Color(0xFFA847FF))
    )
    val BlueGreen = Brush.horizontalGradient(
        listOf(Color(0xFF4C80FF), Color(0xFF3DDC84))
    )
    val PurplePink = Brush.horizontalGradient(
        listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
    )
    val OrangePink = Brush.horizontalGradient(
        listOf(Color(0xFFF97316), Color(0xFFEC4899))
    )
    val TealBlue = Brush.horizontalGradient(
        listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
    )
    val AmberRose = Brush.horizontalGradient(
        listOf(Color(0xFFF59E0B), Color(0xFFF43F5E))
    )

    // Dọc
    val BluePurpleVertical = Brush.verticalGradient(
        listOf(Color(0xFF4C80FF), Color(0xFFA847FF))
    )
    val BlueGreenVertical = Brush.verticalGradient(
        listOf(Color(0xFF4C80FF), Color(0xFF3DDC84))
    )
    val PurplePinkVertical = Brush.verticalGradient(
        listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
    )
    val OrangePinkVertical = Brush.verticalGradient(
        listOf(Color(0xFFF97316), Color(0xFFEC4899))
    )
    val TealBlueVertical = Brush.verticalGradient(
        listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
    )
    val AmberRoseVertical = Brush.verticalGradient(
        listOf(Color(0xFFF59E0B), Color(0xFFF43F5E))
    )
}
