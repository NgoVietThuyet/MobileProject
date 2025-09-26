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

    // Dọc (giống layout ban đầu)
    val BluePurpleVertical = Brush.verticalGradient(
        listOf(Color(0xFF4C80FF), Color(0xFFA847FF))
    )
    val BlueGreenVertical = Brush.verticalGradient(
        listOf(Color(0xFF4C80FF), Color(0xFF3DDC84))
    )
}
